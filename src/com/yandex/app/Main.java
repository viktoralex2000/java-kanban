package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.*;

import java.nio.file.Path;

class Main {
    static InMemoryTaskManager inMemoryTaskManager;

    public static void main(String[] args) {
        inMemoryTaskManager = Managers.getFileBackedTaskManager(Path.of("src\\data\\memory.csv"));

        // 1. Создание задач
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        inMemoryTaskManager.createTask(task1);
        inMemoryTaskManager.createTask(task2);

        EpicTask epic1 = new EpicTask("Эпик 1", "Описание эпика 1");
        inMemoryTaskManager.createEpicTask(epic1);
        SubTask sub1 = new SubTask("Подзадача 1", "Подробности 1", epic1.getId());
        SubTask sub2 = new SubTask("Подзадача 2", "Подробности 2", epic1.getId());
        inMemoryTaskManager.createSubTask(sub1);
        inMemoryTaskManager.createSubTask(sub2);

        EpicTask epic2 = new EpicTask("Эпик 2", "Описание эпика 2");
        inMemoryTaskManager.createEpicTask(epic2);
        SubTask sub3 = new SubTask("Подзадача 3", "Подробности 3", epic2.getId());
        inMemoryTaskManager.createSubTask(sub3);

        // 2. Просмотры и история
        System.out.println("\n== Просмотр задач и история после каждого вызова ==\n");

        inMemoryTaskManager.getTaskById(task1.getId());
        printHistory(inMemoryTaskManager);
        System.out.println();
        inMemoryTaskManager.getEpicTaskById(epic1.getId());
        printHistory(inMemoryTaskManager);
        System.out.println();
        inMemoryTaskManager.getSubTaskById(sub1.getId());
        printHistory(inMemoryTaskManager);
        System.out.println();
        inMemoryTaskManager.getTaskById(task2.getId());
        printHistory(inMemoryTaskManager);
        System.out.println();
        inMemoryTaskManager.getSubTaskById(sub2.getId());
        printHistory(inMemoryTaskManager);
        System.out.println();
        inMemoryTaskManager.getEpicTaskById(epic2.getId());
        printHistory(inMemoryTaskManager);
        System.out.println();
        inMemoryTaskManager.getSubTaskById(sub3.getId());
        printHistory(inMemoryTaskManager);
        System.out.println("\n==Перенос повторного запроса Таска в конец истории==\n");
        inMemoryTaskManager.getTaskById(task1.getId());
        printHistory(inMemoryTaskManager);
        System.out.println("\n==Удаление таска приводит к удалению его из истории==\n");
        inMemoryTaskManager.deleteTaskById(task1.getId());
        printHistory(inMemoryTaskManager);
        System.out.println("\n==Удаление всех эпиков удаляет субтаски и эпики еще и из истории==\n");
        inMemoryTaskManager.deleteAllEpicTasks();
        printHistory(inMemoryTaskManager);
    }

    private static void printHistory(InMemoryTaskManager manager) {
        for (Task task : inMemoryTaskManager.getHistory()) {
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
