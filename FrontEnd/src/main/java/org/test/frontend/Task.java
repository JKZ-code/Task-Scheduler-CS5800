package org.test.frontend;

public class Task {
    private String task;
    private String priority;
    private String estimatedDuration;
    private String dueDate;
    private String dependencies;

    public Task(String task, String priority, String estimatedDuration, String dueDate, String dependencies) {
        this.task = task;
        this.priority = priority;
        this.estimatedDuration = estimatedDuration;
        this.dueDate = dueDate;
        this.dependencies = dependencies;
    }

    public Task(){}

    public String getTask() {
        return task;
    }

    public String getPriority() {
        return priority;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDependencies() {
        return dependencies;
    }
}
