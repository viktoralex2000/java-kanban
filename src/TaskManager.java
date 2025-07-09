import java.util.*;

public class TaskManager {
    private int idCount = 1;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, EpicTask> epics;
    private HashMap<Integer, SubTask> subtasks;

    TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    private int newId() {
        return idCount++;
    }

    // Методы задач

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void createTask(Task task) {
        task.setId(newId());
        tasks.put(task.getId(), task);
    }

    public void updateTask(Task task) {
        if(!tasks.containsKey(task.getId())) return;
        tasks.put(task.getId(), task);
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    //Методы эпиков

    public ArrayList<EpicTask> getAllEpicTasks() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllEpicTasks() {
        epics.clear();
        subtasks.clear();
    }

    public Task getEpicTaskById(int id) {
        return epics.get(id);
    }

    public void createEpicTask(EpicTask epic) {
        epic.setId(newId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    public void updateEpicTask(EpicTask newEpic) {
        if(!epics.containsKey(newEpic.getId())) return;
        EpicTask oldEpic = epics.get(newEpic.getId());
        newEpic.clearEpic();
        newEpic.getSubtaskIdList().addAll(oldEpic.getSubtaskIdList());
        epics.put(newEpic.getId(), newEpic);
        updateEpicStatus(newEpic);
    }

    public void deleteEpicTaskById(int id) {
        EpicTask epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIdList()) {
                subtasks.remove(subId);
            }
        }
    }

    //Методы подзадач

    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubTasks() {
        for (EpicTask epic : epics.values()) {
            epic.clearEpic();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    public SubTask getSubTaskById(int id) {
        return subtasks.get(id);
    }

    public void createSubTask(SubTask subtask) {
        subtask.setId(newId());
        subtasks.put(subtask.getId(), subtask);
        EpicTask epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubTask(subtask.getId());
            updateEpicStatus(epic);
        }
    }

    public void updateSubTask(SubTask subtask) {
        if(!subtasks.containsKey(subtask.getId())) return;
        subtasks.put(subtask.getId(), subtask);
        EpicTask epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

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

    public ArrayList<SubTask> getSubtasksOfEpic(int epicId) {
        EpicTask epic = epics.get(epicId);
        ArrayList<SubTask> result = new ArrayList<>();
        if (epic == null) return result;
        for (int subId : epic.getSubtaskIdList()) {
            result.add(subtasks.get(subId));
        }
        return result;
    }

    private void updateEpicStatus(EpicTask epic) {
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