package test;

import com.yandex.app.model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    // Проверка на равенство по айди у задачи (1)
    @Test
    void shouldReturnTrueWhenIdsEqual() {
        Task task1 = new Task("name1", "desc1");
        Task task2 = new Task("name2", "desc2");
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2);
    }
}
