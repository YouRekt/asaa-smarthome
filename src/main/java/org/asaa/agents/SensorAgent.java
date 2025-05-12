package org.asaa.agents;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.asaa.behaviours.sensor.HandleMessageBehaviour;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class SensorAgent extends PhysicalAgent {
    protected List<AID> subscribers = new ArrayList<>();

    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleInform(ACLMessage msg) {
                trigger();
            }

            @Override
            protected void handleRequest(ACLMessage msg) {
                agentCommunicationController.sendMessage(getName(), String.format("Responding to %s's request", msg.getSender().getLocalName()));
                logger.info("Responding to {}'s request", msg.getSender().getLocalName());
                respond(msg);
            }
        });
    }
}
