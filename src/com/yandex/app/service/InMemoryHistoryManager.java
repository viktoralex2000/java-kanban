package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.ArrayDeque;

public class InMemoryHistoryManager implements HistoryManager {
    int maxHistorySize;

    public InMemoryHistoryManager(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    private ArrayDeque<Task> historyDeque = new ArrayDeque<>();

    @Override
    public ArrayDeque<Task> getHistory() {
        return historyDeque;
    }

    public void updateHistory(Task task) {
        if (historyDeque.size() < 10) {
            historyDeque.addFirst(task);
        } else {
            historyDeque.removeLast();
            historyDeque.addFirst(task);
        }
    }
}
