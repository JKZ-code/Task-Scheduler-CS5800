package org.test.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TaskService {
    private static final String API_URL = "http://localhost:8081/api/tasks";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public TaskService(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        httpClient = HttpClient.newBuilder().build();
    }

    public TaskResponse createTask(Task task) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(task);
        //System.out.println("Request JSON Body:\n" + requestBody);

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

}
