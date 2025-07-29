package test;

import com.yandex.app.service.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    // getDef возвращает готовый объект менеджера (5)
    @Test
    void shouldReturnInitializedTaskManager() {
        InMemoryTaskManager manager = Managers.getDefault(10);
        assertNotNull(manager);
    }
    // getDefHist возвращает инициализированный объект накопитель истории (5)
    @Test
    void shouldReturnInitializedHistoryManager() {
        InMemoryHistoryManager history = Managers.getDefaultHistory(10);
        assertNotNull(history);
    }
}
