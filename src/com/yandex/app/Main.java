package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.*;

class Main {
    static final int maxHistorySize = 10;
    static InMemoryTaskManager inMemoryManager;

    public static void main(String[] args) {
        //inMemoryHistoryManager = Managers.getDefaultHistory(maxHistorySize);
        inMemoryManager = Managers.getDefault(maxHistorySize);

        // 1. Создание задач
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        inMemoryManager.createTask(task1);
        inMemoryManager.createTask(task2);

        EpicTask epic1 = new EpicTask("Эпик 1", "Описание эпика 1");
        inMemoryManager.createEpicTask(epic1);
        SubTask sub1 = new SubTask("Подзадача 1", "Подробности 1", epic1.getId());
        SubTask sub2 = new SubTask("Подзадача 2", "Подробности 2", epic1.getId());
        inMemoryManager.createSubTask(sub1);
        inMemoryManager.createSubTask(sub2);

        EpicTask epic2 = new EpicTask("Эпик 2", "Описание эпика 2");
        inMemoryManager.createEpicTask(epic2);
        SubTask sub3 = new SubTask("Подзадача 3", "Подробности 3", epic2.getId());
        inMemoryManager.createSubTask(sub3);

        // 2. Просмотры и история
        System.out.println("== Просмотр задач и история после каждого вызова ==");

        inMemoryManager.getTaskById(task1.getId());
        printHistory(inMemoryManager);
        inMemoryManager.getEpicTaskById(epic1.getId());
        printHistory(inMemoryManager);
        inMemoryManager.getSubTaskById(sub1.getId());
        printHistory(inMemoryManager);
        inMemoryManager.getTaskById(task2.getId());
        printHistory(inMemoryManager);
        inMemoryManager.getSubTaskById(sub2.getId());
        printHistory(inMemoryManager);
        inMemoryManager.getEpicTaskById(epic2.getId());
        printHistory(inMemoryManager);
        inMemoryManager.getSubTaskById(sub3.getId());
        printHistory(inMemoryManager);

        // Проверка переполнения истории
        inMemoryManager.getTaskById(task1.getId());
        inMemoryManager.getSubTaskById(sub1.getId());
        inMemoryManager.getEpicTaskById(epic1.getId());
        inMemoryManager.getTaskById(task2.getId());
        printHistory(inMemoryManager);  // Здесь должно быть максимум 10 элементов
    }

    private static void printHistory(InMemoryTaskManager manager) {
        for (Task task : inMemoryManager.getHistory()) {
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
