package com.yandex.app.service;

import com.yandex.app.model.Task;
import java.util.ArrayList;

public interface HistoryManager {
    void add(int id, Task task);
    void remove(int id);
    ArrayList<Task> getHistory();
}