package com.yandex.app.service;

import com.yandex.app.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    Path memoryPath;
    final String title = "id,type,name,status,description,epic";

    public FileBackedTaskManager(Path memoryPath) {
        super();
        this.memoryPath = memoryPath;
        loadFromFile();
    }

    private void loadFromFile() {
        try (var reader = Files.newBufferedReader(memoryPath, StandardCharsets.UTF_8)) {
            String line;
            if ((line = reader.readLine()) != null) {
                while ((line = reader.readLine()) != null) {
                    Task task = fromString(line);
                    super.putInMap(task);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (SubTask subTask : getAllSubTasks()) {
            EpicTask epic = getAllEpicTasks().get(subTask.getEpicId());
            if (epic != null) {
                epic.addSubTask(subTask.getId());
                updateEpicStatus(epic);
            }
        }
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(memoryPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(title);
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
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + memoryPath);
        }
    }

    private Task fromString(String line) {
        String[] taskParts = line.split(",");
        return switch (taskParts[1]) {
            case "TASK" -> {
                Task task = new Task(taskParts[2], taskParts[4]);
                task.setId(Integer.parseInt(taskParts[0]));
                task.setStatus(TaskStatus.valueOf(taskParts[3].trim()));
                yield task;
            }
            case "EPIC" -> {
                EpicTask epicTask = new EpicTask(taskParts[2], taskParts[4]);
                epicTask.setId(Integer.parseInt(taskParts[0]));
                epicTask.setStatus(TaskStatus.valueOf(taskParts[3].trim()));
                yield epicTask;
            }
            case "SUBTASK" -> {
                SubTask subTask = new SubTask(
                        taskParts[2], taskParts[4], Integer.parseInt(taskParts[5]));
                subTask.setId(Integer.parseInt(taskParts[0]));
                subTask.setStatus(TaskStatus.valueOf(taskParts[3].trim()));
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