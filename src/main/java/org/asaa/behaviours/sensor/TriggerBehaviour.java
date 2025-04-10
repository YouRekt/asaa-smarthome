package org.asaa.behaviours.sensor;

import jade.core.behaviours.OneShotBehaviour;
import org.asaa.agents.SensorAgent;

public class TriggerBehaviour extends OneShotBehaviour {
    private final SensorAgent sensorAgent;

    public TriggerBehaviour(SensorAgent sensorAgent) {
        super(sensorAgent);
        this.sensorAgent = sensorAgent;
    }

    @Override
    public void action() {
        sensorAgent.getSubscribers().forEach(s -> {
            sensorAgent.ha
        });
    }
}
