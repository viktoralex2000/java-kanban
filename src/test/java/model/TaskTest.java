package test.java.model;

import com.yandex.app.model.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    // Проверка равенства по id у задачи
    @Test
    void shouldReturnTrueWhenIdsEqual() {
        Task task1 = new Task("name1", "desc1", LocalDateTime.now(), Duration.ofMinutes(60));
        Task task2 = new Task("name2", "desc2", LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2);
    }
}
