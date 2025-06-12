package org.asaa.tasks.appliances.DishwasherAgent;

import jade.core.behaviours.TickerBehaviour;
import org.asaa.agents.appliances.DishwasherAgent;
import org.asaa.tasks.Task;

public class WashDishesTask extends Task {
    private final DishwasherAgent agent;
    private TickerBehaviour washBehaviour;
    private final long updateDelay;
    private final long noninterruptibleStartTime;
    private final long noninterruptibleEndTime;
    private long fullWashTime;
    private long remainingWashTime;
    private long washStartTime;

    public WashDishesTask(DishwasherAgent agent, long updateDelay, double noninterruptibleStartPercent, double noninterruptibleEndPercent, long fullWashTime) {
        this.agent = agent;
        this.updateDelay = updateDelay;
        this.noninterruptibleStartTime = (long)(noninterruptibleStartPercent * fullWashTime);
        this.noninterruptibleEndTime = (long)(noninterruptibleEndPercent * fullWashTime);
        this.fullWashTime = fullWashTime;
        remainingWashTime = fullWashTime;
        resumable = true;
        interruptible = true;
    }

    @Override
    public void start() {
        agent.getLogger().info("WashDishesTask started");
        super.start(agent);
        performWash();
    }

    public void performWash() {
        if (remainingWashTime <= 0) {
            agent.getLogger().info("Nothing to do: no remaining time");
            return;
        }
        washStartTime = System.currentTimeMillis();
        fullWashTime = remainingWashTime;

        washBehaviour = new TickerBehaviour(agent, updateDelay) {
            @Override
            protected void onTick() {
                remainingWashTime = Math.max(0, fullWashTime - System.currentTimeMillis() + washStartTime);
//                agent.getLogger().info("Wash Dishes Task: {}ms remain", remainingWashTime);
                if (remainingWashTime <= 0) {
                    agent.getLogger().info("Wash complete!");
                    agent.environmentService.addPerformedTask();
                    end(agent);
                    agent.removeBehaviour(this);
                } else if (remainingWashTime <= noninterruptibleStartTime && remainingWashTime >= noninterruptibleEndTime && interruptible) {
                    agent.getLogger().info("Wash Dishes Task entering an uninterruptible phase!");
                    interruptible = false;
                } else if (remainingWashTime < noninterruptibleEndTime && !interruptible) {
                    agent.getLogger().info("Dishwasher may be interrupted again");
                    interruptible = true;
                }
            }
        };
        agent.addBehaviour(washBehaviour);
        agent.getLogger().info("Wash {} for {}ms", (remainingWashTime != fullWashTime ? "resumed" : "started"), remainingWashTime);
    }

    @Override
    public void pause() {
        if (!interruptible) {
            agent.getLogger().error("WashDishesTask tried to call pause when interruptible is false");
        }
        if (!paused && washBehaviour != null) {
            paused = true;
            remainingWashTime = Math.max(0, fullWashTime - System.currentTimeMillis() + washStartTime);
            agent.removeBehaviour(washBehaviour);
            washBehaviour = null;
            agent.getLogger().info("Wash paused, {}ms left", remainingWashTime);
        } else {
            agent.getLogger().warn("Wash Dishes Task was already paused");
        }
    }

    @Override
    public void resume() {
        if (paused) {
            paused = false;
            agent.requestStartTask(this);
        }
    }

    @Override
    public void interrupt() {
        if (!interruptible) {
            agent.getLogger().error("WashDishesTask tried to call interrupt when interruptible is false");
        }
        agent.getLogger().warn("Wash Dishes Task interrupted");
        interrupted = true;
        end(agent);
    }

    @Override
    public void wake() {

    }
}
