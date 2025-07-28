package test;

import com.yandex.app.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTaskTest {
    // Проверка равенста по id у наследника Task
    @Test
    void shouldReturnTrueWhenIdsEqual() {
        EpicTask epic1 = new EpicTask("Эпик1", "Описание1");
        EpicTask epic2 = new EpicTask("Эпик2", "Описание2");
        epic1.setId(55);
        epic2.setId(55);

        assertEquals(epic1, epic2);
    }
    // EpicTask не может быть подзадачей сам себе
    @Test
    void shouldNotBeSubTaskForSelf() {
        EpicTask epic = new EpicTask("Эпик", "Описание");
        epic.setId(77);
        epic.addSubTask(77);

        assertFalse(epic.getSubtaskIdList().contains(77));
    }
    // SubTask не может быть своим эпиком
    @Test
    void shouldNotBeEpicTaskForSelf() {
        SubTask subtask = new SubTask("СубТаск", "Описание", 3);
        subtask.setId(3);

        assertNotEquals(subtask.getId(), subtask.getEpicId());
    }
}