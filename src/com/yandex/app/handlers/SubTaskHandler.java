package com.yandex.app.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.SubTask;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.util.ArrayList;

public class SubTaskHandler extends BaseHttpHandler<SubTask> implements HttpHandler {
    public SubTaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson, SubTask.class, "subtasks");
    }

    @Override
    protected boolean createTask(Task task) {
        return manager.createSubTask((SubTask) task);
    }

    @Override
    protected boolean updateTask(Task task) {
        return manager.updateSubTask((SubTask) task);
    }

    @Override
    protected SubTask getTaskById(int id) {
        return manager.getSubTaskById(id);
    }

    @Override
    protected ArrayList<SubTask> getAllTasks() {
        return manager.getAllSubTasks();
    }

    @Override
    protected void deleteTaskById(int id) {
        manager.deleteSubTaskById(id);
    }

    @Override
    protected void deleteAllTasks() {
        manager.deleteAllSubTasks();
    }

}
