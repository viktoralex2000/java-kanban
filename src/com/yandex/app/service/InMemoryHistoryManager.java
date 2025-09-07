package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private LinkedHashMapContainer<Integer, Task> historyContainer;

    public InMemoryHistoryManager() {
        historyContainer = new LinkedHashMapContainer<>();
    }

    @Override
    public ArrayList<Task> getHistory() {
        ArrayList<Task> list = historyContainer.values();
        Collections.reverse(list);
        return list;
    }

    @Override
    public void add(int id, Task task) {
        if (historyContainer.containsKey(id)) {
            historyContainer.remove(id);
        }
        historyContainer.put(id, task);
    }

    @Override
    public void remove(int id) {
        historyContainer.remove(id);
    }
}