package org.test.frontend;

import java.time.LocalDate;
import java.util.Set;

public class TaskResponse {
    private Long id;
    private String task;
    private int weight;
    private LocalDate dueDate;
    private int estimatedDuration;
    private String dependenciesStr;

    public TaskResponse(Long id, String task, int weight, LocalDate dueDate, int estimatedDuration, String dependenciesStr) {
        this.id = id;
        this.task = task;
        this.weight = weight;
        this.dueDate = dueDate;
        this.estimatedDuration = estimatedDuration;
        this.dependenciesStr = dependenciesStr;
    }

    public Long getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public int getWeight() {
        return weight;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public String getDependenciesStr() {
        return dependenciesStr;
    }
}
