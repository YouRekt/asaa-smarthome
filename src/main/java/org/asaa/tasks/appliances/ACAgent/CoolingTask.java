package org.asaa.tasks.appliances.ACAgent;

import org.asaa.agents.appliances.ACAgent;
import org.asaa.behaviours.appliances.TaskBehaviour;

public class CoolingTask extends TaskBehaviour<ACAgent> {

    private final long delayMillis = 1000;
    private final double coolingRate;
    private final double targetTemperature;

    private boolean awaitingDelay = false;
    private long nextWakeTime = 0;

    public CoolingTask(ACAgent agent, double coolingRate, double targetTemperature) {
        super(agent, "cooling-task", 1, true, true);
        this.coolingRate = coolingRate;
        this.targetTemperature = targetTemperature;
        this.powerUsage = agent.getActiveDraw();
        registerError("cooling-error", new TaskBehaviour<ACAgent>(agent, "cooling-error-resolver", priority, false, false) {
            @Override
            protected boolean execute() {
                agent.getLogger().info("Cooling task: error has been successfully resolved");
                return true;
            }
        });
    }

    private CoolingTask(ACAgent agent, int priority, double coolingRate, double targetTemperature) {
        super(agent, "cooling-task", priority, true, true);
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
            simulateError();
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
