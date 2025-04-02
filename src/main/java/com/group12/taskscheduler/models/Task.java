package com.group12.taskscheduler.models;


import jakarta.persistence.*;
import jakarta.persistence.GenerationType;

import java.time.LocalDate;

@Entity  // Marks this class as a database entity
@Table(name = "tasks")  // Specifies the table name in MySQL
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int priority;

    @Column(name = "due_date")
    private LocalDate dueDate;  // Storing date as LocalDate

    @Column(name = "estimated_duration")
    private int estimatedDuration;  // In minutes

    private String dependencies;  // Can be JSON, list of task IDs, or relation to another entity

    // Constructors
    public Task() {}

    public Task(String name, int priority, LocalDate dueDate, int estimatedDuration, String dependencies) {
        this.name = name;
        this.priority = priority;
        this.dueDate = dueDate;
        this.estimatedDuration = estimatedDuration;
        this.dependencies = dependencies;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public int getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public String getDependencies() { return dependencies; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }
}
