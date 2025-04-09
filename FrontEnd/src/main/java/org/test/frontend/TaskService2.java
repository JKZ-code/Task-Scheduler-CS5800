package org.test.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;

import java.io.IOException;
import java.net.http.HttpClient;

public class TaskService2 {
    private static final MediaType JSON = MediaType.get("application/json");
    private final ObjectMapper objectMapper;
    private static final String url = "http://localhost:8081/api/tasks";

    OkHttpClient client;

    public TaskService2(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        client = new OkHttpClient();
    }
    public TaskResponse post(Task task) throws IOException {
        String json = objectMapper.writeValueAsString(task);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("HTTP error code: " + response.code());
                System.err.println("Response body: " + response); // <-- log this
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, TaskResponse.class);
        }
    }
}
