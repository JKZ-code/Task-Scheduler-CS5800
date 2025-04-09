package org.test.frontend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskResponse {
    private Long id;
    private String name;
    private int weight;
    private LocalDate dueDate;
    private int estimatedDuration;
    //private String dependenciesStr;
    private Set<Long> dependenciesSet;

    public TaskResponse(Long id, String task, int weight, LocalDate dueDate, int estimatedDuration, Set<Long> dependenciesSet) {
        this.id = id;
        this.name = task;
        this.weight = weight;
        this.dueDate = dueDate;
        this.estimatedDuration = estimatedDuration;
        this.dependenciesSet = dependenciesSet;
    }

    public TaskResponse() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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

//    public String getDependenciesStr() {
//        return dependenciesStr;
//    }
    public Set<Long> getDependenciesSet() {
        return dependenciesSet;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

//    public void setDependenciesStr(String dependenciesStr) {
//        this.dependenciesStr = dependenciesStr;
//    }
    public void setDependenciesSet(Set<Long> dependenciesSet) {
        this.dependenciesSet = dependenciesSet;
    }
}
