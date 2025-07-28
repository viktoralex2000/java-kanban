package test;

import com.yandex.app.service.*;
import com.yandex.app.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    InMemoryTaskManager manager;

    @BeforeEach
    void init() {
        manager = Managers.getDefault(new InMemoryHistoryManager(10));
    }
    // Добавление и получение задачи по id
    @Test
    void shouldAddAndReturnTaskById() {
        Task task1 = new Task("Таск", "Описание");
        manager.createTask(task1);

        Task task2 = manager.getTaskById(task1.getId());
        assertEquals(task1, task2);
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
    // Проверка конфликтов вручную заданных и авто id
    @Test
    void shouldNotConflictWithManualAndGenerateIds() {
        Task task = new Task("Таск1", "Описание1");
        task.setId(100);
        manager.createTask(task);

        Task autoIdTask = new Task("Таск2", "АвтоОписание2");
        manager.createTask(autoIdTask);

        assertNotEquals(100, autoIdTask.getId());
    }
    // Задача не должна изменяться после добавления
    @Test
    void shouldNotChangeTaskAfterAdding() {
        Task task = new Task("Таск", "Описание");
        manager.createTask(task);

        task.setName("ТаскТаск");
        task.setDescription("ОпОписание");
        task.setStatus(TaskStatus.DONE);

        Task stored = manager.getTaskById(task.getId());

        assertEquals("Таск", stored.getName());
        assertEquals("Таск", stored.getDescription());
        assertEquals(TaskStatus.NEW, stored.getStatus());
    }
    // Корректное обновление статуса эпика
    @Test
    void epicTaskStatusShouldBeNewWhenAllSubTasksNew() {
        EpicTask epic = new EpicTask("ЭпикТаск", "Описание");
        manager.createEpicTask(epic);

        SubTask sub1 = new SubTask("Субтаск1", "Описание1", epic.getId());
        SubTask sub2 = new SubTask("Субтаск2", "Описание2", epic.getId());
        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        assertEquals(TaskStatus.NEW, manager.getEpicTaskById(epic.getId()).getStatus());
    }
    // Удаление эпика удаляет подзадачи
    @Test
    void deletingEpicAlsoDeletesSubtasks() {
        EpicTask epic = new EpicTask("ЭпикТаск", "Описание");
        manager.createEpicTask(epic);
        SubTask sub = new SubTask("Субтаск", "Описание", epic.getId());
        manager.createSubTask(sub);

        manager.deleteEpicTaskById(epic.getId());
        assertTrue(manager.getAllSubTasks().isEmpty());
    }
}
