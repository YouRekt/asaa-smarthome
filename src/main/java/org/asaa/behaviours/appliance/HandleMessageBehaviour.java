package org.asaa.behaviours.appliance;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.BaseMessageHandler;

public abstract class HandleMessageBehaviour extends BaseMessageHandler {
    protected final SmartApplianceAgent smartApplianceAgent;

    public HandleMessageBehaviour(SmartApplianceAgent smartApplianceAgent) {
        super(smartApplianceAgent);

        this.smartApplianceAgent = smartApplianceAgent;
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
    protected void handleAgree(ACLMessage msg) {
        logger.info("Subscribed to {}", msg.getSender().getLocalName());
        AID sensor = msg.getSender();
        String sensorType = sensor.getClass().getSimpleName();
        smartApplianceAgent.subscribeSensor(sensor,sensorType);
    }

    @Override
    protected void handleRefuse(ACLMessage msg) {
        logger.warn("{} has refused the subscription", msg.getSender().getLocalName());
    }

    @Override
    protected void handleFailure(ACLMessage msg) {
        logger.warn("{} has failed", msg.getSender().getLocalName());
    }
}
