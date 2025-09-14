package com.yandex.app.service;

import com.yandex.app.model.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int idCount = 1;
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, EpicTask> epics = new HashMap<>();
    private HashMap<Integer, SubTask> subtasks = new HashMap<>();
    private HistoryManager historyManager;
    private boolean isRestoreMode = false;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    private int newId() {
        while (tasks.containsKey(idCount)
                || epics.containsKey(idCount)
                || subtasks.containsKey(idCount)) {
            idCount++;
        }
        return idCount++;
    }

    protected void recalcId() {
        int maxId = 0;
        for (Integer id : tasks.keySet()) maxId = Math.max(maxId, id);
        for (Integer id : epics.keySet()) maxId = Math.max(maxId, id);
        for (Integer id : subtasks.keySet()) maxId = Math.max(maxId, id);
        idCount = maxId + 1;
    }

    protected void enableRestoreMode() {
        this.isRestoreMode = true;
    }

    protected void disableRestoreMode() {
        this.isRestoreMode = false;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Методы задач

    @Override
    public void createTask(Task task) {
        if (!isRestoreMode) {
            task.setId(newId());
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) return;
        tasks.put(task.getId(), task);
    }

    @Override
    public Task getTaskById(int id) {
        Task taskById = tasks.get(id);
        if (taskById != null) {
            historyManager.add(id, taskById);
        }
        return taskById;
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach((id) -> historyManager.remove(id));
        tasks.clear();
    }

    //Методы эпиков

    @Override
    public void createEpicTask(EpicTask epic) {
        if (!isRestoreMode) {
            epic.setId(newId());
        }
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
    public EpicTask getEpicTaskById(int id) {
        EpicTask epicTaskById = epics.get(id);
        if (epicTaskById != null) {
            historyManager.add(id, epicTaskById);
        }
        return epicTaskById;
    }

    @Override
    public void deleteEpicTaskById(int id) {
        EpicTask epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIdList()) {
                historyManager.remove(subId);
                subtasks.remove(subId);
            }
        }
        historyManager.remove(id);
    }

    @Override
    public ArrayList<EpicTask> getAllEpicTasks() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpicTasks() {
        epics.keySet().forEach((id) -> historyManager.remove(id));
        epics.clear();
        subtasks.keySet().forEach((id) -> historyManager.remove(id));
        subtasks.clear();
    }

    //Методы подзадач

    @Override
    public void createSubTask(SubTask subtask) {
        EpicTask epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return;
        }
        if (!isRestoreMode) {
            subtask.setId(newId());
        }
        subtasks.put(subtask.getId(), subtask);
        epic.addSubTask(subtask.getId());
        updateEpicStatus(epic);
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
    public SubTask getSubTaskById(int id) {
        SubTask subTaskById = subtasks.get(id);
        if (subTaskById != null) {
            historyManager.add(id, subTaskById);
        }
        return subTaskById;
    }

    @Override
    public ArrayList<SubTask> getSubtasksOfEpic(int epicId) {
        EpicTask epic = epics.get(epicId);
        ArrayList<SubTask> result = new ArrayList<>();
        if (epic == null) return result;
        for (int subId : epic.getSubtaskIdList()) {
            SubTask subTask = subtasks.get(subId);
            if (subTask != null) result.add(subTask);
        }

        return result;
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
        historyManager.remove(id);
    }

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
        subtasks.keySet().forEach((id) -> historyManager.remove(id));
        subtasks.clear();
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