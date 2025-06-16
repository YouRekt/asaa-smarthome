package org.asaa.tasks.appliances.SmartLightbultAgent;

import jade.core.behaviours.WakerBehaviour;
import org.asaa.agents.appliances.SmartLightbulbAgent;
import org.asaa.tasks.Task;

public final class LightTask extends Task {
    private final SmartLightbulbAgent agent;
    private WakerBehaviour lightBehaviour;
    private long fullTime;
    private long remainingTime;
    private long startTime;

    public LightTask(SmartLightbulbAgent agent, long fullTime) {
        super(agent, true, true);
        this.agent = agent;
        this.fullTime = fullTime;
        this.remainingTime = fullTime;
    }

    @Override
    protected void execute() {
        if (remainingTime < 0) {
            agent.getLogger().info("Nothing to do: no remaining time");
            end(true);
        }

        if (fullTime != 0) {
            startTime = System.currentTimeMillis();
            fullTime = remainingTime;

            lightBehaviour = new WakerBehaviour(agent, fullTime) {
                @Override
                protected void onWake() {
                    if (paused || interrupted) {
                        agent.getLogger().error("LightTask executed onWake while interrupted/paused!!!");
                        end(false);
                    }
                    agent.getLogger().info("Duration is up, turning off the light");
                    end(true);
                }
            };
            agent.addBehaviour(lightBehaviour);
        }

        agent.getLogger().info("I am currently on{}", fullTime != 0 ? " for " + fullTime : " indefinitely");
    }

    @Override
    public void pause(boolean isCfpCall) {
        if (!paused && resumable && lightBehaviour != null) {
            remainingTime = Math.max(0, fullTime - System.currentTimeMillis() + startTime);
            agent.removeBehaviour(lightBehaviour);
            lightBehaviour = null;
            agent.getLogger().info("Light turned off, {}ms left", remainingTime);
        }
        super.pause(isCfpCall);
    }

    @Override
    public void interrupt() {
        if (!interrupted && interruptible && lightBehaviour != null) {
            agent.removeBehaviour(lightBehaviour);
            lightBehaviour = null;
            agent.getLogger().info("Light turned off, {}ms of remaining time discarded", remainingTime);
        }
        super.interrupt();
    }
}
