package com.group12.taskscheduler.services;

import com.group12.taskscheduler.models.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskService {
    /**
     * Creates a new task
     * @param task The task to create
     * @return The created task with generated ID
     * @throws IllegalArgumentException if the task data is invalid
     */
    Task createTask(Task task);

    /**
     * Retrieves all tasks in the system
     * @return List of all tasks
     */
    List<Task> getAllTasks();

    /**
     * Retrieves a specific task by ID
     * @param id The ID of the task to retrieve
     * @return Optional containing the task if found, empty otherwise
     */
    Optional<Task> getTaskById(Long id);

    /**
     * Updates an existing task
     * @param task The task with updated data
     * @return The updated task
     * @throws IllegalArgumentException if the task data is invalid
     * @throws RuntimeException if the task doesn't exist
     */
    Task updateTask(Long id, Task task);

    /**
     * Deletes a task by ID
     * @param id The ID of the task to delete
     * @throws RuntimeException if the task doesn't exist
     */
    void deleteTask(Long id);

    // /**
    //  * Generates an optimal schedule based on all tasks
    //  * @return List of task IDs in optimal execution order
    //  * @throws IllegalStateException if no valid schedule can be generated
    //  */
    // List<Long> generateSchedule();

    /**
     * Calculates the total weight of tasks in a schedule
     * @param taskIds List of task IDs in the schedule
     * @return Total weight of all tasks in the schedule
     */
    public int calculateTotalWeight(List<Long> taskIds, List<Task> allTasks);

    // Search operations - consolidated into a single method with parameters
    List<Task> searchTasks(String name, Integer weight, LocalDate startDate, LocalDate endDate);
    
    // Core scheduling functionality
    /**
     * Generates an optimal schedule based on task weights, deadlines, durations and dependencies
     * @return A map containing the scheduled tasks and the total weight achieved
     */
    Map<String, Object> generateSchedule();

    /**
     * Retrieves a specific task by its name
     * @param name The name of the task to retrieve
     * @return Optional containing the task if found, empty otherwise
     */
    Optional<Task> getTaskByName(String name);
}