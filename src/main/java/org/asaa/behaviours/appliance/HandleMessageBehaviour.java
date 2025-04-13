package org.asaa.behaviours.appliance;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.agents.SmartApplianceAgent;

public abstract class HandleMessageBehaviour extends CyclicBehaviour {
    protected final SmartApplianceAgent smartApplianceAgent;
    protected final Logger logger;

    public HandleMessageBehaviour(SmartApplianceAgent smartApplianceAgent) {
        super(smartApplianceAgent);

        this.smartApplianceAgent = smartApplianceAgent;
        this.logger = LogManager.getLogger(smartApplianceAgent.getLocalName());
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
        logger.info("Subscribed to {}", msg.getSender().getLocalName());
        smartApplianceAgent.getFollowedSensors().add(msg.getSender());
    }

    private void handleRefuse(ACLMessage msg) {
        logger.warn("{} has refused the subscription", msg.getSender().getLocalName());
    }

    protected abstract void handleInform(ACLMessage msg);

    private void handleFailure(ACLMessage msg) {
        logger.warn("{} has failed", msg.getSender().getLocalName());
    }
}
