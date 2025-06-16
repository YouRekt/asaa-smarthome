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
        super(agent, true, false);
        this.agent = agent;
        this.updateDelay = updateDelay;
        this.noninterruptibleStartTime = (long)(noninterruptibleStartPercent * fullWashTime);
        this.noninterruptibleEndTime = (long)(noninterruptibleEndPercent * fullWashTime);
        this.fullWashTime = fullWashTime;
        remainingWashTime = fullWashTime;
    }

    @Override
    protected void onPowerGranted() {
        super.onPowerGranted();
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
                    end(true);
                    agent.removeBehaviour(this);
                } else if (remainingWashTime <= noninterruptibleStartTime && remainingWashTime >= noninterruptibleEndTime && resumable) {
                    agent.getLogger().info("Wash Dishes Task entering an unpausable phase!");
                    resumable = false;
                } else if (remainingWashTime < noninterruptibleEndTime && !resumable) {
                    agent.getLogger().info("Dishwasher may be paused again");
                    resumable = true;
                }
            }
        };
        agent.addBehaviour(washBehaviour);
        agent.getLogger().info("Wash {} for {}ms", (remainingWashTime != fullWashTime ? "resumed" : "started"), remainingWashTime);
    }

    @Override
    public void pause(boolean isCfpCall) {
        if (!paused && washBehaviour != null) {
            remainingWashTime = Math.max(0, fullWashTime - System.currentTimeMillis() + washStartTime);
            agent.removeBehaviour(washBehaviour);
            washBehaviour = null;
            agent.getLogger().info("Wash paused, {}ms left", remainingWashTime);
        }
        super.pause(isCfpCall);
    }
}
