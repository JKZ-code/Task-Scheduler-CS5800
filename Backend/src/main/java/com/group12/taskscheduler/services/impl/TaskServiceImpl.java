package com.group12.taskscheduler.services.impl;

import com.group12.taskscheduler.models.Task;
import com.group12.taskscheduler.repositories.TaskRepository;
import com.group12.taskscheduler.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    //region Basic CRUD Operations
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
        taskRepository.deleteById(id);
    }
    //endregion

    //region Search Operations
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
    
    // Helper search methods - used internally
    private List<Task> getTasksByWeight(int weight) {
        return taskRepository.findByWeight(weight);
    }

    private List<Task> getTasksByDueDate(LocalDate dueDate) {
        return taskRepository.findByDueDate(dueDate);
    }

    private List<Task> getTasksDueBefore(LocalDate date) {
        return taskRepository.findByDueDateBefore(date);
    }

    private List<Task> getTasksDueAfter(LocalDate date) {
        return taskRepository.findByDueDateAfter(date);
    }

    private List<Task> searchTasksByName(String name) {
        return taskRepository.findByNameContainingIgnoreCase(name);
    }
    //endregion

    //region Schedule Generation
    @Override
    public Map<String, Object> generateSchedule() {
        // Step 1: Get all tasks
        List<Task> allTasks = taskRepository.findAll();
        System.out.println("Step 1: Found " + allTasks.size() + " tasks");
        
        // Ensure all dependencies are properly parsed
        allTasks.forEach(Task::parseDependencies);
        
        // Step 2: Perform topological sort
        List<Task> topologicallySortedTasks = topologicalSort(allTasks);
        System.out.println("Step 2: Topological sort produced " + topologicallySortedTasks.size() + " tasks");
        
        // Step 3: Compute earliest start times
        List<Task> tasksWithTimes = computeEarliestStartTimes(topologicallySortedTasks);
        System.out.println("Step 3: Earliest start times computed for " + tasksWithTimes.size() + " tasks");
        
        // Step 4: Filter out invalid tasks (end time > deadline)
        List<Task> validTasks = filterValidTasks(tasksWithTimes);
        System.out.println("Step 4: Filtered to " + validTasks.size() + " valid tasks");
        
        // Step 5: Apply Weighted Interval Scheduling with DP
        List<Long> scheduledTasks = weightedIntervalSchedulingDP(validTasks);
        System.out.println("Step 5: Weighted interval scheduling produced " + scheduledTasks.size() + " tasks");
        
        // Step 6: Ensure dependencies are included and order by dependencies
        List<Long> finalSchedule = ensureDependenciesIncluded(scheduledTasks, validTasks);
        System.out.println("Step 6: Final schedule with dependencies: " + finalSchedule.size() + " tasks");
        
        // Calculate total weight of scheduled tasks
        int totalWeight = calculateTotalWeight(finalSchedule, allTasks);
        
        // Prepare and return the result
        Map<String, Object> result = new HashMap<>();
        result.put("schedule", finalSchedule);
        result.put("totalWeight", totalWeight);
        
        return result;
    }

    /**
     * Calculates the total weight of all tasks in the schedule
     */
    private int calculateTotalWeight(List<Long> taskIds, List<Task> allTasks) {
        Map<Long, Task> taskMap = allTasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));
        
        int totalWeight = 0;
        for (Long id : taskIds) {
            Task task = taskMap.get(id);
            if (task != null) {
                totalWeight += task.getWeight();
            }
        }
        
        return totalWeight;
    }
    //endregion

    //region Scheduling Algorithm Methods
    /**
     * Performs topological sort (Kahn's algorithm) on tasks based on dependencies
     */
    private List<Task> topologicalSort(List<Task> tasks) {
        List<Task> result = new ArrayList<>();
        
        // Map task ID to task object for quick lookup
        Map<Long, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));
        
        // Calculate in-degree for each task (number of dependencies)
        Map<Long, Integer> inDegree = new HashMap<>();
        for (Task task : tasks) {
            inDegree.put(task.getId(), 0);
        }
        
        // Calculate in-degrees
        for (Task task : tasks) {
            for (Long dependencyId : task.getDependencies()) {
                inDegree.put(dependencyId, inDegree.getOrDefault(dependencyId, 0) + 1);
            }
        }
        
        System.out.println("In-degrees calculated: " + inDegree);
        
        // Queue for tasks with no dependencies
        Queue<Task> queue = new LinkedList<>();
        for (Task task : tasks) {
            if (inDegree.getOrDefault(task.getId(), 0) == 0) {
                queue.add(task);
                System.out.println("Adding to queue (no dependencies): " + task.getId());
            }
        }
        
        // Process tasks in topological order
        while (!queue.isEmpty()) {
            Task current = queue.poll();
            result.add(current);
            System.out.println("Processing task: " + current.getId());
            
            // Process dependencies
            for (Long dependencyId : current.getDependencies()) {
                Task dependency = taskMap.get(dependencyId);
                if (dependency != null) {
                    int newInDegree = inDegree.get(dependencyId) - 1;
                    inDegree.put(dependencyId, newInDegree);
                    System.out.println("  Reduced in-degree of " + dependencyId + " to " + newInDegree);
                    
                    if (newInDegree == 0) {
                        queue.add(dependency);
                        System.out.println("  Adding to queue: " + dependency.getId());
                    }
                }
            }
        }
        
        System.out.println("Topological sort result: " + result.stream().map(Task::getId).collect(Collectors.toList()));
        
        // Check for cycles
        if (result.size() < tasks.size()) {
            System.out.println("WARNING: Possible cycle in dependencies. Not all tasks included in topological sort.");
        }
        
        return result;
    }

    /**
     * Computes the earliest start time for each task based on dependencies
     * All times are measured in hours
     */
    private List<Task> computeEarliestStartTimes(List<Task> tasks) {
        // Map task ID to task object
        Map<Long, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));
        
        System.out.println("\nComputing earliest start times:");
        
        // For each task in topological order
        for (Task task : tasks) {
            int earliestStartTime = 0; // Default start time
            
            // Find the maximum end time of its dependencies
            for (Long depId : task.getDependencies()) {
                Task dependency = taskMap.get(depId);
                if (dependency != null) {
                    int dependencyEndTime = dependency.getEarliestStartTime() + dependency.getEstimatedDuration();
                    earliestStartTime = Math.max(earliestStartTime, dependencyEndTime);
                    System.out.println("Task " + task.getId() + " depends on " + depId + 
                                     " which ends at " + dependencyEndTime + 
                                     " hours, updating EST to " + earliestStartTime);
                }
            }
            
            // Set the earliest start time and end time
            task.setEarliestStartTime(earliestStartTime);
            task.setEndTime(earliestStartTime + task.getEstimatedDuration());
            System.out.println("Task " + task.getId() + " final EST: " + earliestStartTime + 
                             " hours, end time: " + task.getEndTime() + 
                             " hours, duration: " + task.getEstimatedDuration() + " hours");
        }
        
        return tasks;
    }

    /**
     * Filters out tasks that cannot be completed before their deadline
     */
    private List<Task> filterValidTasks(List<Task> tasks) {
        List<Task> validTasks = new ArrayList<>();
        
        // First, create a map of task ids to their positions in the schedule
        Map<Long, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));
        
        for (Task task : tasks) {
            int endTime = task.getEndTime(); // End time calculated from dependencies and start time
            int deadline = task.getDeadlineAsInt(); // Deadline in hours
            
            System.out.println("Task " + task.getId() + ": " + task.getName() + 
                    " - EST: " + task.getEarliestStartTime() +
                    " hours, End time: " + endTime + 
                    " hours, Deadline: " + deadline +
                    " hours, Due date: " + task.getDueDate());
            
            // A task is valid if it can be completed before its deadline
            if (endTime <= deadline) {
                System.out.println("   -> VALID");
                validTasks.add(task);
            } else {
                System.out.println("   -> INVALID (end time exceeds deadline)");
            }
        }
        
        return validTasks;
    }

    /**
     * Weighted Interval Scheduling with Dynamic Programming
     * Takes into account task weights, durations, and deadlines
     */
    private List<Long> weightedIntervalSchedulingDP(List<Task> tasks) {
        System.out.println("\nWeighted Interval Scheduling DP:");
        
        if (tasks.isEmpty()) {
            System.out.println("No valid tasks to schedule");
            return new ArrayList<>();
        }
        
        // First, separate tasks into two groups: independent and dependent
        List<Task> independentTasks = new ArrayList<>();
        List<Task> dependentTasks = new ArrayList<>();
        
        for (Task task : tasks) {
            if (task.getDependencies().isEmpty()) {
                independentTasks.add(task);
            } else {
                dependentTasks.add(task);
            }
        }
        
        // For independent tasks, sort by weight (highest first)
        independentTasks.sort((a, b) -> Integer.compare(b.getWeight(), a.getWeight()));
        
        // For dependent tasks, sort by end time
        dependentTasks.sort(Comparator.comparingInt(Task::getEndTime));
        
        // Combine the sorted lists
        List<Task> sortedTasks = new ArrayList<>(independentTasks);
        sortedTasks.addAll(dependentTasks);
        
        // Now continue with the DP algorithm on the sorted tasks
        int n = sortedTasks.size();
        
        // Log sorted tasks
        System.out.println("Tasks sorted for scheduling:");
        for (int i = 0; i < sortedTasks.size(); i++) {
            Task task = sortedTasks.get(i);
            System.out.println("[" + i + "] Task " + task.getId() + ": " + task.getName() +
                    " - EST: " + task.getEarliestStartTime() +
                    ", End: " + task.getEndTime() +
                    ", Deadline: " + task.getDeadlineAsInt() +
                    ", Weight: " + task.getWeight());
        }
        
        // Find the latest non-conflicting task for each task
        int[] p = new int[n];
        for (int i = 0; i < n; i++) {
            p[i] = -1; // Default: no compatible task
            
            // For tasks with no dependencies (EST = 0), no need to find compatible tasks
            if (sortedTasks.get(i).getEarliestStartTime() == 0) {
                p[i] = -1;
            } else {
                // Find the latest compatible task (one that finishes before this task can start)
                for (int j = i - 1; j >= 0; j--) {
                    if (sortedTasks.get(j).getEndTime() <= sortedTasks.get(i).getEarliestStartTime()) {
                        p[i] = j;
                        break;
                    }
                }
            }
            
            System.out.println("Task " + sortedTasks.get(i).getId() + " can follow task " + 
                              (p[i] == -1 ? "None" : sortedTasks.get(p[i]).getId()));
        }
        
        // Initialize DP table and selected tasks tracking
        int[] dp = new int[n + 1];
        dp[0] = 0; // Base case: no tasks
        
        List<List<Integer>> selected = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            selected.add(new ArrayList<>());
        }
        
        // Special handling for independent tasks - always include them first
        for (int i = 0; i < independentTasks.size() && i < n; i++) {
            dp[i+1] = dp[i] + independentTasks.get(i).getWeight();
            
            // Add this task to selected
            List<Integer> includeTasks = new ArrayList<>(selected.get(i));
            includeTasks.add(i);
            selected.set(i+1, includeTasks);
        }
        
        // Dynamic programming solution for dependent tasks
        for (int i = independentTasks.size() + 1; i <= n; i++) {
            Task currentTask = sortedTasks.get(i-1);
            
            // Calculate weight if we include the current task
            int includeWeight = currentTask.getWeight();
            List<Integer> includeTasks = new ArrayList<>();
            includeTasks.add(i-1); // Add current task index
            
            // Add weights from non-conflicting tasks
            if (p[i-1] != -1) {
                includeWeight += dp[p[i-1] + 1];
                includeTasks.addAll(selected.get(p[i-1] + 1));
            }
            
            // Weight if we exclude the current task
            int excludeWeight = dp[i-1];
            List<Integer> excludeTasks = new ArrayList<>(selected.get(i-1));
            
            System.out.println("Task " + currentTask.getId() + 
                    ": Include weight = " + includeWeight + 
                    ", Exclude weight = " + excludeWeight);
            
            // Take the maximum strategy
            if (includeWeight >= excludeWeight) {
                dp[i] = includeWeight;
                selected.set(i, includeTasks);
                System.out.println("Decision: Include task " + currentTask.getId());
            } else {
                dp[i] = excludeWeight;
                selected.set(i, excludeTasks);
                System.out.println("Decision: Exclude task " + currentTask.getId());
            }
        }
        
        // Extract the final selected tasks
        List<Integer> selectedIndices = selected.get(n);
        List<Long> result = new ArrayList<>();
        
        // Convert indices to task IDs
        for (Integer idx : selectedIndices) {
            result.add(sortedTasks.get(idx).getId());
        }
        
        System.out.println("DP solution: " + result);
        return result;
    }
    
    /**
     * Ensures all dependencies of included tasks are in the schedule
     * and properly orders the schedule according to dependencies
     */
    private List<Long> ensureDependenciesIncluded(List<Long> scheduledTaskIds, List<Task> validTasks) {
        // Build a map of all valid tasks by ID for easy access
        Map<Long, Task> taskMap = validTasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));
        
        // Create a set of the initially scheduled task IDs
        Set<Long> included = new HashSet<>(scheduledTaskIds);
        
        System.out.println("Initial schedule: " + scheduledTaskIds);
        
        // Recursively include all dependencies
        Set<Long> allNeededDependencies = new HashSet<>();
        for (Long taskId : scheduledTaskIds) {
            findAllDependencies(taskId, taskMap, allNeededDependencies);
        }
        
        // Add all dependencies to the included set
        for (Long depId : allNeededDependencies) {
            if (!included.contains(depId) && taskMap.containsKey(depId)) {
                included.add(depId);
                System.out.println("Added missing dependency: Task " + depId);
            }
        }
        
        // Create a full task list based on the topological sort of ALL included tasks
        List<Long> sortedSchedule = topologicallySortTaskIds(new ArrayList<>(included), taskMap);
        
        if (sortedSchedule.isEmpty()) {
            System.out.println("WARNING: Could not create valid topological sort. Using original schedule.");
            return scheduledTaskIds;
        }
        
        // Sequential execution model - calculate execution times and validate deadlines
        List<Long> finalSchedule = new ArrayList<>();
        int currentTime = 0;
        
        // Check if tasks meet their deadlines in sequential order
        for (Long taskId : sortedSchedule) {
            Task task = taskMap.get(taskId);
            if (task == null) continue;
            
            int startTime = currentTime;
            int endTime = startTime + task.getEstimatedDuration();
            
            System.out.println("Task " + taskId + " would execute: starts=" + startTime + 
                     ", ends=" + endTime + ", deadline=" + task.getDeadlineAsInt());
            
            // Only include tasks that meet their deadlines
            if (endTime <= task.getDeadlineAsInt()) {
                finalSchedule.add(taskId);
                currentTime = endTime; // Update current time only for included tasks
            } else {
                System.out.println("Excluded task " + taskId + " because it exceeds its deadline");
            }
        }
        
        // If all tasks were excluded, use the original set (to maintain some output)
        if (finalSchedule.isEmpty()) {
            System.out.println("WARNING: All tasks would miss deadlines. Using original schedule as fallback.");
            return scheduledTaskIds;
        }
        
        System.out.println("Final schedule with dependencies and deadline checks: " + finalSchedule);
        return finalSchedule;
    }
    
    /**
     * Performs a topological sort on task IDs
     * Returns tasks in dependency order (prerequisites first)
     */
    private List<Long> topologicallySortTaskIds(List<Long> taskIds, Map<Long, Task> taskMap) {
        List<Long> result = new ArrayList<>();
        
        // Create dependency graph
        Map<Long, Set<Long>> graph = new HashMap<>();
        Map<Long, Integer> inDegree = new HashMap<>();
        
        // Initialize
        for (Long id : taskIds) {
            graph.put(id, new HashSet<>());
            inDegree.put(id, 0);
        }
        
        // Build the graph
        for (Long id : taskIds) {
            Task task = taskMap.get(id);
            if (task == null) continue;
            
            for (Long depId : task.getDependencies()) {
                if (taskIds.contains(depId)) {
                    // This is a dependency edge: depId -> id
                    graph.get(depId).add(id);
                    inDegree.put(id, inDegree.get(id) + 1);
                }
            }
        }
        
        // Queue for processing (start with nodes that have no incoming edges)
        Queue<Long> queue = new LinkedList<>();
        for (Long id : taskIds) {
            if (inDegree.get(id) == 0) {
                queue.add(id);
            }
        }
        
        // Process the queue
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            result.add(current);
            
            // Reduce in-degree for all neighbors
            for (Long neighbor : graph.get(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }
        
        // Check for cycles
        if (result.size() < taskIds.size()) {
            System.out.println("WARNING: Graph contains cycles, cannot produce a valid topological sort");
            return new ArrayList<>();
        }
        
        return result;
    }
    
    /**
     * Recursively find all dependencies for a task, including dependencies of dependencies
     */
    private void findAllDependencies(Long taskId, Map<Long, Task> taskMap, Set<Long> allDependencies) {
        Task task = taskMap.get(taskId);
        if (task == null) return;
        
        // Process each direct dependency
        for (Long depId : task.getDependencies()) {
            // Skip if already processed to avoid cycles
            if (allDependencies.contains(depId)) continue;
            
            // Add this dependency
            if (taskMap.containsKey(depId)) {
                allDependencies.add(depId);
                
                // Recursively find dependencies of this dependency
                findAllDependencies(depId, taskMap, allDependencies);
            }
        }
    }
    //endregion
} 