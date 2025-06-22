package org.asaa.behaviours.appliances.tasks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PowerRequest {
    private String requesterId;
    private int powerAmount;
    private TaskInfo taskInfo;
    private Urgency urgency;
    private long maxWaitTime; // Maximum time willing to wait
    private boolean canBeScheduled; // Can this request be delayed/scheduled

    public PowerRequest(String requesterId, int powerAmount) {
        this.requesterId = requesterId;
        this.powerAmount = powerAmount;
        taskInfo = null;
        urgency = null;
        maxWaitTime = 0;
        canBeScheduled = false;
    }

    public enum Urgency {
        IMMEDIATE,    // Must start now
        HIGH,         // Should start soon
        NORMAL,       // Can wait reasonable time
        LOW          // Can be delayed significantly
    }
}

