package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.*;

import java.nio.file.Path;

class Main {
    static TaskManager taskManager;

    public static void main(String[] args) {
        taskManager = Managers.getDefault(Path.of("src\\data\\memory.csv"));

        // 1. Создание задач
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        EpicTask epic1 = new EpicTask("Эпик 1", "Описание эпика 1");
        taskManager.createEpicTask(epic1);
        SubTask sub1 = new SubTask("Подзадача 1", "Подробности 1", epic1.getId());
        SubTask sub2 = new SubTask("Подзадача 2", "Подробности 2", epic1.getId());
        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        EpicTask epic2 = new EpicTask("Эпик 2", "Описание эпика 2");
        taskManager.createEpicTask(epic2);
        SubTask sub3 = new SubTask("Подзадача 3", "Подробности 3", epic2.getId());
        taskManager.createSubTask(sub3);

        // 2. Просмотры и история
        System.out.println("\n== Просмотр задач и история после каждого вызова ==\n");

        taskManager.getTaskById(task1.getId());
        printHistory(taskManager);
        System.out.println();
        taskManager.getEpicTaskById(epic1.getId());
        printHistory(taskManager);
        System.out.println();
        taskManager.getSubTaskById(sub1.getId());
        printHistory(taskManager);
        System.out.println();
        taskManager.getTaskById(task2.getId());
        printHistory(taskManager);
        System.out.println();
        taskManager.getSubTaskById(sub2.getId());
        printHistory(taskManager);
        System.out.println();
        taskManager.getEpicTaskById(epic2.getId());
        printHistory(taskManager);
        System.out.println();
        taskManager.getSubTaskById(sub3.getId());
        printHistory(taskManager);
        System.out.println("\n==Перенос повторного запроса Таска в конец истории==\n");
        taskManager.getTaskById(task1.getId());
        printHistory(taskManager);
        System.out.println("\n==Удаление таска приводит к удалению его из истории==\n");
        taskManager.deleteTaskById(task1.getId());
        printHistory(taskManager);
        System.out.println("\n==Удаление всех эпиков удаляет субтаски и эпики еще и из истории==\n");
        taskManager.deleteAllEpicTasks();
        printHistory(taskManager);
    }

    private static void printHistory(TaskManager manager) {
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }

    private static void printTasks(InMemoryTaskManager manager) {

        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        for (EpicTask epic : manager.getAllEpicTasks()) {
            System.out.println(epic);
            for (SubTask subTask : manager.getSubtasksOfEpic(epic.getId())) {
                System.out.println(subTask);
            }
        }
        System.out.println();
    }
}
