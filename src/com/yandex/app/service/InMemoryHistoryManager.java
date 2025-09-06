package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private LinkedHashMap<Integer,Task> historyLinkedMap;
    public InMemoryHistoryManager() {
        historyLinkedMap = new LinkedHashMap<>(16, 0.75f, true);
    }

    @Override
    public ArrayList<Task> getHistory() {
        ArrayList<Task> historyList = new ArrayList<>(historyLinkedMap.values());
        Collections.reverse(historyList);
        return historyList;
    }

    @Override
    public void add(int id, Task task) {
        historyLinkedMap.put(id, task);
    }

    @Override
    public void remove(int id) {
        historyLinkedMap.remove(id);
    }
}