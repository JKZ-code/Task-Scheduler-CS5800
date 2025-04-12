package org.test.frontend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TaskService {
    private static final String API_URL = "http://localhost:8080/api/tasks";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public TaskService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        httpClient = HttpClient.newBuilder().build();
    }

    public TaskResponse createTask(Task task) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(task);
        // System.out.println("Request JSON Body:\n" + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new IOException("Failed to create task: " + response.body());
        }

        return objectMapper.readValue(response.body(), TaskResponse.class);
    }

    public List<TaskResponse> getAllTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to get tasks: " + response.body());
        }

        return objectMapper.readValue(response.body(), new TypeReference<List<TaskResponse>>() {});
    }

    public TaskResponse updateTask(Task task, Long id) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to update task: " + response.body());
        }

        return objectMapper.readValue(response.body(), TaskResponse.class);
    }

    public void deleteTask(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/" + id))
                .header("Content-Type", "application/json")
                .DELETE().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 204) {
            throw new IOException("Failed to delete task: " + response.body());
        }
    }

    public List<String> getSchedule() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/schedule"))
                .header("Content-Type", "application/json")
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to schedule task: " + response.body());
        }

        return objectMapper.readValue(response.body(), new TypeReference<List<String>>() {});
    }
}
