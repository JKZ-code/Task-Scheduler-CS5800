package org.test.frontend;

import java.util.HashMap;
import java.util.Map;

public class TaskState {
    private static final TaskState instance = new TaskState();

    private Map<Integer, TaskResponse> numberToTask = new HashMap<>();
    private Map<Long, Integer> idToNumber = new HashMap<>();

    private TaskState() {
    }

    public static TaskState getInstance() {
        return instance;
    }

    public Map<Integer, TaskResponse> getNumberToTask() {
        return numberToTask;
    }

    public void addToNumberToTask(int number, TaskResponse taskResponse) {
        this.numberToTask.put(number, taskResponse);
    }

    public void addToIdToNumber(Long id, Integer number) {
        this.idToNumber.put(id, number);
    }

    public void setNumberToTask(Map<Integer, TaskResponse> numberToTask) {
        this.numberToTask = numberToTask;
    }

    public Map<Long, Integer> getIdToNumber() {
        return idToNumber;
    }

    public void setIdToNumber(Map<Long, Integer> idToNumber) {
        this.idToNumber = idToNumber;
    }
}
