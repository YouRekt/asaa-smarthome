package org.asaa.tasks.appliances.ACAgent;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import org.asaa.agents.appliances.ACAgent;
import org.asaa.behaviours.appliances.RelinquishPowerBehaviour;
import org.asaa.tasks.Task;

public final class CoolingTask extends Task {
    private final ACAgent agent;
    private final long delayMillis;
    private final double coolingRate;
    private final double targetTemperature;

    private boolean paused = false;
    private boolean interrupted = false;
    private boolean awaitingWake = false;

    public CoolingTask(ACAgent agent, double coolingRate, double targetTemperature) {
        this.agent = agent;
        this.coolingRate = coolingRate;
        this.targetTemperature = targetTemperature;
        this.delayMillis = 1000;
    }

    @Override
    public void start() {
        super.start(agent);
        coolAndWait();
    }

    private void coolAndWait() {
        if (paused || interrupted)
            return;

        agent.environmentService.getArea(agent.getAreaName()).setAttribute("temperature", (Double)agent.environmentService.getArea(agent.getAreaName()).getAttribute("temperature") - coolingRate);

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
            paused = true;
            agent.addBehaviour(new RelinquishPowerBehaviour(agent, agent.getActiveDraw(), "disable-active"));
        }
    }

    @Override
    public void resume() {
        if (paused) {
            paused = false;
            start();
        }
    }

    @Override
    public void interrupt() {
        interrupted = true;
        end(agent);
    }

    @Override
    public void wake() {
        if (awaitingWake) {
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
