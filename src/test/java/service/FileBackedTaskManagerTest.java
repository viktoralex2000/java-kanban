package test.java.service;

import com.yandex.app.model.*;
import com.yandex.app.service.FileBackedTaskManager;
import com.yandex.app.service.Managers;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private Path testFile;

    @BeforeEach
    void setup() throws IOException {
        testFile = Files.createTempFile("tasks", ".csv");
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(testFile);
    }

    @Test
    void saveAndLoadFromFile_shouldRestoreAllTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

        Task task = new Task("Task1", "Description1", LocalDateTime.now(), Duration.ofMinutes(60));
        EpicTask epic = new EpicTask("Epic1", "EpicDescription1", null, null);
        manager.createTask(task);
        manager.createEpicTask(epic);
        SubTask subTask = new SubTask("Sub1", "SubDescription1", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));
        manager.createSubTask(subTask);

        FileBackedTaskManager loadedManager = (FileBackedTaskManager) Managers.getDefault(testFile);

        assertEquals(1, loadedManager.getAllTasks().size(), "Должна быть 1 обычная задача");
        assertEquals(1, loadedManager.getAllEpicTasks().size(), "Должен быть 1 эпик");
        assertEquals(1, loadedManager.getAllSubTasks().size(), "Должна быть 1 подзадача");

        Task loadedTask = loadedManager.getAllTasks().get(0);
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());

        EpicTask loadedEpic = loadedManager.getAllEpicTasks().get(0);
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());

        SubTask loadedSubTask = loadedManager.getAllSubTasks().get(0);
        assertEquals(subTask.getName(), loadedSubTask.getName());
        assertEquals(subTask.getDescription(), loadedSubTask.getDescription());
        assertEquals(subTask.getEpicId(), loadedSubTask.getEpicId());
        assertEquals(subTask.getStartTime(), loadedSubTask.getStartTime());
        assertEquals(subTask.getDuration(), loadedSubTask.getDuration());
    }

    @Test
    void save_shouldCreateFileWithContent() throws IOException {
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile);

        Task task = new Task("TaskTest", "DescTest", LocalDateTime.now(), Duration.ofMinutes(45));
        manager.createTask(task);

        assertTrue(Files.exists(testFile), "Файл должен существовать после save");

        List<String> lines = Files.readAllLines(testFile);
        assertTrue(lines.size() > 1, "Файл должен содержать заголовок и хотя бы одну задачу");

        // Проверяем заголовок CSV
        assertEquals("id,type,name,status,description,start_time,duration,epic", lines.get(0));

        // Проверяем содержимое первой задачи
        assertTrue(lines.get(1).contains("TaskTest"));
    }

}
