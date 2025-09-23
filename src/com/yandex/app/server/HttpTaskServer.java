package com.yandex.app.server;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import com.yandex.app.handlers.*;
import com.yandex.app.service.*;
import com.yandex.app.util.json.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

public class HttpTaskServer {
    private static final int PORT = 8081;
    private final TaskManager manager;
    private final Gson gson;
    private HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) {
        gson = getGson();
        this.manager = manager;
    }

    public static void main(String[] args) {
        HttpTaskServer httpTaskServer
                = new HttpTaskServer(Managers.getDefault(Path.of("src\\data\\memory.csv")));
        Scanner scanner = new Scanner(System.in);
        String text = "start - запустить сервер, stop - остановить сервер, exit - закрыть программу";
        String mes;
        boolean serverFlag = false;
        while (true) {
            System.out.println(text);
            mes = scanner.nextLine();
            if (mes.equals("start") && !serverFlag) {
                httpTaskServer.startServer();
                serverFlag = true;
            } else if (mes.equals("stop")) {
                httpTaskServer.stopServer();
                serverFlag = false;
            } else if (mes.equals("exit")) {
                httpTaskServer.stopServer();
                System.out.println("Завершение работы");
                break;
            }
        }
    }

    public void startServer() {
        try {
            if (httpServer == null) {
                httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
                httpServer.createContext("/tasks", new TaskHandler(manager, gson));
                httpServer.createContext("/epics", new EpicTaskHandler(manager, gson));
                httpServer.createContext("/subtask", new SubTaskHandler(manager, gson));
                httpServer.createContext("/history", new HistoryHandler(manager, gson));
                httpServer.createContext("/prioritized", new PriorityHandler(manager, gson));
            }
            httpServer.start();
            System.out.println("Сервер запущен на порту " + PORT);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void stopServer() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
            System.out.println("Сервер остановлен");
        }
    }

    public Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .registerTypeAdapter(Duration.class, new DurationSerializer())
                .registerTypeAdapter(Duration.class, new DurationDeserializer())
                .setPrettyPrinting()
                .create();
    }

}