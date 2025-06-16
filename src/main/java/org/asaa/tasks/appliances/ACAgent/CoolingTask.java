package org.asaa.tasks.appliances.ACAgent;

import jade.core.behaviours.WakerBehaviour;
import org.asaa.agents.appliances.ACAgent;
import org.asaa.tasks.Task;

public final class CoolingTask extends Task {
    private final ACAgent agent;
    private final long delayMillis;
    private final double coolingRate;
    private final double targetTemperature;

    public CoolingTask(ACAgent agent, double coolingRate, double targetTemperature) {
        super(agent, true, true);

        this.agent = agent;
        this.coolingRate = coolingRate;
        this.targetTemperature = targetTemperature;
        this.delayMillis = 1000;
    }

    @Override
    protected void onPowerGranted() {
        super.onPowerGranted();
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
                if (paused || interrupted) {
                    agent.getLogger().warn("Cooling task tried to wake when interrupted or paused");
                    return;
                }

                agent.requestTemperature();
                awaitingWake = true;
            }
        });
    }

    @Override
    public void wake() {
        if (awaitingWake) {
            agent.getLogger().info("Cooling task received wake call");
            awaitingWake = false;
            if (agent.getCurrentTemperature() > targetTemperature) {
                coolAndWait();
            } else {
                end(true);
            }
        }
    }
}
