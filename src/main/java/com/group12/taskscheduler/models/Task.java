package com.group12.taskscheduler.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Entity  // Marks this class as a database entity
@Table(name = "tasks")  // Specifies the table name in MySQL
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Weight on a scale from 1-10 (importance)
    @Column(nullable = false)
    private int weight;

    // Due date serves as the deadline
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // Estimated duration in hours
    @Column(name = "estimated_duration", nullable = false)
    private int estimatedDuration;

    // Dependencies stored as a comma-separated string of task IDs
    private String dependenciesStr;

    // Transient fields used by the algorithm (not persisted)
    @Transient
    private List<Long> dependencies;
    
    @Transient
    private int earliestStartTime;
    
    @Transient
    private int endTime;
    
    @Transient
    private boolean valid;

    @Transient
    private Integer deadlineOverride; // Used for testing to override the calculated deadline

    // Constructors
    public Task() {
        this.dependencies = new ArrayList<>();
        this.valid = true;
    }

    public Task(String name, int weight, LocalDate dueDate, int estimatedDuration, String dependenciesStr) {
        this.name = name;
        this.weight = weight;
        this.dueDate = dueDate;
        this.estimatedDuration = estimatedDuration;
        this.dependenciesStr = dependenciesStr;
        this.dependencies = new ArrayList<>();
        this.valid = true;
        
        // Parse dependencies if provided
        if (dependenciesStr != null && !dependenciesStr.isEmpty()) {
            parseDependencies();
        }
    }

    // Parse dependencies from string to List<Long>
    public void parseDependencies() {
        dependencies = new ArrayList<>();
        if (dependenciesStr != null && !dependenciesStr.isEmpty()) {
            String[] deps = dependenciesStr.split(",");
            for (String dep : deps) {
                try {
                    dependencies.add(Long.parseLong(dep.trim()));
                } catch (NumberFormatException e) {
                    // Skip invalid dependency
                }
            }
        }
    }

    // Convert dependencies list back to string
    public void convertDependenciesToString() {
        if (dependencies != null && !dependencies.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Long dep : dependencies) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(dep);
            }
            this.dependenciesStr = sb.toString();
        } else {
            this.dependenciesStr = "";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public int getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public String getDependenciesStr() { return dependenciesStr; }
    public void setDependenciesStr(String dependenciesStr) { 
        this.dependenciesStr = dependenciesStr;
        parseDependencies();
    }

    public List<Long> getDependencies() { 
        if (dependencies == null || dependencies.isEmpty()) {
            parseDependencies();
        }
        return dependencies; 
    }
    public void setDependencies(List<Long> dependencies) { 
        this.dependencies = dependencies;
        convertDependenciesToString();
    }

    public int getEarliestStartTime() { return earliestStartTime; }
    public void setEarliestStartTime(int earliestStartTime) { this.earliestStartTime = earliestStartTime; }

    public int getEndTime() { return endTime; }
    public void setEndTime(int endTime) { this.endTime = endTime; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    /**
     * Calculates the deadline as hours from now to the due date
     * This makes it comparable with the task duration (also in hours)
     */
    public int getDeadlineAsInt() {
        // If there's a override value for testing, use it
        if (deadlineOverride != null) {
            return deadlineOverride;
        }
        
        // Calculate hours from now to the due date
        if (dueDate == null) {
            return Integer.MAX_VALUE; // No deadline
        }
        
        LocalDate today = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(today, dueDate);
        
        // Convert days to hours (24 hours per day)
        return (int)(daysBetween * 24);
    }

    /**
     * Used in tests to directly set the deadline value in hours
     */
    public void setDeadlineOverride(int hours) {
        this.deadlineOverride = hours;
    }

    /**
     * Returns the dependencies as a comma-separated string
     */
    public String getDependenciesString() {
        if (dependencies == null || dependencies.isEmpty()) {
            return "";
        }
        return dependencies.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    // Helper method for the scheduler
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", weight=" + weight +
                ", dueDate=" + dueDate +
                ", estimatedDuration=" + estimatedDuration +
                ", dependencies=" + dependencies +
                '}';
    }
}
