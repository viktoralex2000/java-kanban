package com.yandex.app.handlers;

import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;
import com.google.gson.Gson;

import java.util.ArrayList;

public class TaskHandler extends BaseHttpHandler<Task> implements HttpHandler {
    public TaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson, Task.class, "tasks");
    }

    @Override
    protected boolean createTask(Task task) {
        return manager.createTask(task);
    }

    @Override
    protected boolean updateTask(Task task) {
        return manager.updateTask(task);
    }

    @Override
    protected Task getTaskById(int id) {
        return manager.getTaskById(id);
    }

    @Override
    protected ArrayList<Task> getAllTasks() {
        return manager.getAllTasks();
    }

    @Override
    protected void deleteTaskById(int id) {
        manager.deleteTaskById(id);
    }

    @Override
    protected void deleteAllTasks() {
        manager.deleteAllTasks();
    }

}