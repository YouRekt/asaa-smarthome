package org.asaa.agents.sensors;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SensorAgent;

import java.util.concurrent.atomic.AtomicInteger;

public class TemperatureSensorAgent extends SensorAgent {

    private final AtomicInteger alertTimeout = new AtomicInteger(60);
    private final int timeout = 60;

    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                if (getTemperature() > 21.0) {
                    if (alertTimeout.get() == 60) {
                        logger.info("The ambient temperature is too high! Suggesting to turn on the AC");
                        handleTrigger();
                    }
                    if (alertTimeout.decrementAndGet() == 0) {
                        alertTimeout.set(timeout);
                    }
                }
            }
        });
    }

    private Double getTemperature() {
        return (Double) getArea().getAttribute("temperature");
    }

    private String getTemperatureString() {
        return String.valueOf(getTemperature());
    }

    @Override
    protected String responseMsgContent() {
        return getTemperatureString();
    }

    @Override
    protected void handleTrigger() {
        final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(getTemperatureString());
        subscribers.forEach(msg::addReceiver);
        send(msg);
    }
}
