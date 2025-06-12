package org.asaa.behaviours.sensor;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.SensorAgent;
import org.asaa.behaviours.BaseMessageHandlerBehaviour;

public class MessageHandlerBehaviour extends BaseMessageHandlerBehaviour {
    protected final SensorAgent agent;

    public MessageHandlerBehaviour(SensorAgent agent) {
        super(agent);

        this.agent = agent;
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
        agent.getLogger().info("Cancelled {}'s subscription", msg.getSender().getLocalName());
        agent.getSubscribers().remove(msg.getSender());
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        msg.setContent("cancelled");
        agent.sendMessage(reply);
    }

    @Override
    protected void handleSubscribe(ACLMessage msg) {
        agent.getLogger().info("{} has subscribed", msg.getSender().getLocalName());
        agent.getSubscribers().add(msg.getSender());
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        msg.setContent("subscribed");
        agent.sendMessage(reply);
    }
}
