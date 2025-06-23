package org.asaa.behaviours.appliances.tasks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PowerProposal {
    private String agentId;
    private int powerAmount;
    private TaskInfo currentTask;
    private Action action; // PAUSE, INTERRUPT
    private double bonusImpactScore; // How much this affects the agent
    private long timeToFree; // How long to free the power

    public enum Action {
        PAUSE,        // Pause and can resume later
        INTERRUPT    // Stop permanently (abort)
    }
}

