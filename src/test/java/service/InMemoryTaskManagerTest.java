package test.java.service;

import com.yandex.app.model.*;
import com.yandex.app.service.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    InMemoryTaskManager manager;

    @BeforeEach
    void init() {
        manager = new InMemoryTaskManager();
    }

    // SubTask не может быть своим эпиком
    @Test
    void shouldHandleSubTaskWithSameIdAsEpicWithoutBreakingEpic() {
        LocalDateTime now = LocalDateTime.now();

        // Создаем эпик
        EpicTask epic = new EpicTask("Эпик", "Описание", now, Duration.ZERO);
        manager.createEpicTask(epic);

        // Создаем подзадачу с тем же ID, что и эпик
        SubTask subtask = new SubTask("Субтаск", "Описание", epic.getId(), now.plusMinutes(10), Duration.ofMinutes(20));
        subtask.setId(epic.getId()); // намеренно совпадает с ID эпика
        manager.createSubTask(subtask);

        // Проверяем, что эпик остался без изменений
        EpicTask epicAfter = manager.getEpicTaskById(epic.getId());
        assertEquals(epic.getName(), epicAfter.getName(), "Эпик не должен изменяться");
        assertEquals(TaskStatus.NEW, epicAfter.getStatus(), "Статус эпика должен остаться NEW");

        // Проверяем, что подзадача добавилась в менеджер
        assertTrue(manager.getAllSubTasks().contains(subtask), "Подзадача добавлена в менеджер");

        // Проверяем, что эпик не содержит ID подзадачи, которая совпадает с его ID
        assertFalse(epicAfter.getSubtaskIdList().contains(epic.getId()),
                "Эпик не должен содержать ID подзадачи с совпадающим ID");

        // Проверяем, что приоритетный список содержит подзадачу
        assertTrue(manager.getPrioritizedTasks().contains(subtask), "Подзадача должна быть в приоритетном списке");
    }


    // Добавление и получение задачи по id
    @Test
    void shouldAddAndReturnTaskById() {
        Task task = new Task("Таск", "Описание", LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createTask(task);
        Task stored = manager.getTaskById(task.getId());
        assertEquals(task, stored);
    }

    // Добавление и получение эпика и подзадачи по id
    @Test
    void shouldAddAndReturnEpicTaskAndSubTaskById() {
        EpicTask epic = new EpicTask("Эпик", "Описание", LocalDateTime.now(), Duration.ZERO);
        manager.createEpicTask(epic);
        SubTask sub = new SubTask("СубТаск", "Описание", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        manager.createSubTask(sub);
        assertEquals(epic, manager.getEpicTaskById(epic.getId()));
        assertEquals(sub, manager.getSubTaskById(sub.getId()));
    }

    // Проверка конфликтов вручную заданных и сгенерированных id
    @Test
    void shouldNotConflictWithManualAndGeneratedIds() {
        Task task1 = new Task("Таск1", "Описание1", LocalDateTime.now(), Duration.ofMinutes(10));
        task1.setId(100);
        manager.createTask(task1);
        Task task2 = new Task("Таск2", "Описание2", LocalDateTime.now().plusHours(1), Duration.ofMinutes(20));
        manager.createTask(task2);
        assertNotEquals(100, task2.getId());
    }

    // Проверка пересечения времени при создании
    @Test
    void shouldThrowIfTasksOverlap() {
        LocalDateTime start = LocalDateTime.now();
        Task task1 = new Task("T1", "Desc", start, Duration.ofMinutes(60));
        manager.createTask(task1);
        Task task2 = new Task("T2", "Desc", start.plusMinutes(30), Duration.ofMinutes(30));
        assertThrows(IllegalArgumentException.class, () -> manager.createTask(task2));
    }

    // Проверка пересечения времени при обновлении
    @Test
    void shouldThrowIfUpdatedTaskOverlaps() {
        LocalDateTime start = LocalDateTime.now();
        Task task1 = new Task("T1", "Desc", start, Duration.ofMinutes(60));
        Task task2 = new Task("T2", "Desc", start.plusHours(2), Duration.ofMinutes(30));
        manager.createTask(task1);
        manager.createTask(task2);
        task2.setStartTime(start.plusMinutes(30));
        assertThrows(IllegalArgumentException.class, () -> manager.updateTask(task2));
    }

    // Проверка правильного порядка в TreeSet (priorityTaskTree)
    @Test
    void shouldOrderTasksByStartTime() {
        Task task1 = new Task("T1", "Desc", LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        Task task2 = new Task("T2", "Desc", LocalDateTime.now(), Duration.ofMinutes(30));
        manager.createTask(task1);
        manager.createTask(task2);
        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(task2, prioritized.get(0));
        assertEquals(task1, prioritized.get(1));
    }

    // Проверка удаления эпика вместе с подзадачами
    @Test
    void deletingEpicAlsoDeletesSubtasks() {
        EpicTask epic = new EpicTask("Epic", "Desc", LocalDateTime.now(), Duration.ZERO);
        manager.createEpicTask(epic);
        SubTask sub = new SubTask("Sub", "Desc", epic.getId(), LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createSubTask(sub);
        manager.deleteEpicTaskById(epic.getId());
        assertTrue(manager.getAllSubTasks().isEmpty());
    }

    // История добавляется только если Task существует
    @Test
    void shouldAddInHistoryOnlyIfTaskExists() {
        Task task = new Task("Task", "Desc", LocalDateTime.now(), Duration.ofMinutes(15));
        manager.createTask(task);
        manager.getTaskById(task.getId());
        assertEquals(1, manager.getHistory().size());
        manager.getTaskById(task.getId() + 999);
        assertEquals(1, manager.getHistory().size());
    }

    // Проверка порядка истории (LIFO: последний просмотренный первый)
    @Test
    void shouldAddTasksInHistoryInCorrectOrder() {
        Task task = new Task("Task", "Desc", LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createTask(task);
        EpicTask epic = new EpicTask("Epic", "Desc", LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(30));
        manager.createEpicTask(epic);
        SubTask sub = new SubTask("Sub", "Desc", epic.getId(), LocalDateTime.now().plusMinutes(40), Duration.ofMinutes(15));
        manager.createSubTask(sub);
        manager.getTaskById(task.getId());
        manager.getEpicTaskById(epic.getId());
        manager.getSubTaskById(sub.getId());
        ArrayList<Task> history = manager.getHistory();
        assertEquals(3, history.size());
        assertEquals(sub, history.get(0));
        assertEquals(epic, history.get(1));
        assertEquals(task, history.get(2));
    }

    // Проверка удаления задач из истории
    @Test
    void shouldRemoveTasksFromHistoryWhenDeleted() {
        Task task = new Task("Task", "Desc", LocalDateTime.now(), Duration.ofMinutes(10));
        EpicTask epic = new EpicTask("Epic", "Desc", LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(20));
        manager.createTask(task);
        manager.createEpicTask(epic);
        SubTask sub1 = new SubTask("Sub1", "Desc", epic.getId(), LocalDateTime.now().plusMinutes(30), Duration.ofMinutes(10));
        SubTask sub2 = new SubTask("Sub2", "Desc", epic.getId(), LocalDateTime.now().plusMinutes(50), Duration.ofMinutes(10));
        manager.createSubTask(sub1);
        manager.createSubTask(sub2);
        manager.getTaskById(task.getId());
        manager.getEpicTaskById(epic.getId());
        manager.getSubTaskById(sub1.getId());
        manager.getSubTaskById(sub2.getId());
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
