package org.asaa.tasks.appliances.ACAgent;

import jade.core.behaviours.WakerBehaviour;
import org.asaa.agents.appliances.ACAgent;
import org.asaa.behaviours.appliances.RelinquishPowerBehaviour;
import org.asaa.tasks.Task;

public final class CoolingTask extends Task {
    private final ACAgent agent;
    private final long delayMillis;
    private final double coolingRate;
    private final double targetTemperature;

    public CoolingTask(ACAgent agent, double coolingRate, double targetTemperature) {
        this.agent = agent;
        this.coolingRate = coolingRate;
        this.targetTemperature = targetTemperature;
        this.delayMillis = 1000;
        resumable = true;
        interruptible = true;
    }

    @Override
    public void start() {
        agent.getLogger().info("Cooling task started");
        super.start(agent);
        coolAndWait();
    }

    private void coolAndWait() {
        if (paused || interrupted)
            return;

        agent.environmentService.getArea(agent.getAreaName()).setAttribute("temperature", (Double)agent.environmentService.getArea(agent.getAreaName()).getAttribute("temperature") - coolingRate);
        agent.getLogger().info("Cooling task step: before - {}, after - {}", String.format("%.2f", agent.getCurrentTemperature()), String.format("%.2f", (Double)agent.environmentService.getArea(agent.getAreaName()).getAttribute("temperature")));

        agent.addBehaviour(new WakerBehaviour(agent, delayMillis) {
            @Override
            protected void onWake() {
                if (paused || interrupted)
                    return;

                agent.requestTemperature();
                awaitingWake = true;
            }
        });
    }

    @Override
    public void pause() {
        if (!paused) {
            agent.getLogger().info("Cooling task paused");
            paused = true;
            agent.addBehaviour(new RelinquishPowerBehaviour(agent, agent.getActiveDraw(), "disable-active"));
        } else {
            agent.getLogger().warn("Cooling task was already paused");
        }
    }

    @Override
    public void resume() {
        if (paused) {
            agent.getLogger().info("Cooling task resumed");
            paused = false;
            start();
        }
    }

    @Override
    public void interrupt() {
        agent.getLogger().warn("Cooling task interrupted");
        interrupted = true;
        end(agent);
    }

    @Override
    public void wake() {
        if (awaitingWake) {
            agent.getLogger().info("Cooling task received wake call");
            awaitingWake = false;
            if (agent.getCurrentTemperature() > targetTemperature) {
                coolAndWait();
            } else {
                end(agent);
            }
        }
    }

    @Override
    public boolean isResumable() {
        return true;
    }

    @Override
    public boolean isInterruptible() {
        return true;
    }
}
