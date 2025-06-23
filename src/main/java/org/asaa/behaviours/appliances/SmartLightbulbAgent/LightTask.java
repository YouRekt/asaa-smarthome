package org.asaa.behaviours.appliances.SmartLightbulbAgent;

import org.asaa.agents.appliances.SmartLightbulbAgent;
import org.asaa.behaviours.appliances.tasks.PowerRequest;
import org.asaa.behaviours.appliances.tasks.TaskBehaviour;
import org.asaa.behaviours.appliances.tasks.TaskInfo;

public class LightTask extends TaskBehaviour<SmartLightbulbAgent> {
    private boolean firstCycle = true;
    private long fullTime;
    private long remainingTime;
    private long startTime;

    public LightTask(SmartLightbulbAgent agent, long duration) {
        super(agent, "light-task", agent.getPriority(), true, true, TaskInfo.Type.USER_COMFORT, PowerRequest.Urgency.LOW, false);
        this.fullTime = duration;
        this.remainingTime = duration;
    }

    @Override
    protected boolean execute() {
        estimatedRemainingTime = remainingTime;
        if (remainingTime <= 0) {
            agent.getLogger().info("Turning off light");
            return true;
        } else if (firstCycle) {
            agent.getLogger().info("Light {} for {}ms", (remainingTime != fullTime ? "resumed" : "started"), remainingTime);
            startTime = System.currentTimeMillis();
            fullTime = remainingTime;
            firstCycle = false;
        }

        remainingTime = Math.max(0, fullTime - System.currentTimeMillis() + startTime);
        return false;
    }

    @Override
    protected void onPause() {
        remainingTime = Math.max(0, fullTime - System.currentTimeMillis() + startTime);
        firstCycle = true;
    }
}
