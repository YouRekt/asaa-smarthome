package org.asaa.tasks.appliances.CoffeeMachineAgent;

import jade.core.behaviours.WakerBehaviour;
import org.asaa.agents.appliances.CoffeeMachineAgent;
import org.asaa.tasks.Task;

public final class MakeCoffeeTask extends Task {
    private final CoffeeMachineAgent agent;
    private final long duration = 10000;

    public MakeCoffeeTask(CoffeeMachineAgent agent) {
        this.agent = agent;
        resumable = false;
        interruptible = false;
    }

    @Override
    public void start() {
        agent.getLogger().info("MakeCoffeeTask started");
        super.start(agent);
        makeCoffee();
    }

    private void makeCoffee() {
        agent.getLogger().info("Making coffee");
        agent.addBehaviour(new WakerBehaviour(agent, duration) {
            @Override
            protected void onWake() {
                agent.getLogger().info("Coffee made! Enjoy");
                agent.environmentService.addPerformedTask();
                end(agent);
            }
        });
    }

    @Override
    public void pause() {
        agent.getLogger().warn("MakeCoffeeTask cannot be paused!");
    }

    @Override
    public void resume() {

    }

    @Override
    public void interrupt() {
        agent.getLogger().warn("MakeCoffeeTask cannot be interrupted!");
    }

    @Override
    public void wake() {
        agent.getLogger().warn("MakeCoffeeTask will never await wake!");
    }
}
