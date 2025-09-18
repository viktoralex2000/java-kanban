package com.yandex.app;

import com.yandex.app.model.*;
import com.yandex.app.service.*;

class Main {
    static TaskManager taskManager;

    public static void main(String[] args) {
        //taskManager = Managers.getDefault(Path.of("src\\data\\memory.csv"));
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
