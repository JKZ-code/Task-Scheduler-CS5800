package com.group12.taskscheduler.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.HashSet;

@Entity // Marks this class as a database entity
@Table(name = "tasks") // Specifies the table name in MySQL
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
    @ElementCollection
    @CollectionTable(
        name = "task_dependencies", 
        joinColumns = @JoinColumn(name = "task_id")
    )
    @Column(name = "dependency_id") 
    private Set<Long> dependenciesSet = new HashSet<>();

    private String dependenciesStr; 

    // Transient fields used by the algorithm (not persisted)
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
        this.valid = true;
    }

    public Task(String name, int weight, LocalDate dueDate, int estimatedDuration) {
        this.name = name;
        this.weight = weight;
        this.dueDate = dueDate;
        this.estimatedDuration = estimatedDuration;
        this.valid = true;
        this.dependenciesSet = new HashSet<>(); // Initialize empty dependency set
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public int getEarliestStartTime() {
        return earliestStartTime;
    }

    public void setEarliestStartTime(int earliestStartTime) {
        this.earliestStartTime = earliestStartTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getDependenciesStr() {
        return dependenciesStr;
    }

    public void setDependenciesStr(String dependenciesStr) {
        this.dependenciesStr = dependenciesStr;

        if ((this.dependenciesSet == null || this.dependenciesSet.isEmpty()) 
            && dependenciesStr != null && !dependenciesStr.isBlank()) {
            Set<Long> parsedDeps = new HashSet<>();
            for (String part : dependenciesStr.split(",")) {
                try {
                    parsedDeps.add(Long.parseLong(part.trim()));
                } catch (NumberFormatException ignored) {}
            }
            this.dependenciesSet = parsedDeps;
        }
    }

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
        return (int) (daysBetween * 24);
    }

    /**
     * Used in tests to directly set the deadline value in hours
     */
    public void setDeadlineOverride(int hours) {
        this.deadlineOverride = hours;
    }

    public Set<Long> getDependenciesSet() {
        return dependenciesSet;
    }

    public void setDependenciesSet(Set<Long> dependenciesSet) {
        this.dependenciesSet = dependenciesSet;
    }

    public void addDependency(Long taskId) {
        if (this.dependenciesSet == null) {
            this.dependenciesSet = new HashSet<>();
        }
        this.dependenciesSet.add(taskId);
    }

    public void removeDependency(Long taskId) {
        if (this.dependenciesSet != null) {
            this.dependenciesSet.remove(taskId);
        }
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
                ", dependencies=" + dependenciesSet +
                '}';
    }
}
