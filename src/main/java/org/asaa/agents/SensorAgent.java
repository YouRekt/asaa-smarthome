package org.asaa.agents;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.asaa.behaviours.sensor.MessageHandlerBehaviour;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class SensorAgent extends PhysicalAgent {
    protected List<AID> subscribers = new ArrayList<>();

    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this) {
            @Override
            protected void handleInform(ACLMessage msg) {
                trigger();
            }

            @Override
            protected void handleRequest(ACLMessage msg) {
                logger.info("Responding to {}'s request", msg.getSender().getLocalName());
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(defaultRequestReplyMessage());
                reply.setConversationId("def-reply");
                sendMessage(reply);
            }
        });
    }

    protected abstract String defaultRequestReplyMessage();
}
