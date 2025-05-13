package org.asaa.agents.sensors;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.SensorAgent;

public final class TemperatureSensorAgent extends SensorAgent {

    private Double getTemperature() {
        return (Double) getArea().getAttribute("temperature");
    }

    private String getTemperatureString() {
        return String.valueOf(getTemperature());
    }

    @Override
    protected String responseDefaultMsgContent() {
        return getTemperatureString();
    }

    @Override
    protected void handleTrigger() {
        final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(getTemperatureString());
        subscribers.forEach(msg::addReceiver);
        sendMessage(msg);
    }
}
