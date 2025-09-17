package test.java.model;

import com.yandex.app.model.SubTask;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    // Проверка равенства по id у подзадачи
    @Test
    void shouldReturnTrueWhenIdsAreEqual() {
        SubTask sub1 = new SubTask("a", "b", 2, LocalDateTime.now(), Duration.ofMinutes(30));
        SubTask sub2 = new SubTask("c", "d", 2, LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));
        sub1.setId(10);
        sub2.setId(10);

        assertEquals(sub1, sub2);
    }
}
