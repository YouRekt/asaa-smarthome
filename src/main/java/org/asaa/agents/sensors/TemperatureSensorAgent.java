package org.asaa.agents.sensors;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.SensorAgent;

public class TemperatureSensorAgent extends SensorAgent {

    private String getTemperatureString()
    {
        return String.valueOf(getArea().getAttribute("temperature"));
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
