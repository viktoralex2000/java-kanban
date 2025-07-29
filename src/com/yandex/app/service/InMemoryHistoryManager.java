package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.ArrayDeque;

public class InMemoryHistoryManager implements HistoryManager {
    private int maxHistorySize;//Замечание: модификатор доступа private
    private ArrayDeque<Task> historyDeque;//Замечание: все переменные в начале

    public InMemoryHistoryManager(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
        historyDeque = new ArrayDeque<>(maxHistorySize);
    }

    @Override
    public ArrayDeque<Task> getHistory() {
        return new ArrayDeque<>(historyDeque);//Замечание: возврат копии
    }

    public void updateHistory(Task task) {
        if (historyDeque.size() < maxHistorySize) {//Замечание: использование maxHistorySize
            historyDeque.addFirst(task);
        } else {
            historyDeque.removeLast();
            historyDeque.addFirst(task);
        }
    }
}
