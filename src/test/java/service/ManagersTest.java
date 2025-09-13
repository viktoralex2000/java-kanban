package test.java.service;

import com.yandex.app.service.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    // getDef возвращает готовый объект менеджера (5)
    @Test
    void shouldReturnInitializedTaskManager() {
        InMemoryTaskManager manager = Managers.getDefault(Path.of("src\\data\\memory.csv"));
        assertNotNull(manager);
    }

    // getDefHist возвращает инициализированный объект накопитель истории (5)
    @Test
    void shouldReturnInitializedHistoryManager() {
        InMemoryHistoryManager history = Managers.getDefaultHistory();
        assertNotNull(history);
    }
}
