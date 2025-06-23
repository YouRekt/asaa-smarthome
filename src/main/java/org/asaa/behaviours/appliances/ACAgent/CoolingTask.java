package org.asaa.behaviours.appliances.ACAgent;

import org.asaa.agents.appliances.ACAgent;
import org.asaa.behaviours.appliances.tasks.PowerRequest;
import org.asaa.behaviours.appliances.tasks.TaskBehaviour;
import org.asaa.behaviours.appliances.tasks.TaskInfo;

public class CoolingTask extends TaskBehaviour<ACAgent> {
    private final double coolingRate;
    private final double targetTemperature;
    private final long delayMillis = 1000;

    private boolean awaitingDelay = false;
    private long nextWakeTime = 0;

    public CoolingTask(ACAgent agent, double coolingRate, double targetTemperature) {
        super(agent, "cooling-task", agent.getPriority(), true, true, TaskInfo.Type.USER_COMFORT, PowerRequest.Urgency.NORMAL, true);
        this.coolingRate = coolingRate;
        this.targetTemperature = targetTemperature;
        this.powerUsage = agent.getActiveDraw();
        registerError("cooling-error", new TaskBehaviour<ACAgent>(agent, "cooling-error-resolver", priority, false, false, TaskInfo.Type.MAINTENANCE, PowerRequest.Urgency.HIGH, true) {
            private final long delayMillis = 5000;
            private boolean awaitingDelay = false;
            private long nextWakeTime;

            @Override
            protected boolean execute() {
                if (awaitingDelay) {
                    if (System.currentTimeMillis() >= nextWakeTime) {
                        agent.getLogger().info("Cooling task: error has been successfully resolved");
                        return true;
                    } else {
                        block(nextWakeTime - System.currentTimeMillis());
                        return false;
                    }
                }

                nextWakeTime = System.currentTimeMillis() + delayMillis;
                awaitingDelay = true;
                return false;
            }
        });
    }

    private CoolingTask(ACAgent agent, int priority, double coolingRate, double targetTemperature) {
        super(agent, "cooling-task", priority, true, true, TaskInfo.Type.USER_COMFORT, PowerRequest.Urgency.NORMAL, true);
        this.coolingRate = coolingRate;
        this.targetTemperature = targetTemperature;
        this.powerUsage = agent.getActiveDraw();
    }

    @Override
    public TaskBehaviour<ACAgent> resumeWith(int priority) {
        return new CoolingTask(agent, priority, coolingRate, targetTemperature);
    }

    @Override
    protected boolean execute() {
        double currentTemp = agent.getCurrentTemperature();

        estimatedRemainingTime = (long) Math.ceil((currentTemp - targetTemperature) / coolingRate) * delayMillis;

        if (currentTemp <= targetTemperature) {
            agent.getLogger().info("Target temperature reached. Done.");
            return true;
        }

        if (awaitingDelay) {
            if (System.currentTimeMillis() >= nextWakeTime) {
                awaitingDelay = false; // Delay has passed
            } else {
                block(nextWakeTime - System.currentTimeMillis());
                return false; // Still waiting
            }
        }

        if (!definedErrors.isEmpty()) {
            if (simulateError()) return false;
        }

        // Apply cooling
        double newTemp = currentTemp - coolingRate;
        agent.environmentService.getArea(agent.getAreaName()).setAttribute("temperature", newTemp);
        agent.getLogger().info("Cooling: before = {}, after = {}", String.format("%.2f", currentTemp), String.format("%.2f", newTemp));

        // Schedule next cycle (delay)
        agent.requestTemperature(); // will eventually update current temperature
        awaitingDelay = true;
        nextWakeTime = System.currentTimeMillis() + delayMillis;

        return false; // not finished
    }
}
