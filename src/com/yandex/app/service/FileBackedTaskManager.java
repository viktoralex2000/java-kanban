package com.yandex.app.service;

import com.yandex.app.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String TITLE = "id,type,name,status,description,start_time,duration,epic";
    private Path path;

    public FileBackedTaskManager(Path path) {
        super();
        this.path = path;
    }

    protected static FileBackedTaskManager loadFromFile(Path path) {
        FileBackedTaskManager manager = new FileBackedTaskManager(path);
        ArrayList<SubTask> subTaskList = new ArrayList<>();

        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            manager.enableRestoreMode();
            String line;
            if ((line = reader.readLine()) != null) {
                while ((line = reader.readLine()) != null) {
                    Task task = manager.fromString(line);
                    if (task.getType() == TaskTypes.TASK) {
                        manager.restoreTask(task);
                    } else if (task.getType() == TaskTypes.EPIC) {
                        manager.restoreEpicTask((EpicTask) task);
                    } else {
                        subTaskList.add((SubTask) task);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (SubTask subTask : subTaskList) {
            manager.restoreSubTask(subTask);
        }
        manager.disableRestoreMode();
        manager.recalcId();
        return manager;
    }

    private void restoreTask(Task task) {
        super.createTask(task);
    }

    private void restoreEpicTask(EpicTask epicTask) {
        super.createEpicTask((EpicTask) epicTask);
    }

    private void restoreSubTask(SubTask subTask) {
        super.createSubTask((SubTask) subTask);
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(TITLE);
            writer.newLine();
            for (Task task : getAllTasks()) {
                writer.write(task.toString());
                writer.newLine();
            }
            for (EpicTask epicTask : getAllEpicTasks()) {
                writer.write(epicTask.toString());
                writer.newLine();
            }
            for (SubTask subTask : getAllSubTasks()) {
                writer.write(subTask.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + path);
        }
    }

    private Task fromString(String line) {
        String[] taskParts = line.split(",");
        int id = Integer.parseInt(taskParts[0]);
        String name = taskParts[2];
        TaskStatus status = TaskStatus.valueOf(taskParts[3].trim());
        String description = taskParts[4];
        String startTimePart = taskParts[5];
        String durationPart = taskParts[6];

        LocalDateTime startTime = startTimePart.isEmpty() ? null : LocalDateTime.parse(startTimePart);
        Duration duration = durationPart.isEmpty() ? null : Duration.ofMinutes(Long.parseLong(durationPart));

        return switch (taskParts[1]) {
            case "TASK" -> {
                Task task = new Task(name, description, startTime, duration);
                task.setId(id);
                task.setStatus(status);
                yield task;
            }
            case "EPIC" -> {
                EpicTask epicTask = new EpicTask(name, description, startTime, duration);
                epicTask.setId(id);
                epicTask.setStatus(status);
                yield epicTask;
            }
            case "SUBTASK" -> {
                SubTask subTask = new SubTask(name, description, Integer.parseInt(taskParts[7]), startTime, duration);
                subTask.setId(id);
                subTask.setStatus(status);
                yield subTask;
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + taskParts[1]);
        };
    }

    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();

    }

    @Override
    public void createEpicTask(EpicTask epic) {
        super.createEpicTask(epic);
        save();
    }

    @Override
    public void updateEpicTask(EpicTask newEpic) {
        super.updateEpicTask(newEpic);
        save();
    }

    @Override
    public void deleteEpicTaskById(int id) {
        super.deleteEpicTaskById(id);
        save();
    }

    @Override
    public void deleteAllEpicTasks() {
        super.deleteAllEpicTasks();
        save();
    }

    @Override
    public void createSubTask(SubTask subtask) {
        super.createSubTask(subtask);
        save();
    }

    @Override
    public void updateSubTask(SubTask newSubTask) {
        super.updateSubTask(newSubTask);
        save();
    }

    @Override
    public void deleteSubTaskById(int id) {
        super.deleteSubTaskById(id);
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

}