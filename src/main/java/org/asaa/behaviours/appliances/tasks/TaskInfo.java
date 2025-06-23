package org.asaa.behaviours.appliances.tasks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskInfo {
    private String taskId;
    private int priority; // Higher number = higher priority
    private boolean pausable;
    private boolean interruptible;
    private long estimatedRemainingTime; // in milliseconds
    private Type type;
    private long startTime;

    public enum Type {
        CRITICAL_SAFETY,    // e.g., security systems
        USER_COMFORT,       // e.g., heating, cooling
        MAINTENANCE,        // e.g., cleaning cycles
        OPTIMIZATION,       // e.g., energy optimization tasks
        ENTERTAINMENT      // e.g., media streaming
    }
}

