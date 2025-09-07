package test.service;

import com.yandex.app.service.*;

import com.yandex.app.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    HistoryManager history;

    @BeforeEach
    void init() {
        history = new InMemoryHistoryManager();
    }

    // Добавление в историю должно сохранять исходное состояние задачи (9)
    @Test
    void shouldKeepOriginalTaskState() {
        Task task = new Task("Таск", "Описание");
        task.setId(42);
        history.add(42, task);
        task.setName("ТаскТаск");
        assertEquals(task.getName(), history.getHistory().getFirst().getName());
    }

    //Новый таск должен добавляться в начало истории и не нарушать её порядок
    @Test
    void shouldKeepOrderOfHistoryAndAddTaskAtStart() {
        Task task1 = new Task("Таск1", "Описание");
        task1.setId(1);
        history.add(1, task1);
        Task task2 = new Task("Таск2", "Описание");
        task2.setId(2);
        history.add(2, task2);
        Task task3 = new Task("Таск3", "Описание");
        task3.setId(3);
        history.add(3, task3);
        ArrayList<Task> historyListTest = history.getHistory();
        assertEquals(historyListTest.get(0), task3);
        assertEquals(historyListTest.get(1), task2);
        assertEquals(historyListTest.get(2), task1);
    }

}

