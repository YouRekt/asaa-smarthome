package org.asaa.behaviours.appliances.CoffeeMachineAgent;

import org.asaa.agents.appliances.CoffeeMachineAgent;
import org.asaa.behaviours.appliances.tasks.TaskBehaviour;
import org.asaa.behaviours.appliances.tasks.TaskInfo;

public class MakeCoffeeTask extends TaskBehaviour<CoffeeMachineAgent> {
    private final long endTime;

    public MakeCoffeeTask(CoffeeMachineAgent agent, long duration) {
        super(agent, "make-coffee-task", 1, false, false, TaskInfo.Type.USER_COMFORT);
        this.endTime = System.currentTimeMillis() + duration;
    }

    @Override
    protected boolean execute() {
        estimatedRemainingTime = endTime - System.currentTimeMillis();
        if (endTime <= System.currentTimeMillis()) {
            agent.getLogger().info("Coffee has been made! Enjoy!");
            return true;
        }

        return false;
    }
}