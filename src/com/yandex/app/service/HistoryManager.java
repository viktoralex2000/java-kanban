package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.ArrayDeque;

public interface HistoryManager {
    void updateHistory(Task task);

    ArrayDeque<Task> getHistory();
}
