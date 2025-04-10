package org.asaa.agents;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.asaa.environment.Area;

import java.util.List;

public class TemperatureSensorAgent extends SensorAgent {

    private String getTemperatureString()
    {
        Area area = getMyArea();
        Double temperature = (Double) area.getAttribute("temperature");

        return String.valueOf(temperature);
    }

    @Override
    protected void respond(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(getTemperatureString());
        send(reply);
    }

    @Override
    protected void handleTrigger(final List<AID> subscribers) {
        final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(getTemperatureString());
        subscribers.forEach(msg::addReceiver);
        send(msg);
    }
}
