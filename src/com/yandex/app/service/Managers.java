package com.yandex.app.service;

public class Managers {
    public static InMemoryTaskManager getDefault(HistoryManager historyManager) {
        return new InMemoryTaskManager(historyManager);
    }
    public static InMemoryHistoryManager getDefaultHistory(int maxHistorySize) {
        return new InMemoryHistoryManager(maxHistorySize);
    }
}
