package test.java.service;

import com.yandex.app.model.*;
import com.yandex.app.service.FileBackedTaskManager;
import org.junit.jupiter.api.*;

import java.nio.file.*;
import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private Path tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setup() throws IOException {
        tempFile = Files.createTempFile("testMemory", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void save_shouldWriteAllTasksToFile() throws IOException {
        Task task = new Task("Task1", "Description1");
        EpicTask epic = new EpicTask("Epic1", "EpicDescription");
        manager.createTask(task);
        manager.createEpicTask(epic);
        SubTask sub = new SubTask("Sub1", "SubDescription", epic.getId());
        manager.createSubTask(sub);

        manager.save();

        List<String> lines = Files.readAllLines(tempFile);

        assertEquals("id,type,name,status,description,epic", lines.get(0));
        assertTrue(lines.contains(task.toString()));
        assertTrue(lines.contains(epic.toString()));
        assertTrue(lines.contains(sub.toString()));
    }

    @Test
    void constructor_shouldLoadTasksFromFile() throws IOException {
        String csvContent = "id,type,name,status,description,epic\n" +
                "1,TASK,Task1,NEW,Description1,\n" +
                "2,EPIC,Epic1,NEW,EpicDescription,\n" +
                "3,SUBTASK,Sub1,NEW,SubDescription,2\n";
        Files.writeString(tempFile, csvContent);

        manager = new FileBackedTaskManager(tempFile);

        Task task = manager.getTaskById(1);
        EpicTask epic = (EpicTask) manager.getEpicTaskById(2);
        SubTask sub = manager.getSubTaskById(3);

        assertNotNull(task);
        assertNotNull(epic);
        assertNotNull(sub);

        assertEquals("Task1", task.getName());
        assertEquals("Epic1", epic.getName());
        assertEquals("Sub1", sub.getName());
        assertEquals(epic.getId(), sub.getEpicId());
    }

    @Test
    void constructor_shouldRestoreEpicSubtaskLinksAndStatus() throws IOException {
        String csvContent = "id,type,name,status,description,epic\n" +
                "1,EPIC,Epic1,NEW,EpicDescription,\n" +
                "2,SUBTASK,Sub1,NEW,SubDescription,1\n" +
                "3,SUBTASK,Sub2,DONE,AnotherSub,1\n";
        Files.writeString(tempFile, csvContent);

        manager = new FileBackedTaskManager(tempFile);

        EpicTask epic = (EpicTask) manager.getEpicTaskById(1);
        List<SubTask> subtasks = manager.getSubtasksOfEpic(epic.getId());

        assertEquals(2, subtasks.size());
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals("Sub1")));
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals("Sub2")));
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void saveTaskInFile_shouldAppendNewTaskWithoutOverwriting() throws IOException {
        Task task1 = new Task("Task1", "Description1");
        manager.createTask(task1);
        manager.save();

        Task task2 = new Task("Task2", "Description2");
        manager.createTask(task2);

        List<String> lines = Files.readAllLines(tempFile);
        assertTrue(lines.contains(task1.toString()));
        assertTrue(lines.contains(task2.toString()));
        assertEquals(3, lines.size()); // Заголовок + 2 задачи
    }
}