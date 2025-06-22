package org.asaa.behaviours.appliances.tasks;

import org.asaa.agents.base.SmartApplianceAgent;

public class PowerProposalGenerator {
    private SmartApplianceAgent agent;

    public PowerProposal generateProposal(SmartApplianceAgent agent, String agentId, TaskInfo currentTask,
                                          int currentPowerUsage, PowerRequest cfpRequest) {

        this.agent = agent;
        // Determine what action this agent can take
        PowerProposal.Action action = determineOptimalAction(currentTask, cfpRequest);

        // Calculate how much power can be freed
        int powerAmount = calculateFreePower(currentPowerUsage, action);

        // Calculate impact score (how much this hurts the agent)
        double impactScore = calculateImpactScore(currentTask, action, cfpRequest);

        // Estimate time needed to free the power
        long timeToFree = calculateTimeToFree(currentTask, action);

        return new PowerProposal(agentId, powerAmount, currentTask, action, impactScore, timeToFree);
    }

    private PowerProposal.Action determineOptimalAction(TaskInfo currentTask, PowerRequest cfpRequest) {
        // Priority-based decision making
        if (currentTask == null || !isTaskRunning()) {
            // Idle agents can only offer minimal power savings, so we'll return
            // a proposal that's unlikely to be selected (handled by impact score)
            return PowerProposal.Action.PAUSE; // Doesn't matter much since no real task to pause
        }

        // If current task has much lower priority, consider pausing/interrupting
        if (cfpRequest.getTaskInfo().getPriority() - currentTask.getPriority() > 20) {
            if (currentTask.isPausable()) {
                return PowerProposal.Action.PAUSE;
            } else if (currentTask.isInterruptible()) {
                return PowerProposal.Action.INTERRUPT;
            }
        }

        // If current task is pausable and request is urgent
        if (currentTask.isPausable() &&
                (cfpRequest.getUrgency() == PowerRequest.Urgency.IMMEDIATE ||
                        cfpRequest.getUrgency() == PowerRequest.Urgency.HIGH)) {
            return PowerProposal.Action.PAUSE;
        }

        // Default to pause if possible, otherwise interrupt if allowed
        if (currentTask.isPausable()) {
            return PowerProposal.Action.PAUSE;
        } else if (currentTask.isInterruptible()) {
            return PowerProposal.Action.INTERRUPT;
        }

        // If we can't pause or interrupt, still need to return something
        // This will be handled by a very high impact score
        return PowerProposal.Action.INTERRUPT; // Will be rejected due to high impact
    }

    private int calculateFreePower(int currentPowerUsage, PowerProposal.Action action) {
        if (!isTaskRunning())
            return agent.getIdleDraw();
        return switch (action) {
            case PAUSE, INTERRUPT ->
                // Can free all power currently being used
                    currentPowerUsage;
            default -> 0;
        };
    }

    private double calculateImpactScore(TaskInfo currentTask, PowerProposal.Action action, PowerRequest cfpRequest) {
        double impact = 0.0;

        // If no current task (idle agent), very high impact score to make it unlikely to be selected
        if (currentTask == null || !isTaskRunning()) {
            return 1000.0; // Very high impact - idle agents offer minimal benefit
        }

        // Base impact based on current task priority
        impact += currentTask.getPriority() * 2.0;

        // Action-specific impact
        switch (action) {
            case PAUSE:
                impact += 20.0; // Moderate impact
                // Less impact if task is almost done
                if (currentTask.getEstimatedRemainingTime() < 300000) { // < 5 minutes
                    impact += 15.0;
                }
                break;

            case INTERRUPT:
                impact += 50.0; // High impact - losing all progress
                // Consider how much work would be lost
                long runningTime = System.currentTimeMillis() - currentTask.getStartTime();
                impact += (runningTime / 60000.0) * 5.0; // Add 5 points per minute of lost work
                break;
        }

        // Task type impact
        switch (currentTask.getType()) {
            case CRITICAL_SAFETY:
                impact += 100.0; // Very high impact
                break;
            case USER_COMFORT:
                impact += 30.0;
                break;
            case MAINTENANCE:
                impact += 20.0;
                break;
            case OPTIMIZATION:
                impact += 10.0;
                break;
            case ENTERTAINMENT:
                impact += 5.0;
                break;
        }

        // If the requesting task has much higher priority, reduce our impact score
        int priorityDiff = cfpRequest.getTaskInfo().getPriority() - currentTask.getPriority();
        if (priorityDiff > 0) {
            impact *= (1.0 - (priorityDiff / 100.0)); // Reduce impact proportionally
        }

        return Math.max(impact, 1.0); // Minimum impact of 1
    }

    private long calculateTimeToFree(TaskInfo currentTask, PowerProposal.Action action) {
        if (!isTaskRunning())
            return 0;

        return switch (action) {
            case PAUSE ->
                // Time to safely pause (save state, etc.)
                    estimatePauseTime(currentTask);
            case INTERRUPT ->
                // Time to safely interrupt (cleanup, save what we can)
                    estimateInterruptTime(currentTask);

            default -> 0;
        };
    }

    private long estimatePauseTime(TaskInfo currentTask) {
        // This depends on the specific task type and what needs to be saved
        return switch (currentTask.getType()) {
            case CRITICAL_SAFETY -> 30000; // 30 seconds - need to be very careful
            case USER_COMFORT -> 10000; // 10 seconds - moderate shutdown
            case MAINTENANCE -> 15000; // 15 seconds - may need to finish current cycle
            default -> 5000;  // 5 seconds - quick pause
        };
    }

    private long estimateInterruptTime(TaskInfo currentTask) {
        // Usually longer than pause because we need cleanup
        return estimatePauseTime(currentTask) + 5000; // Add 5 seconds for cleanup
    }

    private boolean isTaskRunning() {
        // This would check the actual task state
        // Implementation depends on how you track task states
        return agent.getCurrentTaskBehaviour().getStatus() == TaskBehaviour.Status.running;
    }
}
