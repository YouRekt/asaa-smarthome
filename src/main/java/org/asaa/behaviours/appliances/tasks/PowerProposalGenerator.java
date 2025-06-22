package org.asaa.behaviours.appliances.tasks;

public class PowerProposalGenerator {

    public PowerProposal generateProposal(String agentId, TaskInfo currentTask,
                                          int currentPowerUsage, PowerRequest cfpRequest) {

        // Determine what action this agent can take
        PowerProposal.Action action = determineOptimalAction(currentTask, cfpRequest);

        // Calculate how much power can be freed
        int powerAmount = calculateFreePower(currentTask, currentPowerUsage, action);

        // Calculate impact score (how much this hurts the agent)
        double impactScore = calculateImpactScore(currentTask, action, cfpRequest);

        // Estimate time needed to free the power
        long timeToFree = calculateTimeToFree(currentTask, action);

        return new PowerProposal(agentId, powerAmount, currentTask, action, impactScore, timeToFree);
    }

    private PowerProposal.Action determineOptimalAction(TaskInfo currentTask, PowerRequest cfpRequest) {
        // Priority-based decision making
        if (currentTask == null || !isTaskRunning(currentTask)) {
            return PowerProposal.Action.RESCHEDULE; // No current task or task not running
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

        // If we can't do anything with current task, see if we can reschedule
        return PowerProposal.Action.RESCHEDULE;
    }

    private int calculateFreePower(TaskInfo currentTask, int currentPowerUsage, PowerProposal.Action action) {
        switch (action) {
            case PAUSE:
            case INTERRUPT:
                // Can free all power currently being used
                return currentPowerUsage;

            case RESCHEDULE:
                // If we're not running anything, we can offer our maximum power capacity
                // This would be the power we would use if we started our task
                return getMaxPowerCapacity(); // Agent should know its own max power

            default:
                return 0;
        }
    }

    private double calculateImpactScore(TaskInfo currentTask, PowerProposal.Action action, PowerRequest cfpRequest) {
        double impact = 0.0;

        if (currentTask == null) {
            return 10.0; // Low impact - no current task
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

            case RESCHEDULE:
                impact += 10.0; // Low impact - just delaying start
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
        return switch (action) {
            case PAUSE ->
                // Time to safely pause (save state, etc.)
                    estimatePauseTime(currentTask);
            case INTERRUPT ->
                // Time to safely interrupt (cleanup, save what we can)
                    estimateInterruptTime(currentTask);
            case RESCHEDULE ->
                // Immediate - no power to free right now
                    0;
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

    private boolean isTaskRunning(TaskInfo currentTask) {
        // This would check the actual task state
        // Implementation depends on how you track task states
        return currentTask != null && currentTask.getEstimatedRemainingTime() > 0;
    }

    private int getMaxPowerCapacity() {
        // Each agent should know its maximum power consumption
        // This is agent-specific and should be configurable
        return -999; // Example value
    }
}
