package test.java.server;

import com.google.gson.Gson;
import com.yandex.app.model.*;
import com.yandex.app.server.*;
import com.yandex.app.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    static TaskManager manager;
    static HttpTaskServer httpTaskServer;
    static Gson gson;
    static HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        httpTaskServer = new HttpTaskServer(manager);
        gson = httpTaskServer.getGson();
        manager.deleteAllTasks();
        manager.deleteAllSubTasks();
        manager.deleteAllEpicTasks();
        httpTaskServer.startServer();
    }

    @AfterEach
    void tearDown() {
        httpTaskServer.stopServer();
    }

    @Test
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Task", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8081/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Должен вернуться 201 Created");

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Task", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        manager.createTask(task);
        int id = task.getId();
        Task task2 = new Task("Task2", "desc", LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        task2.setId(id);
        String taskJson = gson.toJson(task2);

        URI url = URI.create("http://localhost:8081/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Должен вернуться 201 Created");

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        manager.createTask(task);
        int id = task.getId();

        URI url = URI.create("http://localhost:8081/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Должен вернуться 200 OK");
        Task returnedTask = gson.fromJson(response.body(), Task.class);

        assertNotNull(returnedTask, "Задача не возвращается");
        assertEquals(id, returnedTask.getId(), "Некорректный id задачи");
        assertEquals("Task", returnedTask.getName(), "Некорректное имя задачи");
    }

    @Test
    void testGetAllTasks() throws IOException, InterruptedException {
        Task task = new Task("Task", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task("Task2", "desc", LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        manager.createTask(task);
        manager.createTask(task2);
        int id = task.getId();
        int id2 = task2.getId();

        URI url = URI.create("http://localhost:8081/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Должен вернуться 200 OK");
        Task[] returnedTask = gson.fromJson(response.body(), Task[].class);

        assertNotNull(returnedTask, "Задачи не возвращаются");
        assertEquals(2, returnedTask.length, "Некорректное количество задач");
        assertEquals(id, returnedTask[0].getId(), "Некорректный id задачи");
        assertEquals(id2, returnedTask[1].getId(), "Некорректный id задачи");
        assertEquals("Task", returnedTask[0].getName(), "Некорректное имя задачи");
        assertEquals("Task2", returnedTask[1].getName(), "Некорректное имя задачи");
    }

    @Test
    void testDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task("Task2", "desc", LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        manager.createTask(task);
        manager.createTask(task2);
        int id = task.getId();
        int id2 = task2.getId();

        URI url = URI.create("http://localhost:8081/tasks/" + id2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Должен вернуться 200 OK");

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Список задач не должен быть null");
        assertEquals(1, tasksFromManager.size(), "Не корректное количество задач");
        assertEquals(id, tasksFromManager.get(0).getId(), "Удалена не та задача");
    }

    @Test
    void testDeleteAllTasks() throws IOException, InterruptedException {
        Task task = new Task("Task", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task("Task2", "desc", LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        manager.createTask(task);
        manager.createTask(task2);

        URI url = URI.create("http://localhost:8081/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Должен вернуться 200 OK");

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Список задач не должен быть null");
        assertTrue(tasksFromManager.isEmpty(), "Все задачи должны быть удалены");
    }


    @Test
    void testCreateEpicTask() throws IOException, InterruptedException {
        EpicTask epicTask = new EpicTask("EpicTask", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(epicTask);

        URI url = URI.create("http://localhost:8081/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Должен вернуться 201 Created");
        List<EpicTask> tasksFromManager = manager.getAllEpicTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("EpicTask", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    void testCreateSubTask() throws IOException, InterruptedException {
        EpicTask epicTask = new EpicTask("EpicTask", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        manager.createEpicTask(epicTask);
        SubTask subTask = new SubTask("SubTask", "desc", epicTask.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        String taskJson = gson.toJson(subTask);

        URI url = URI.create("http://localhost:8081/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Должен вернуться 201 Created");
        List<SubTask> tasksFromManager = manager.getAllSubTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("SubTask", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    void testGetHistory() throws IOException, InterruptedException {
        Task task = new Task("Task", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task("Task2", "desc", LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        manager.createTask(task);
        manager.createTask(task2);
        int id = task.getId();
        int id2 = task2.getId();
        manager.getTaskById(id);
        manager.getTaskById(id2);

        URI url = URI.create("http://localhost:8081/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Должен вернуться 200 OK");
        Task[] returnedTask = gson.fromJson(response.body(), Task[].class);

        assertNotNull(returnedTask, "Задачи не возвращаются");
        assertEquals(2, returnedTask.length, "Некорректное количество задач");
        assertEquals(id, returnedTask[1].getId(), "Некорректный id задачи");
        assertEquals(id2, returnedTask[0].getId(), "Некорректный id задачи");
        assertEquals("Task", returnedTask[1].getName(), "Некорректное имя задачи");
        assertEquals("Task2", returnedTask[0].getName(), "Некорректное имя задачи");
    }

    @Test
    void testGetPriority() throws IOException, InterruptedException {
        Task task = new Task("Task", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task("Task2", "desc", LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        manager.createTask(task);
        manager.createTask(task2);
        int id = task.getId();
        int id2 = task2.getId();

        URI url = URI.create("http://localhost:8081/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Должен вернуться 200 OK");
        Task[] returnedTask = gson.fromJson(response.body(), Task[].class);

        assertNotNull(returnedTask, "Задачи не возвращаются");
        assertEquals(2, returnedTask.length, "Некорректное количество задач");
        assertEquals(id, returnedTask[0].getId(), "Некорректный id задачи");
        assertEquals(id2, returnedTask[1].getId(), "Некорректный id задачи");
        assertEquals("Task", returnedTask[0].getName(), "Некорректное имя задачи");
        assertEquals("Task2", returnedTask[1].getName(), "Некорректное имя задачи");
    }

    @Test
    void testGetSubTasksOfEpic() throws IOException, InterruptedException {
        EpicTask epicTask = new EpicTask("EpicTask", "desc", LocalDateTime.now(), Duration.ofMinutes(5));
        manager.createEpicTask(epicTask);
        int epicId = epicTask.getId();
        SubTask subTask = new SubTask("SubTask", "desc", epicTask.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        manager.createSubTask(subTask);

        URI url = URI.create("http://localhost:8081/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Должен вернуться 200 OK");
        SubTask[] returnedTask = gson.fromJson(response.body(), SubTask[].class);

        assertNotNull(returnedTask, "Задачи не возвращаются");
        assertEquals(1, returnedTask.length, "Некорректное количество задач");
        assertEquals(subTask.getId(), returnedTask[0].getId(), "Некорректный id задачи");
        assertEquals(subTask.getName(), returnedTask[0].getName(), "Некорректное имя задачи");
    }
}
