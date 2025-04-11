package com.group12.taskscheduler.services;

import com.group12.taskscheduler.models.Task;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulerService {

    /**
     * Checks if any task in the list has dependencies.
     * 
     * @param tasks The list of tasks.
     * @return true if at least one task has dependencies in its dependenciesSet. 
     */
    public boolean hasDependencies(List<Task> tasks) {
        return tasks.stream()
                .anyMatch(task -> task.getDependenciesSet() != null && !task.getDependenciesSet().isEmpty());
    }

    /**
     * Applies a greedy scheduling algorithm for tasks without dependencies.
     *
     * @param tasks The list of tasks to schedule.
     * @return The sorted list of tasks.
     */
    public List<Task> greedySchedule(List<Task> tasks) {
        return tasks.stream()
                .sorted(Comparator
                        .comparing(Task::getWeight).reversed()
                        .thenComparing(Task::getDueDate)
                        .thenComparing(Task::getEstimatedDuration))
                .collect(Collectors.toList());
    }

    /**
     * Topologically sorts tasks using Kahn's algorithm with a priority queue.
     *
     * @param tasks The list of tasks.
     * @return A topologically sorted list of tasks.
     */
    public List<Task> topologicalSort(List<Task> tasks) {
        Map<Long, Task> idToTask = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));

        Map<Long, List<Long>> graph = new HashMap<>();
        Map<Long, Integer> inDegree = new HashMap<>();

        // Initialize in-degrees
        for (Task task : tasks) {
            inDegree.put(task.getId(), 0);
        }

        // Build the graph
        for (Task task : tasks) {
            for (Long depId : task.getDependenciesSet()) {
                graph.computeIfAbsent(depId, k -> new ArrayList<>()).add(task.getId());
                inDegree.put(task.getId(), inDegree.get(task.getId()) + 1);
            }
        }

        // Priority queue to pick tasks with zero in-degree based on earliest due date.
        PriorityQueue<Task> queue = new PriorityQueue<>(Comparator.comparing(Task::getDueDate));
        for (Task task : tasks) {
            if (inDegree.get(task.getId()) == 0) {
                queue.add(task);
            }
        }

        List<Task> sortedTasks = new ArrayList<>();
        while (!queue.isEmpty()) {
            Task current = queue.poll();
            sortedTasks.add(current);
            List<Long> neighbors = graph.getOrDefault(current.getId(), new ArrayList<>());
            for (Long neighborId : neighbors) {
                inDegree.put(neighborId, inDegree.get(neighborId) - 1);
                if (inDegree.get(neighborId) == 0) {
                    queue.add(idToTask.get(neighborId));
                }
            }
        }

        if (sortedTasks.size() != tasks.size()) {
            throw new RuntimeException("Cycle detected in task dependencies.");
        }
        return sortedTasks;
    }

    public List<Task> computeEST(List<Task> sortedTasks) {
        Map<Long, Task> idToTask = sortedTasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));

        for (Task task : sortedTasks) {
            int est = 0;
            for (Long depId : task.getDependenciesSet()) {
                Task depTask = idToTask.get(depId);
                if (depTask != null) {
                    est = Math.max(est, depTask.getEndTime());
                }
            }
            task.setEarliestStartTime(est);
            task.setEndTime(est + task.getEstimatedDuration());
        }
        return sortedTasks;
    }

    /**
     * Filters tasks that meet their deadlines.
     *
     * @param sortedTasks The tasks in topologically sorted order with computed EST
     *                    and End Times
     * @return A list of valid tasks, sorted by end time
     */
    public List<Task> filterAndSortTasks(List<Task> sortedTasks) {
        List<Task> validTasks = sortedTasks.stream()
                .filter(task -> task.getEndTime() <= task.getDeadlineAsInt())
                .collect(Collectors.toList());

        validTasks.sort(Comparator.comparing(Task::getEndTime));
        return validTasks;
    }

    /**
     * Implements Weighted Interval Scheduling to select a subset of tasks that
     * maximizes total weight while meeting deadlines.
     *
     * @param tasks The list of valid tasks, sorted by endTime.
     * @return The optimal subset of tasks.
     */
    public List<Task> dpSchedule(List<Task> tasks) {
        int n = tasks.size();
        if (n == 0)
            return new ArrayList<>();

        int[] dp = new int[n]; // Maximum weight achievable up to task i
        int[] choice = new int[n]; // Whether task i is included (1) or not (0)
        int[] p = new int[n]; // Previous compatible task index

        // Find previous compatible tasks
        Arrays.fill(p, -1);
        for (int i = 0; i < n; i++) {
            Task current = tasks.get(i);
            for (int j = i - 1; j >= 0; j--) {
                if (tasks.get(j).getEndTime() <= current.getEarliestStartTime()) {
                    p[i] = j;
                    break;
                }
            }
        }

        // Dynamic programming to find optimal solution
        dp[0] = tasks.get(0).getWeight();
        choice[0] = 1;

        for (int i = 1; i < n; i++) {
            // Compare including vs excluding current task
            int includeWeight = tasks.get(i).getWeight() + (p[i] != -1 ? dp[p[i]] : 0);
            if (includeWeight > dp[i - 1]) {
                dp[i] = includeWeight;
                choice[i] = 1;
            } else {
                dp[i] = dp[i - 1];
                choice[i] = 0;
            }
        }

        // Reconstruct solution
        List<Task> selected = new ArrayList<>();
        for (int i = n - 1; i >= 0;) {
            if (choice[i] == 1) {
                selected.add(tasks.get(i));
                i = p[i];
            } else {
                i--;
            }
        }
        Collections.reverse(selected);
        return selected;
    }

    /**
     * Main scheduling method that implements the 5-step algorithm:
     * 1. Topological sort with greedy heuristics
     * 2. Compute EST and End Times
     * 3. Filter valid tasks
     * 4. Sort by End Time
     * 5. Apply Weighted Interval Scheduling
     *
     * @param tasks The list of tasks.
     * @return The scheduled list of tasks.
     */
    public List<Task> scheduleTasks(List<Task> tasks) {
        // Step 1
        List<Task> sortedTasks;
        if (hasDependencies(tasks)) {
            sortedTasks = topologicalSort(tasks);
        } else {
            sortedTasks = greedySchedule(tasks);
        }

        // Step 2: Compute EST and End Times
        sortedTasks = computeEST(sortedTasks);

        // Steps 3-4: Filter valid tasks and sort by End Time
        List<Task> validSortedTasks = filterAndSortTasks(sortedTasks);

        // Step 5: Apply Weighted Interval Scheduling
        return dpSchedule(validSortedTasks);
    }
}
