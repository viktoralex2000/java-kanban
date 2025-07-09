import java.util.*;

class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        //   1. Создание материала для теста
        // Две задачи
        Task task1 = new Task("Купить продуктов", "Сходить в ближайший магазин за продуктами");
        Task task2 = new Task("Сделать уборку", "Провести полную уборку во всём доме с мытьём полов");
        manager.createTask(task1);
        manager.createTask(task2);

        // Эпик 1 с 2 подзадачами
        EpicTask epic1 = new EpicTask("Решить Фз4", "Сесть за компьютер и " +
                "решить Финальное задание 4 спринта");
        manager.createEpicTask(epic1);
        SubTask sub1 = new SubTask("Включить мозг", "Активировать внимательность и рассудительность", epic1.getId());
        SubTask sub2 = new SubTask("Написать код", "Изучив тз написать то что требуется в тз так," +
                " как ты это понял", epic1.getId());
        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        // Эпик 2 с 1 подзадачей
        EpicTask epic2 = new EpicTask("Дождаться ревью", "Просто подождать");
        manager.createEpicTask(epic2);
        SubTask sub3 = new SubTask("Надеяться на лучшее", "Надеяться на получение" +
                " максимально развернутого ответа", epic2.getId());
        manager.createSubTask(sub3);

        //   2. Печать
        printAll(manager);

        //   3. Изменение статусов и печать результатов
        // Одна задача
        Task newTask1 = new Task("Купить продуктов", "Сходить в ближайший магазин за продуктами");
        newTask1.setStatus(TaskStatus.DONE);
        newTask1.setId(task1.getId());
        manager.updateTask(newTask1);

        // Две подзадачи 1 эпика
        SubTask newSub1 = new SubTask("Включить мозг", "Активировать внимательность и рассудительность",
                epic1.getId());
        SubTask newSub2 = new SubTask("Написать код", "Изучив тз написать то что требуется в тз так," +
                " как ты это понял", epic1.getId());
        newSub1.setStatus(TaskStatus.DONE);
        newSub2.setStatus(TaskStatus.DONE);
        newSub1.setId(sub1.getId());
        newSub2.setId(sub2.getId());
        manager.updateSubTask(newSub1);
        manager.updateSubTask(newSub2);

        // Подзадача 2 эпика
        SubTask newSub3 = new SubTask("Надеяться на лучшее", "Надеяться на получение" +
                " максимально развернутого ответа", epic2.getId());
        newSub3.setStatus(TaskStatus.IN_PROGRESS);
        newSub3.setId(sub3.getId());
        manager.updateSubTask(newSub3);

        printAll(manager);

        //   5. Удаление одной задачи и одного эпика
        manager.deleteTaskById(task2.getId());
        manager.deleteEpicTaskById(epic1.getId());

        printAll(manager);
    }

    private static void printAll(TaskManager manager) {
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        for (EpicTask epic : manager.getAllEpicTasks()) {
            System.out.println(epic);
            for (int id : epic.getSubtaskIdList()) {
                System.out.println(manager.getSubTaskById(id));
            }
        }
        System.out.println();
    }

}
