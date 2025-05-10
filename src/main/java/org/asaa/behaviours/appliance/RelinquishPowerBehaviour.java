package org.asaa.behaviours.appliance;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.agents.SmartApplianceAgent;

public class RelinquishPowerBehaviour extends OneShotBehaviour {
    private final SmartApplianceAgent smartApplianceAgent;
    private final int amount;
    private final String convId;
    private final Logger logger;

    public RelinquishPowerBehaviour(SmartApplianceAgent smartApplianceAgent, int amount, String convId) {
        super(smartApplianceAgent);
        this.smartApplianceAgent = smartApplianceAgent;
        this.amount = amount;
        this.convId = convId;
        logger = LogManager.getLogger(smartApplianceAgent.getLocalName());
    }

    @Override
    public void action() {
        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
        inform.addReceiver(smartApplianceAgent.coordinatorAgent);
        inform.setConversationId(convId);
        inform.setContent(Integer.toString(amount));
        smartApplianceAgent.send(inform);
        logger.info("Sent INFORM for {}W, convId={}", amount, convId);
        if (convId.equals("disable-active") || convId.equals("disable-active-cfp"))
            smartApplianceAgent.setWorking(false);
        else if (convId.equals("disable-passive") || convId.equals("disable-passive-cfp"))
            smartApplianceAgent.setEnabled(false);
        else
            logger.warn("Invalid convId {}", convId);
    }
}
