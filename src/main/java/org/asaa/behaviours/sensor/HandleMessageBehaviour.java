package org.asaa.behaviours.sensor;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.SensorAgent;
import org.asaa.behaviours.BaseMessageHandler;

public class HandleMessageBehaviour extends BaseMessageHandler {
    protected final SensorAgent sensorAgent;

    public HandleMessageBehaviour(SensorAgent sensorAgent) {
        super(sensorAgent);

        this.sensorAgent = sensorAgent;
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();

        if (msg != null) {
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }

    @Override
    protected void handleCancel(ACLMessage msg) {
        sensorAgent.logger.info("Cancelled {}'s subscription", msg.getSender().getLocalName());
        sensorAgent.getSubscribers().remove(msg.getSender());
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        msg.setContent("cancelled");
        sensorAgent.sendMessage(reply,false);
    }

    @Override
    protected void handleSubscribe(ACLMessage msg) {
        sensorAgent.logger.info("{} has subscribed", msg.getSender().getLocalName());
        sensorAgent.getSubscribers().add(msg.getSender());
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        msg.setContent("subscribed");
        sensorAgent.sendMessage(reply,false);
    }
}
