package com.yandex.app.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Task;
import com.yandex.app.service.TaskManager;

import java.io.IOException;
import java.util.ArrayList;

public abstract class BaseHttpHandler<T extends Task> implements HttpHandler {
    protected TaskManager manager;
    protected Gson gson;
    protected HttpExchange httpExchange;
    private Class<T> type;
    protected String endPoint;

    public BaseHttpHandler(TaskManager manager, Gson gson, Class<T> type, String endPoint) {
        this.manager = manager;
        this.gson = gson;
        this.type = type;
        this.endPoint = endPoint;
    }

    protected abstract boolean createTask(Task task);

    protected abstract boolean updateTask(Task task);

    protected abstract T getTaskById(int id);

    protected abstract ArrayList<T> getAllTasks();

    protected abstract void deleteTaskById(int id);

    protected abstract void deleteAllTasks();

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
                    default -> sendResponse(500, null);
                }
            } else if (path.matches("^/" + endPoint + "/\\d+$")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                switch (method) {
                    case "GET" -> getById(id);
                    case "POST" -> update(id);
                    case "DELETE" -> deleteById(id);
                    default -> sendResponse(500, null);
                }
            } else {
                sendResponse(404, null);
            }
        } catch (Exception e) {
            System.out.println(e);
            sendResponse(500, null);
        }
    }


    protected void sendResponse(int statusCode, String body) {
        try {
            byte[] bytes = body != null ? body.getBytes() : new byte[0];
            httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            httpExchange.sendResponseHeaders(statusCode, bytes.length);
            if (bytes.length > 0) {
                try (var os = httpExchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            httpExchange.close();
        }
    }

    protected void getById(int id) {
        T task = getTaskById(id);
        if (task != null) {
            sendResponse(200, gson.toJson(task));
        } else {
            sendResponse(404, null);
        }
    }

    protected void getAll() {
        sendResponse(200, gson.toJson(getAllTasks()));
    }

    protected void create() {
        try {
            String body = new String(httpExchange.getRequestBody().readAllBytes());
            Task task = gson.fromJson(body, type);
            boolean isCreated = createTask(task);
            sendResponse(isCreated ? 201 : 406, null);
        } catch (Exception e) {
            System.out.println(e);
            sendResponse(500, null);
        }
    }

    protected void update(int id) {
        try {
            String body = new String(httpExchange.getRequestBody().readAllBytes());
            Task task = gson.fromJson(body, type);
            boolean isUpdated = updateTask(task);
            sendResponse(isUpdated ? 201 : 406, null);
        } catch (Exception e) {
            System.out.println(e);
            sendResponse(500, null);
        }
    }

    protected void deleteById(int id) {
        try {
            deleteTaskById(id);
            sendResponse(200, null);
        } catch (Exception e) {
            System.out.println(e);
            sendResponse(500, null);
        }
    }

    protected void deleteAll() {
        try {
            deleteAllTasks();
            sendResponse(200, null);
        } catch (Exception e) {
            System.out.println(e);
            sendResponse(500, null);
        }
    }

}