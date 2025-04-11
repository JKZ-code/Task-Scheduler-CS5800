package org.test.frontend;

import java.util.List;

public class ScheduleDisplay {
    private List<String> schedule;
    private int totalWeight;

    public ScheduleDisplay() {
    }

    public List<String> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<String> schedule) {
        this.schedule = schedule;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }
}
