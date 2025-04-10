package org.asaa.agents;

import jade.lang.acl.ACLMessage;
import org.asaa.environment.Area;

public class TemperatureSensorAgent extends SensorAgent {
    @Override
    protected void respondToRequest(ACLMessage msg) {
        System.out.printf("[%s] %s sent a request%n",getLocalName(), msg.getSender().getLocalName());
        Area area = getMyArea();
        Double temperature = (Double) area.getAttribute("temperature");

        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(String.valueOf(temperature));
        send(reply);
    }
}
