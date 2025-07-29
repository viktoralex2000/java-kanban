package com.yandex.app.service;

public class Managers {
    public static InMemoryTaskManager getDefault(int maxHistorySize) {
        return new InMemoryTaskManager(maxHistorySize);//Замечание: теперь история создается в менеджере задач
    }
    public static InMemoryHistoryManager getDefaultHistory(int maxHistorySize) {
        return new InMemoryHistoryManager(maxHistorySize);
    }
}
