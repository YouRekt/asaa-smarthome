package org.asaa.behaviours.sensor;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.agents.SensorAgent;

public abstract class HandleMessageBehaviour extends CyclicBehaviour {
    protected final SensorAgent sensorAgent;
    protected final Logger logger;

    public HandleMessageBehaviour(SensorAgent sensorAgent) {
        super(sensorAgent);

        this.sensorAgent = sensorAgent;
        this.logger = LogManager.getLogger(sensorAgent.getLocalName());
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();

        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.REQUEST -> handleRequest(msg);
                case ACLMessage.SUBSCRIBE -> handleSubscribe(msg);
                case ACLMessage.CANCEL -> handleCancel(msg);
                case ACLMessage.INFORM -> handleInform(msg);
                default -> block();
            }
        } else {
            block();
        }
    }

    private void handleCancel(ACLMessage msg) {
        logger.info("Cancelled {}'s subscription", msg.getSender().getLocalName());
        sensorAgent.getSubscribers().remove(msg.getSender());
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        msg.setContent("cancelled");
        myAgent.send(reply);
    }

    private void handleSubscribe(ACLMessage msg) {
        logger.info("{} has subscribed", msg.getSender().getLocalName());
        sensorAgent.getSubscribers().add(msg.getSender());
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        msg.setContent("subscribed");
        myAgent.send(reply);
    }

    protected abstract void handleInform(ACLMessage msg);

    protected abstract void handleRequest(ACLMessage msg);
}
