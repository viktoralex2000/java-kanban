package com.yandex.app.service;

import com.yandex.app.model.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int idCount = 1;
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, EpicTask> epics = new HashMap<>();
    private HashMap<Integer, SubTask> subtasks = new HashMap<>();
    private HistoryManager historyManager;

    InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    int newId() {
        return idCount++;
    }

    // Методы задач

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.updateHistory(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public void createTask(Task task) {
        task.setId(newId());
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) return;
        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    //Методы эпиков

    @Override
    public ArrayList<EpicTask> getAllEpicTasks() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpicTasks() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Task getEpicTaskById(int id) {
        historyManager.updateHistory(epics.get(id));
        return epics.get(id);
    }

    @Override
    public void createEpicTask(EpicTask epic) {
        epic.setId(newId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    @Override
    public void updateEpicTask(EpicTask newEpic) {
        if (!epics.containsKey(newEpic.getId())) return;
        EpicTask oldEpic = epics.get(newEpic.getId());
        oldEpic.setName(newEpic.getName());
        oldEpic.setDescription(newEpic.getDescription());
    }

    @Override
    public void deleteEpicTaskById(int id) {
        EpicTask epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIdList()) {
                subtasks.remove(subId);
            }
        }
    }

    //Методы подзадач

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubTasks() {
        for (EpicTask epic : epics.values()) {
            epic.clearEpic();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    @Override
    public SubTask getSubTaskById(int id) {
        historyManager.updateHistory(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public void createSubTask(SubTask subtask) {
        subtask.setId(newId());
        subtasks.put(subtask.getId(), subtask);
        EpicTask epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubTask(subtask.getId());
            updateEpicStatus(epic);
        }
    }

    @Override
    public void updateSubTask(SubTask newSubTask) {
        if (!subtasks.containsKey(newSubTask.getId())) return;
        SubTask oldSubTask = subtasks.get(newSubTask.getId());
        oldSubTask.setName(newSubTask.getName());
        oldSubTask.setDescription(newSubTask.getDescription());
        oldSubTask.setStatus(newSubTask.getStatus());
        EpicTask epic = epics.get(oldSubTask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteSubTaskById(int id) {
        SubTask subtask = subtasks.remove(id);
        if (subtask != null) {
            EpicTask epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubTask(id);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public ArrayList<SubTask> getSubtasksOfEpic(int epicId) {
        EpicTask epic = epics.get(epicId);
        ArrayList<SubTask> result = new ArrayList<>();
        if (epic == null) return result;
        for (int subId : epic.getSubtaskIdList()) {
            result.add(subtasks.get(subId));
        }
        return result;
    }

    void updateEpicStatus(EpicTask epic) {
        ArrayList<Integer> subTaskIdList = epic.getSubtaskIdList();
        if (subTaskIdList.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;

        for (int subTaskId : subTaskIdList) {
            SubTask subTask = subtasks.get(subTaskId);
            if (subTask == null) continue;

            if (subTask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subTask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }
        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

}