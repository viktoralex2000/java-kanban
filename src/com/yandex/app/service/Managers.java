package com.yandex.app.service;

import java.nio.file.Path;

public class Managers {
    public static InMemoryTaskManager getDefault(Path path) {
        return new FileBackedTaskManager(path);
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static InMemoryTaskManager getInMemoryTaskManager() {
        return new InMemoryTaskManager();
    }
}
