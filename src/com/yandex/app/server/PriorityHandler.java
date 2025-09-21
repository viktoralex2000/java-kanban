package com.yandex.app.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.service.TaskManager;

import java.io.IOException;

public class PriorityHandler implements HttpHandler {
    TaskManager manager;
    Gson gson;

    public PriorityHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        String response = gson.toJson(manager.getPrioritizedTasks());
        byte[] bytes = response.getBytes();
        try {
            httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            httpExchange.sendResponseHeaders(200, bytes.length);
            try (var os = httpExchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            try {
                httpExchange.sendResponseHeaders(500, 0);
                httpExchange.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }
}
