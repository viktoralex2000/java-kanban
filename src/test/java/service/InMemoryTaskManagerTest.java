package test.java.service;

import com.yandex.app.model.*;
import com.yandex.app.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    InMemoryTaskManager manager;

    @BeforeEach
    void init() {
        // Для unit-тестов используем чистый InMemoryTaskManager без файлов
        manager = new InMemoryTaskManager();
    }

    // SubTask не может быть своим эпиком
    @Test
    void shouldNotBeEpicTaskForSelf() {
        EpicTask epic = new EpicTask("Эпик", "Описание");
        manager.createEpicTask(epic);

        SubTask subtask = new SubTask("Субтаск", "Описание", epic.getId());
        // Пытаемся вручную установить id равным id эпика
        subtask.setId(epic.getId());
        manager.createSubTask(subtask);

        // Подтаск с id равным эпику не должен создаться
        assertFalse(epic.getSubtaskIdList().contains(epic.getId()));
    }

    // Добавление и получение задачи по id
    @Test
    void shouldAddAndReturnTaskById() {
        Task task = new Task("Таск", "Описание");
        manager.createTask(task);
        Task stored = manager.getTaskById(task.getId());
        assertEquals(task, stored);
    }

    // Добавление и получение эпика и подзадачи по id
    @Test
    void shouldAddAndReturnEpicTaskAndSubTaskById() {
        EpicTask epic = new EpicTask("Эпик", "Описание");
        manager.createEpicTask(epic);

        SubTask sub = new SubTask("СубТаск", "Описание", epic.getId());
        manager.createSubTask(sub);

        assertEquals(epic, manager.getEpicTaskById(epic.getId()));
        assertEquals(sub, manager.getSubTaskById(sub.getId()));
    }

    // Проверка конфликтов вручную заданных и сгенерированных id
    @Test
    void shouldNotConflictWithManualAndGeneratedIds() {
        Task task1 = new Task("Таск1", "Описание1");
        task1.setId(100);
        manager.createTask(task1);

        Task task2 = new Task("Таск2", "Описание2");
        manager.createTask(task2);

        assertNotEquals(100, task2.getId());
    }

    // Задача не должна изменяться после добавления
    @Test
    void shouldNotChangeTaskAfterAdding() {
        Task task = new Task("Таск", "Описание");
        manager.createTask(task);

        // Меняем оригинал
        task.setName("НовыйТаск");
        task.setDescription("НовыйОписание");
        task.setStatus(TaskStatus.DONE);

        Task stored = manager.getTaskById(task.getId());
        assertEquals("НовыйТаск", stored.getName());
        assertEquals("НовыйОписание", stored.getDescription());
        assertEquals(TaskStatus.DONE, stored.getStatus());
    }

    // Статус эпика должен быть NEW если все подзадачи новые
    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        EpicTask epic = new EpicTask("ЭпикТаск", "Описание");
        manager.createEpicTask(epic);

        SubTask sub1 = new SubTask("Субтаск1", "Описание1", epic.getId());
        SubTask sub2 = new SubTask("Субтаск2", "Описание2", epic.getId());

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        assertEquals(TaskStatus.NEW, manager.getEpicTaskById(epic.getId()).getStatus());
    }

    // Удаление эпика удаляет все его подзадачи
    @Test
    void deletingEpicAlsoDeletesSubtasks() {
        EpicTask epic = new EpicTask("Эпик", "Описание");
        manager.createEpicTask(epic);

        SubTask sub = new SubTask("Субтаск", "Описание", epic.getId());
        manager.createSubTask(sub);

        manager.deleteEpicTaskById(epic.getId());
        assertTrue(manager.getAllSubTasks().isEmpty());
    }

    // История добавляется только если Task существует
    @Test
    void shouldAddInHistoryOnlyIfTaskExists() {
        Task task = new Task("Таск", "Описание");
        manager.createTask(task);

        manager.getTaskById(task.getId());
        assertEquals(1, manager.getHistory().size());

        // Попытка получить несуществующий id не добавит ничего в историю
        manager.getTaskById(task.getId() + 999);
        assertEquals(1, manager.getHistory().size());
    }

    // Проверка порядка истории (LIFO: последний просмотренный первый)
    @Test
    void shouldAddTasksInHistoryInCorrectOrder() {
        Task task = new Task("Таск", "Описание");
        manager.createTask(task);

        EpicTask epic = new EpicTask("Эпик", "Описание");
        manager.createEpicTask(epic);

        SubTask sub = new SubTask("Субтаск", "Описание", epic.getId());
        manager.createSubTask(sub);

        // Просмотр задач
        manager.getTaskById(task.getId());
        manager.getEpicTaskById(epic.getId());
        manager.getSubTaskById(sub.getId());

        ArrayList<Task> history = manager.getHistory();
        assertEquals(3, history.size());
        assertEquals(sub, history.get(0));   // последний просмотренный
        assertEquals(epic, history.get(1));
        assertEquals(task, history.get(2));  // первый просмотренный
    }

    // Удаление задач из памяти также удаляет их из истории
    @Test
    void shouldRemoveTasksFromHistoryWhenDeleted() {
        Task task = new Task("Таск", "Описание");
        EpicTask epic = new EpicTask("Эпик", "Описание");

        manager.createTask(task);
        manager.createEpicTask(epic);

        SubTask sub1 = new SubTask("Субтаск1", "Описание", epic.getId());
        SubTask sub2 = new SubTask("Субтаск2", "Описание", epic.getId());

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        // Просмотр всех
        manager.getTaskById(task.getId());
        manager.getEpicTaskById(epic.getId());
        manager.getSubTaskById(sub1.getId());
        manager.getSubTaskById(sub2.getId());

        // Удаляем задачи
        manager.deleteTaskById(task.getId());
        manager.deleteSubTaskById(sub1.getId());
        manager.deleteEpicTaskById(epic.getId());

        ArrayList<Task> history = manager.getHistory();
        assertFalse(history.contains(task));
        assertFalse(history.contains(sub1));
        assertFalse(history.contains(sub2));
        assertFalse(history.contains(epic));
    }
}
