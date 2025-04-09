package com.group12.taskscheduler.controllers;

import com.group12.taskscheduler.models.Task;
import com.group12.taskscheduler.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*") // Enable CORS for all origins
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        try {
            Task createdTask = taskService.createTask(task);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving tasks");
        }
    }

    @PostMapping("/by-name")
    public ResponseEntity<Task> getTaskByName(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task name is required");
        }
        try {
            Optional<Task> taskOptional = taskService.getTaskByName(name);
            if (taskOptional.isPresent()) {
                return ResponseEntity.ok(taskOptional.get());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with name: " + name);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving task by name");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            task.setId(id); // Ensure the ID is set
            Task updatedTask = taskService.updateTask(task.getId(), task);
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Task not found with id: " + id);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Task not found with id: " + id);
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> generateSchedule(@RequestBody Map<String, String> payload) {
        if (payload == null || !payload.containsKey("name") || payload.get("name").trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task name is required");
        }

        try {
            Optional<Task> taskOptional = taskService.getTaskByName(payload.get("name"));
            if (taskOptional.isPresent()) {
                Map<String, Object> response = taskService.generateSchedule();
                return ResponseEntity.ok(response);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with name: " + payload.get("name"));
            }
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error generating schedule");
        }
    }

    // Error handler for validation errors
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getReason());
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, ex.getStatusCode());
    }
}