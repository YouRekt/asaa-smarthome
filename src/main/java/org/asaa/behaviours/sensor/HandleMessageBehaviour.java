package org.asaa.behaviours.sensor;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SensorAgent;

public abstract class HandleMessageBehaviour extends CyclicBehaviour {
    protected final SensorAgent sensorAgent;

    public HandleMessageBehaviour(SensorAgent sensorAgent) {
        super(sensorAgent);
        this.sensorAgent = sensorAgent;
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();

        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.REQUEST -> handleRequest(msg);
                case ACLMessage.SUBSCRIBE -> handleSubscribe(msg);
                case ACLMessage.CANCEL -> handleCancel(msg);
                default -> block();
            }
        } else {
            block();
        }
    }

    private void handleCancel(ACLMessage msg) {
        sensorAgent.getSubscribers().remove(msg.getSender());
    }

    private void handleSubscribe(ACLMessage msg) {
        sensorAgent.getSubscribers().add(msg.getSender());
    }

    protected abstract void handleRequest(ACLMessage msg);
}
