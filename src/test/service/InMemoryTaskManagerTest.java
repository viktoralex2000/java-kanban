package test.service;

import com.yandex.app.service.*;
import com.yandex.app.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    InMemoryTaskManager manager;

    @BeforeEach
    void init() {
        manager = Managers.getDefault();
    }

    // SubTask не может быть своим эпиком
    @Test
    void shouldNotBeEpicTaskForSelf() {
        EpicTask epic = new EpicTask("Эпик", "Описание");
        manager.createEpicTask(epic);
        SubTask subtask = new SubTask("Субтаск", "Описание", epic.getId());
        subtask.setId(epic.getId());
        epic.addSubTask(subtask.getId());
        assertFalse(epic.getSubtaskIdList().contains(epic.getId()));
    }

    //Субтаск не может быть своим эпиком (4)
    @Test
    void managerShouldNotAllowSubtaskToBeItsOwnEpic() {
        EpicTask epic = new EpicTask("Эпик", "Описание");
        epic.setId(1);
        manager.createEpicTask(epic);
        SubTask sub = new SubTask("СубТаск", "Описание", 1);
        sub.setId(1);
        manager.createSubTask(sub);
        SubTask stored = manager.getSubTaskById(1);
        assertNull(stored, "Подзадача не должна ссылаться на себя как на эпик");
    }

    // Добавление и получение задачи по id (6)
    @Test
    void shouldAddAndReturnTaskById() {
        Task task1 = new Task("Таск", "Описание");
        manager.createTask(task1);

        Task task2 = manager.getTaskById(task1.getId());
        assertEquals(task1, task2);
    }

    // Добавление и получение эпика и подзадачи по id (6)
    @Test
    void shouldAddAndReturnEpicTaskAndSubTaskById() {
        EpicTask epic = new EpicTask("Эпик", "Описание");
        manager.createEpicTask(epic);
        SubTask sub = new SubTask("СубТаск", "Описание", epic.getId());
        manager.createSubTask(sub);

        assertEquals(epic, manager.getEpicTaskById(epic.getId()));
        assertEquals(sub, manager.getSubTaskById(sub.getId()));
    }

    // Проверка конфликтов вручную заданных и сгенерированных id (7)
    @Test
    void shouldNotConflictWithManualAndGenerateIds() {
        Task task = new Task("Таск1", "Описание1");
        task.setId(100);
        manager.createTask(task);

        Task autoIdTask = new Task("Таск2", "АвтоОписание2");
        manager.createTask(autoIdTask);

        assertNotEquals(100, autoIdTask.getId());
    }

    // Задача не должна изменяться после добавления (8)
    @Test
    void shouldNotChangeTaskAfterAdding() {
        Task task = new Task("Таск", "Описание");
        manager.createTask(task);
        task.setName("ТаскТаск");
        task.setDescription("ОпОписание");
        task.setStatus(TaskStatus.DONE);
        Task stored = manager.getTaskById(task.getId());
        assertEquals("ТаскТаск", stored.getName());
        assertEquals("ОпОписание", stored.getDescription());
        assertEquals(TaskStatus.DONE, stored.getStatus());
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

    // В случае если полученый Task по id от Map равен Null, добавление в историю не происходит
    @Test
    void shouldAddInHistoryIfTaskOfMapNotNull() {
        Task task = new Task("Таск", "Описание");
        manager.createTask(task);
        manager.getTaskById(task.getId());
        assertEquals(1, manager.getHistory().size());
        manager.getTaskById(task.getId() + 100);
        assertEquals(1, manager.getHistory().size());
    }

    //Должен добавлять таски, эпики и субтаски в историю при их просмотре с соблюдением порядка
    @Test
    void shouldAddTaskEpicSubInMemoryWithCorrectOrders() {
        Task task = new Task("Таск", "Описание");
        manager.createTask(task);
        EpicTask epic = new EpicTask("ЭпикТаск", "Описание");
        manager.createEpicTask(epic);
        SubTask sub = new SubTask("Субтаск", "Описание", epic.getId());
        manager.createSubTask(sub);
        manager.getTaskById(task.getId());
        manager.getEpicTaskById(epic.getId());
        manager.getSubTaskById(sub.getId());
        ArrayList<Task> testListHistory = manager.getHistory();
        assertEquals(testListHistory.size(), 3);
        assertEquals(task, testListHistory.get(2));
        assertEquals(epic, testListHistory.get(1));
        assertEquals(sub, testListHistory.get(0));
    }

    //Должен удалять таск, субтаск и эпиктаск из истории когда те удаляются из памяти
    @Test
    void shouldDeleteTaskEpicSubFromHistoryWithDeleteFromMemory() {
        Task task = new Task("Таск", "Описание");
        manager.createTask(task);
        EpicTask epic = new EpicTask("ЭпикТаск", "Описание");
        manager.createEpicTask(epic);
        SubTask sub1 = new SubTask("Субтаск1", "Описание", epic.getId());
        manager.createSubTask(sub1);
        SubTask sub2 = new SubTask("Субтаск2", "Описание", epic.getId());
        manager.createSubTask(sub2);
        manager.getTaskById(task.getId());
        manager.getEpicTaskById(epic.getId());
        manager.getSubTaskById(sub1.getId());
        manager.getSubTaskById(sub2.getId());

        manager.deleteTaskById(task.getId());//Удалится ли история о таске
        assertFalse(manager.getHistory().contains(task));
        manager.deleteSubTaskById(sub1.getId());//Удалится ли история о субтаске
        assertFalse(manager.getHistory().contains(sub1));
        manager.deleteEpicTaskById(epic.getId());//Удалится ли история о эпике и о его субтаске
        assertFalse(manager.getHistory().contains(epic));
        assertFalse(manager.getHistory().contains(sub2));
        //Дополнительная проверка методов на удаление всех тасков
        manager.createTask(task);
        manager.createEpicTask(epic);
        manager.createSubTask(sub2);
        manager.getTaskById(task.getId());
        manager.getEpicTaskById(epic.getId());
        manager.getSubTaskById(sub2.getId());
        manager.deleteAllTasks();
        assertFalse(manager.getHistory().contains(task));
        manager.deleteAllEpicTasks();
        assertFalse(manager.getHistory().contains(epic));
        assertFalse(manager.getHistory().contains(sub2));

        manager.createSubTask(sub1);
        manager.getSubTaskById(sub1.getId());
        manager.deleteAllSubTasks();
        assertFalse(manager.getHistory().contains(sub1));
    }
}
