package com.group12.taskscheduler.services.impl;

import com.group12.taskscheduler.models.Task;
import com.group12.taskscheduler.repositories.TaskRepository;
import com.group12.taskscheduler.services.TaskService;
import com.group12.taskscheduler.services.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final SchedulerService schedulerService;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, SchedulerService schedulerService) {
        this.taskRepository = taskRepository;
        this.schedulerService = schedulerService;
    }

    // region Basic CRUD Operations
    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    public Task createTask(Task task) {
        if (task.getDependenciesSet() == null) {
            task.setDependenciesSet(new HashSet<>());
        }
        System.out.println("Creating task: " + task.getName() + 
                ", Weight: " + task.getWeight() + 
                ", Due Date: " + task.getDueDate() + 
                ", Duration: " + task.getEstimatedDuration() + 
                ", Dependencies: " + task.getDependenciesSet());
    
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(Long id, Task task) {
        if (taskRepository.existsById(id)) {
            task.setId(id);
            return taskRepository.save(task);
        }
        return null; // Task not found
    }

    @Override
    public void deleteTask(Long id) {
        // First, find all tasks that depend on this task
        List<Task> allTasks = getAllTasks();
        List<Task> dependentTasks = allTasks.stream()
            .filter(task -> task.getDependenciesSet() != null && task.getDependenciesSet().contains(id))
            .collect(Collectors.toList());
        
        // Update all dependent tasks to remove this task from dependencies
        for (Task dependentTask : dependentTasks) {
            dependentTask.getDependenciesSet().remove(id);
            
            // Update the dependencies string
            String updatedDependenciesStr = dependentTask.getDependenciesSet().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            dependentTask.setDependenciesStr(updatedDependenciesStr);
            
            // Save the updated task
            taskRepository.save(dependentTask);
        }
        
        // Now delete the task
        taskRepository.deleteById(id);
    }
    // endregion

    // region Search Operations
    @Override
    public List<Task> searchTasks(String name, Integer weight, LocalDate startDate, LocalDate endDate) {
        List<Task> result = new ArrayList<>(taskRepository.findAll());

        // Apply filters if provided
        if (name != null && !name.isEmpty()) {
            result = result.stream()
                    .filter(task -> task.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (weight != null) {
            result = result.stream()
                    .filter(task -> task.getWeight() == weight)
                    .collect(Collectors.toList());
        }

        if (startDate != null) {
            result = result.stream()
                    .filter(task -> task.getDueDate().isEqual(startDate) || task.getDueDate().isAfter(startDate))
                    .collect(Collectors.toList());
        }

        if (endDate != null) {
            result = result.stream()
                    .filter(task -> task.getDueDate().isEqual(endDate) || task.getDueDate().isBefore(endDate))
                    .collect(Collectors.toList());
        }

        return result;
    }

    @Override
    public Optional<Task> getTaskByName(String name) {
        List<Task> tasks = taskRepository.findByNameContainingIgnoreCase(name);
        if (!tasks.isEmpty()) {
            return Optional.of(tasks.get(0)); // Return the first matching task
        } else {
            return Optional.empty();
        }
    }
    // endregion

    // region Schedule Generation
    @Override
    public Map<String, Object> generateSchedule() {
        try {
            List<Task> allTasks = getAllTasks();
            System.out.println("Found " + allTasks.size() + " tasks in the database");
            
            // Return empty schedule if no tasks exist
            if (allTasks.isEmpty()) {
                System.out.println("No tasks found in the database, returning empty schedule");
                Map<String, Object> result = new HashMap<>();
                result.put("schedule", new ArrayList<>());
                result.put("totalWeight", 0);
                return result;
            }
            
            // Use SchedulerService to generate schedule
            System.out.println("Using SchedulerService to generate schedule");
            List<Task> scheduledTasks = schedulerService.scheduleTasks(allTasks);
            System.out.println("SchedulerService returned " + scheduledTasks.size() + " tasks");
            
            // Extract task IDs and calculate total weight
            List<Long> scheduledTaskIds = scheduledTasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());
            System.out.println("Extracted " + scheduledTaskIds.size() + " task IDs");
            
            int totalWeight = calculateTotalWeight(scheduledTaskIds, allTasks);
            System.out.println("Total weight: " + totalWeight);
            
            // Create and return the result
        Map<String, Object> result = new HashMap<>();
            result.put("schedule", scheduledTaskIds);
        result.put("totalWeight", totalWeight);

        return result;
        } catch (Exception e) {
            System.err.println("Error in generateSchedule: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int calculateTotalWeight(List<Long> taskIds, List<Task> allTasks) {
        // Create a map for faster lookups
        Map<Long, Task> taskMap = allTasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));

        // Sum up the weights
        int totalWeight = 0;
        for (Long id : taskIds) {
            Task task = taskMap.get(id);
            if (task != null) {
                totalWeight += task.getWeight();
            }
        }

        return totalWeight;
    }
    // endregion
}
