package org.test.frontend;

import java.time.LocalDate;

public class Task {
    private String name;
    private int weight;
    private LocalDate dueDate;
    private int estimatedDuration;
    private String dependenciesStr;

    public Task(String task, int weight, int estimatedDuration, LocalDate dueDate, String dependencies) {
        this.name = task;
        this.weight = weight;
        this.estimatedDuration = estimatedDuration;
        this.dueDate = dueDate;
        this.dependenciesStr = dependencies;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getDependenciesStr() {
        return dependenciesStr;
    }
}
