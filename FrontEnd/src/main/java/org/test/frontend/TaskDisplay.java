package org.test.frontend;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class TaskDisplay {
    private final SimpleIntegerProperty number;
    private final SimpleStringProperty task;
    private final SimpleStringProperty priority;
    private final SimpleStringProperty estimatedDuration;
    private final SimpleStringProperty dueDate;
    private final SimpleStringProperty dependencies;

    public TaskDisplay(Integer number, Task task) {
        this.number = new SimpleIntegerProperty(number);
        this.task = new SimpleStringProperty(task.getTask());
        this.priority = new SimpleStringProperty(task.getPriority());
        this.estimatedDuration = new SimpleStringProperty(task.getEstimatedDuration());
        this.dueDate = new SimpleStringProperty(task.getDueDate());
        this.dependencies = new SimpleStringProperty(task.getDependencies());
    }

    public int getNumber() {
        return number.get();
    }

    public SimpleIntegerProperty numberProperty() {
        return number;
    }

    public String getTask() {
        return task.get();
    }

    public SimpleStringProperty taskProperty() {
        return task;
    }

    public String getPriority() {
        return priority.get();
    }

    public SimpleStringProperty priorityProperty() {
        return priority;
    }

    public String getEstimatedDuration() {
        return estimatedDuration.get();
    }

    public SimpleStringProperty estimatedDurationProperty() {
        return estimatedDuration;
    }

    public String getDueDate() {
        return dueDate.get();
    }

    public SimpleStringProperty dueDateProperty() {
        return dueDate;
    }

    public String getDependencies() {
        return dependencies.get();
    }

    public SimpleStringProperty dependenciesProperty() {
        return dependencies;
    }
}
