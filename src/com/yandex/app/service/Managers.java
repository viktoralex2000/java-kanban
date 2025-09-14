package com.yandex.app.service;

import java.nio.file.Path;

public class Managers {
    public static TaskManager getDefault(Path path) {
        return FileBackedTaskManager.loadFromFile(path);
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
