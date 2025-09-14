
package test.java.service;

import com.yandex.app.service.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldReturnInitializedTaskManager() throws IOException {
        Path tempFile = Files.createTempFile("tasks", ".csv");
        tempFile.toFile().deleteOnExit();

        TaskManager manager = Managers.getDefault(tempFile);
        assertNotNull(manager);
        assertTrue(manager instanceof FileBackedTaskManager);
    }

    @Test
    void shouldReturnInitializedHistoryManager() {
        InMemoryHistoryManager history = Managers.getDefaultHistory();
        assertNotNull(history);
    }
}
