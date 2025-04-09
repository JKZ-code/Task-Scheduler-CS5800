package org.test.frontend;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TaskDisplay {
    private final SimpleIntegerProperty number;
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty weight;
    private final SimpleIntegerProperty estimatedDuration;
    private final SimpleStringProperty dueDate;
    private final SimpleStringProperty dependencies;

    public TaskDisplay(Integer number, Task task, String dependencyNums) {
        this.number = new SimpleIntegerProperty(number);
        this.name = new SimpleStringProperty(task.getName());
        this.weight = new SimpleIntegerProperty(task.getWeight());
        this.estimatedDuration = new SimpleIntegerProperty(task.getEstimatedDuration());
        this.dueDate = new SimpleStringProperty(task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        this.dependencies = new SimpleStringProperty(dependencyNums);
    }

    public int getNumber() {
        return number.get();
    }

    public SimpleIntegerProperty numberProperty() {
        return number;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public int getWeight() {
        return weight.get();
    }

    public SimpleIntegerProperty weightProperty() {
        return weight;
    }

    public int getEstimatedDuration() {
        return estimatedDuration.get();
    }

    public SimpleIntegerProperty estimatedDurationProperty() {
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
