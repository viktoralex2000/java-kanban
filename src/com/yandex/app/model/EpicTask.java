package com.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EpicTask extends Task {
    private ArrayList<Integer> subtaskIdList;

    public EpicTask(String name, String description, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        subtaskIdList = new ArrayList<>();
    }

    @Override
    public TaskTypes getType() {
        return TaskTypes.EPIC;
    }

    public ArrayList<Integer> getSubtaskIdList() {
        return subtaskIdList;
    }

    public void addSubTask(int subtaskId) {
        if (subtaskId != getId() && !subtaskIdList.contains(subtaskId)) {
            subtaskIdList.add(subtaskId);
        }
    }

    public void removeSubTask(int subtaskId) {
        subtaskIdList.remove(Integer.valueOf(subtaskId));
    }

    public void clearEpic() {
        subtaskIdList.clear();
    }

}