package com.yandex.app.model;

import java.time.*;

public class SubTask extends Task {
    private int epicId;

    public SubTask(String name, String description, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskTypes getType() {
        return TaskTypes.SUBTASK;
    }

    @Override
    public String toString() {
        return super.toString() + "," + getEpicId();
    }
}