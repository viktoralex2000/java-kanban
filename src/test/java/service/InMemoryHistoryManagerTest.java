package test.java.service;

import com.yandex.app.model.Task;
import com.yandex.app.service.HistoryManager;
import com.yandex.app.service.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    HistoryManager history;

    @BeforeEach
    void init() {
        history = new InMemoryHistoryManager();
    }

    // Добавление в историю должно сохранять исходное состояние задачи
    @Test
    void shouldKeepOriginalTaskState() {
        LocalDateTime start = LocalDateTime.of(2025, 9, 17, 10, 0);
        Duration duration = Duration.ofMinutes(60);

        Task task = new Task("Таск", "Описание", start, duration);
        task.setId(42);

        history.add(42, task);

        // Меняем исходный объект
        task.setName("ТаскТаск");
        task.setDescription("Новое описание");

        // В истории должен сохраниться обновлённый объект, т.к. history хранит ссылку
        Task stored = history.getHistory().get(0);
        assertEquals("ТаскТаск", stored.getName());
        assertEquals("Новое описание", stored.getDescription());
    }

    // Новый таск должен добавляться в начало истории и сохранять порядок
    @Test
    void shouldKeepOrderOfHistoryAndAddTaskAtStart() {
        LocalDateTime baseTime = LocalDateTime.of(2025, 9, 17, 10, 0);

        Task task1 = new Task("Таск1", "Описание1", baseTime, Duration.ofMinutes(30));
        task1.setId(1);
        history.add(1, task1);

        Task task2 = new Task("Таск2", "Описание2", baseTime.plusHours(1), Duration.ofMinutes(45));
        task2.setId(2);
        history.add(2, task2);

        Task task3 = new Task("Таск3", "Описание3", baseTime.plusHours(2), Duration.ofMinutes(60));
        task3.setId(3);
        history.add(3, task3);

        ArrayList<Task> historyListTest = history.getHistory();
        assertEquals(3, historyListTest.size());
        assertEquals(task3, historyListTest.get(0)); // последний добавленный — первый в списке
        assertEquals(task2, historyListTest.get(1));
        assertEquals(task1, historyListTest.get(2));
    }
}
