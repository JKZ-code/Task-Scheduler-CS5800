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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
        System.out.println("Received task: " + task);
        System.out.println("  Name: " + task.getName());
        System.out.println("  Weight: " + task.getWeight());
        System.out.println("  Due Date: " + task.getDueDate());
        System.out.println("  Estimated Duration: " + task.getEstimatedDuration());
        System.out.println("  Dependencies: " + task.getDependenciesSet());
        try {
            if (task.getDependenciesStr() != null && !task.getDependenciesStr().isEmpty()) {
                Set<Long> parsedDeps = Arrays.stream(task.getDependenciesStr().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
                task.setDependenciesSet(parsedDeps);
            }
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

    @GetMapping("/schedule")
    public ResponseEntity<List<String>> generateSchedule() {
        try {
            Map<String, Object> response = taskService.generateSchedule();
            if (response == null || !response.containsKey("schedule")) {
                throw new IllegalStateException("Schedule missing from response");
            }
            @SuppressWarnings("unchecked")
            List<Long> scheduledTaskIds = (List<Long>) response.get("schedule");
            List<Task> scheduledTasks = scheduledTaskIds.stream()
                .map(id -> taskService.getTaskById(id).orElse(null))
                .filter(task -> task != null)
                .collect(Collectors.toList());
            List<String> taskNames = scheduledTasks.stream()
                .map(Task::getName)
                .collect(Collectors.toList());
            return ResponseEntity.ok(taskNames);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            // Log the exception details
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error generating schedule: " + e.getMessage());
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