package test;

import com.yandex.app.service.*;

import com.yandex.app.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    HistoryManager history;

    @BeforeEach
    void init() {
        history = new InMemoryHistoryManager(10);
    }
    // История не должна превышать 10 задач
    @Test
    void shouldFollowMaxSize() {
        for (int i = 0; i < 15; i++) {
            Task task = new Task("Таск" + i, "Описание");
            task.setId(i);
            history.updateHistory(task);
        }

        assertEquals(10, history.getHistory().size());
    }
    // История должна сохранять исходное состояние задачи
    @Test
    void shouldKeepOriginalTaskState() {
        Task task = new Task("Таск", "Описание");
        task.setId(42);
        history.updateHistory(task);
        task.setName("ТаскТаск");

        assertNotEquals(task.getName(), history.getHistory().getFirst().getName());
    }
}
