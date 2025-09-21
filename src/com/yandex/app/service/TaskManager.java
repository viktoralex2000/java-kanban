package com.yandex.app.service;

import com.yandex.app.model.*;

import java.util.ArrayList;

public interface TaskManager {

    ArrayList<Task> getPrioritizedTasks();

    ArrayList<Task> getHistory();

    ArrayList<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    boolean createTask(Task task);

    boolean updateTask(Task task);

    void deleteTaskById(int id);

    ArrayList<EpicTask> getAllEpicTasks();

    void deleteAllEpicTasks();

    Task getEpicTaskById(int id);

    boolean createEpicTask(EpicTask epic);

    boolean updateEpicTask(EpicTask newEpic);

    void deleteEpicTaskById(int id);

    ArrayList<SubTask> getAllSubTasks();

    void deleteAllSubTasks();

    SubTask getSubTaskById(int id);

    boolean createSubTask(SubTask subtask);

    boolean updateSubTask(SubTask newSubTask);

    void deleteSubTaskById(int id);

    ArrayList<SubTask> getSubtasksOfEpic(int epicId);

}
