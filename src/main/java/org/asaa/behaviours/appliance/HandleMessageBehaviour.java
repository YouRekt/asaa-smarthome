package org.asaa.behaviours.appliance;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.logging.log4j.Logger;

public abstract class HandleMessageBehaviour extends CyclicBehaviour {
    //TODO: add ApplianceAgent
    protected final Logger logger;

    public HandleMessageBehaviour(Agent a, Logger logger) {
        super(a);
        this.logger = logger;
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();

        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.INFORM -> handleInform(msg);
                case ACLMessage.REFUSE -> handleRefuse(msg);
                case ACLMessage.AGREE -> handleAgree(msg);
                case ACLMessage.FAILURE -> handleFailure(msg);
                default -> block();
            }
        } else {
            block();
        }
    }

    private void handleAgree(ACLMessage msg) {
        //TODO: add sensor to subscribed sensor list
    }

    private void handleRefuse(ACLMessage msg) {
        logger.warn("{} has refused the subscription", msg.getSender().getLocalName());
    }

    protected abstract void handleInform(ACLMessage msg);

    private void handleFailure(ACLMessage msg) {
        logger.warn("{} has failed", msg.getSender().getLocalName());
    }
}
