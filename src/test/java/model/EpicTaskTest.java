package test.java.model;

import com.yandex.app.model.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTaskTest {

    // Проверка равенства по id у наследника Task
    @Test
    void shouldReturnTrueWhenIdsEqual() {
        EpicTask epic1 = new EpicTask("Эпик1", "Описание1", null, null);
        EpicTask epic2 = new EpicTask("Эпик2", "Описание2", null, null);
        epic1.setId(55);
        epic2.setId(55);

        assertEquals(epic1, epic2);
    }

    // EpicTask не может быть подзадачей сам себе
    @Test
    void shouldNotBeSubTaskForSelf() {
        EpicTask epic = new EpicTask("Эпик", "Описание", LocalDateTime.now(), Duration.ofMinutes(60));
        epic.setId(77);
        epic.addSubTask(77);

        assertFalse(epic.getSubtaskIdList().contains(77));
    }

}
