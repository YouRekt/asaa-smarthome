package org.asaa.tasks.appliances.CoffeeMachineAgent;

import org.asaa.agents.appliances.CoffeeMachineAgent;
import org.asaa.behaviours.appliances.TaskBehaviour;

public class MakeCoffeeTask extends TaskBehaviour<CoffeeMachineAgent> {
    private final long endTime;

    public MakeCoffeeTask(CoffeeMachineAgent agent, long duration) {
        super(agent, "make-coffee-task", 1, false, false);
        this.endTime = System.currentTimeMillis() + duration;
    }

    @Override
    protected boolean execute() {
        if (endTime <= System.currentTimeMillis()) {
            agent.getLogger().info("Coffee has been made! Enjoy!");
            return true;
        }

        return false;
    }
}