package org.asaa.behaviours.appliance;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.agents.SmartApplianceAgent;

public class RequestPowerBehaviour extends OneShotBehaviour {
    private final SmartApplianceAgent smartApplianceAgent;
    private final int amount;
    private final String convId;
    private final String replyWith;
    private final Logger logger;

    public RequestPowerBehaviour(SmartApplianceAgent smartApplianceAgent, int amount, String convId, String replyWith) {
        super(smartApplianceAgent);
        this.smartApplianceAgent = smartApplianceAgent;
        this.amount = amount;
        this.convId = convId;
        this.replyWith = replyWith;
        this.logger = LogManager.getLogger(smartApplianceAgent.getLocalName());
    }

    @Override
    public void action() {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.addReceiver(smartApplianceAgent.coordinatorAgent);
        cfp.setConversationId(convId);
        cfp.setContent(Integer.toString(amount));
        cfp.setReplyWith(replyWith);
        smartApplianceAgent.send(cfp);
        logger.info("Sent CFP for {}W, convId={}", amount, convId);
    }
}
