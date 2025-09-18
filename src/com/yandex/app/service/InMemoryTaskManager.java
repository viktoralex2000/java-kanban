package com.yandex.app.service;

import com.yandex.app.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int idCount = 1;
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, EpicTask> epics = new HashMap<>();
    private HashMap<Integer, SubTask> subtasks = new HashMap<>();
    private HistoryManager historyManager;
    private boolean isRestoreMode = false;
    private final TreeSet<Task> priorityTaskTree = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo))
                    .thenComparing(Task::getId)
    );

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

    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(priorityTaskTree);
    }

    // Методы задач

    @Override
    public void createTask(Task task) {
        if (!isRestoreMode) {
            task.setId(newId());
        }

        if (isTimeOverlap(task)) {
            throw new IllegalArgumentException("Новая задача пересекается по времени с существующей");
        }

        tasks.put(task.getId(), task);
        priorityTaskTree.add(task);
    }

    @Override
    public void updateTask(Task task) {
        int id = task.getId();
        if (!tasks.containsKey(id)) {
            return;
        }

        if (isTimeOverlap(task)) {
            throw new IllegalArgumentException("Обновляемая задача пересекается по времени с существующей");
        }

        Task oldTask = tasks.get(id);
        priorityTaskTree.remove(oldTask);
        tasks.put(id, task);
        priorityTaskTree.add(task);
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
        Task task = tasks.remove(id);
        if (task != null) {
            priorityTaskTree.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            priorityTaskTree.remove(task);
            historyManager.remove(task.getId());
        }
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
        if (!epics.containsKey(newEpic.getId())) {
            return;
        }
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

        if (isTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Новая подзадача пересекается по времени с существующей");
        }

        subtasks.put(subtask.getId(), subtask);
        priorityTaskTree.add(subtask);

        epic.addSubTask(subtask.getId());
        updateEpicStatus(epic);
        updateEpicTimeAndDuration(epic);
    }

    @Override
    public void updateSubTask(SubTask newSubTask) {
        if (!subtasks.containsKey(newSubTask.getId())) {
            return;
        }

        if (isTimeOverlap(newSubTask)) {
            throw new IllegalArgumentException("Обновляемая подзадача пересекается по времени с существующей");
        }

        SubTask oldSubTask = subtasks.get(newSubTask.getId());
        priorityTaskTree.remove(oldSubTask);
        subtasks.put(newSubTask.getId(), newSubTask);
        priorityTaskTree.add(newSubTask);

        EpicTask epic = epics.get(newSubTask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
            updateEpicTimeAndDuration(epic);
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
        if (epic == null) {
            return result;
        }
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
            priorityTaskTree.remove(subtask); // удаляем из дерева
            EpicTask epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubTask(id);
                updateEpicStatus(epic);
                updateEpicTimeAndDuration(epic);
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
        for (SubTask subTask : subtasks.values()) {
            priorityTaskTree.remove(subTask);
            historyManager.remove(subTask.getId());
        }
        subtasks.clear();

        // Обновляем эпики
        for (EpicTask epic : epics.values()) {
            epic.clearEpic();
            updateEpicStatus(epic);
            updateEpicTimeAndDuration(epic);
        }
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

    private void updateEpicTimeAndDuration(EpicTask epic) {
        List<SubTask> subTasksList = getSubtasksOfEpic(epic.getId());
        if (subTasksList.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            return;
        }

        LocalDateTime start = subTasksList.stream()
                .map(SubTask::getStartTime)
                .filter(t -> t != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime end = subTasksList.stream()
                .map(SubTask::getEndTime)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        Duration totalDuration = subTasksList.stream()
                .map(SubTask::getDuration)
                .filter(d -> d != null)
                .reduce(Duration.ZERO, Duration::plus);

        epic.setStartTime(start);
        epic.setDuration(totalDuration);
    }

    private boolean isTimeOverlap(Task newTask) {
        for (Task task : priorityTaskTree) {
            if (task.getStartTime() == null || newTask.getStartTime() == null) {
                continue;
            }
            LocalDateTime t1Start = task.getStartTime();
            LocalDateTime t1End = task.getEndTime();
            LocalDateTime t2Start = newTask.getStartTime();
            LocalDateTime t2End = newTask.getEndTime();
            if (!t2End.isBefore(t1Start) && !t2Start.isAfter(t1End)) {
                return true;
            }
        }
        return false;
    }

}