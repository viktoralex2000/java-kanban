package com.yandex.app.server;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import com.yandex.app.service.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class HttpTaskServer {
    private static final int PORT = 8081;
    private static TaskManager manager;
    private static Gson gson;
    private HttpServer httpServer;

    public static void main(String[] args) {
        HttpTaskServer httpTaskServer = new HttpTaskServer();
        manager = Managers.getDefault(Path.of("src\\data\\memory.csv"));
        Scanner scanner = new Scanner(System.in);
        gson = new GsonBuilder()
                // LocalDateTime
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                })
                // Duration (единица — минуты)
                .registerTypeAdapter(Duration.class, new JsonSerializer<Duration>() {
                    @Override
                    public JsonElement serialize(Duration src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toMinutes());
                    }
                })
                .registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
                    @Override
                    public Duration deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return Duration.ofMinutes(json.getAsLong());
                    }
                })
                .setPrettyPrinting()
                .create();
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

    void startServer() {
        try {
            if (httpServer == null) {
                httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
                httpServer.createContext("/tasks", new TaskHandler(manager, gson));
                httpServer.createContext("/epics", new EpicTaskHandler(manager, gson));
                httpServer.createContext("/subtask", new SubTaskHandler(manager, gson));
                httpServer.createContext("/history", new HistoryHandler(manager, gson));
                httpServer.createContext("/prioritized", new PriorityHandler(manager, gson));
            }
            Thread serverThread = new Thread(() -> httpServer.start());
            serverThread.start();
            System.out.println("Сервер запущен на порту " + PORT);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    void stopServer() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
            System.out.println("Сервер остановлен");
        }
    }

}