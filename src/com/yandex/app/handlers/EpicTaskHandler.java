package com.yandex.app.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.EpicTask;
import com.yandex.app.model.SubTask;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.util.ArrayList;

public class EpicTaskHandler extends BaseHttpHandler<EpicTask> implements HttpHandler {
    public EpicTaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson, EpicTask.class, "epics");
    }

    @Override
    public void handle(HttpExchange httpEx) {
        httpExchange = httpEx;
        String path = httpExchange.getRequestURI().getPath();
        String method = httpExchange.getRequestMethod();

        try {
            if (path.equals("/" + endPoint)) {
                switch (method) {
                    case "GET" -> getAll();
                    case "POST" -> create();
                    case "DELETE" -> deleteAll();
                    default -> sendResponse(404, null);
                }
            } else if (path.matches("^/" + endPoint + "/\\d+$")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                switch (method) {
                    case "GET" -> getById(id);
                    case "POST" -> update(id);
                    case "DELETE" -> deleteById(id);
                    default -> sendResponse(404, null);
                }
            } else if (path.matches("^/" + endPoint + "/\\d+/subtasks$")) {
                int id = Integer.parseInt(path.split("/")[2]);
                if ("GET".equals(method)) {
                    getSubtasksOfEpic(id);
                } else {
                    sendResponse(500, null);
                }
            } else {
                sendResponse(404, null);
            }
        } catch (Exception e) {
            System.out.println(e);
            sendResponse(500, null);
        }
    }


    private void getSubtasksOfEpic(int epicId) {
        ArrayList<SubTask> subtasks = manager.getSubtasksOfEpic(epicId);
        if (subtasks != null && !subtasks.isEmpty()) {
            sendResponse(200, gson.toJson(subtasks));
        } else {
            sendResponse(404, null);
        }
    }

    @Override
    protected boolean createTask(Task task) {
        return manager.createEpicTask((EpicTask) task);
    }

    @Override
    protected boolean updateTask(Task task) {
        return manager.updateEpicTask((EpicTask) task);
    }

    @Override
    protected EpicTask getTaskById(int id) {
        return (EpicTask) manager.getEpicTaskById(id);
    }

    @Override
    protected ArrayList<EpicTask> getAllTasks() {
        return manager.getAllEpicTasks();
    }

    @Override
    protected void deleteTaskById(int id) {
        manager.deleteEpicTaskById(id);
    }

    @Override
    protected void deleteAllTasks() {
        manager.deleteAllEpicTasks();
    }
}
