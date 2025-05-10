package org.asaa.behaviours.appliance;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.BaseMessageHandler;

public class HandleMessageBehaviour extends BaseMessageHandler {
    protected final SmartApplianceAgent smartApplianceAgent;

    public HandleMessageBehaviour(SmartApplianceAgent smartApplianceAgent) {
        super(smartApplianceAgent);

        this.smartApplianceAgent = smartApplianceAgent;
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();

        if (msg != null) {
            if (!smartApplianceAgent.isEnabled() &&
                    (msg.getConversationId() == null ||
                            !(msg.getConversationId().equals("enable-passive") || msg.getConversationId().equals("enable-active")))) {
                logger.warn("{} is not enabled. Ignoring message {} {}", smartApplianceAgent.getLocalName(), msg.getConversationId(), msg.getContent());
                return;
            }
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }

    @Override
    protected void handleAgree(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "enable-passive":
                logger.info("Coordinator AGREED: {}", msg.getContent());
                smartApplianceAgent.setEnabled(true);
                break;
            case "enable-active":
                logger.info("Coordinator AGREED: {}", msg.getContent());
                smartApplianceAgent.setWorking(true);
                String replyWith = msg.getInReplyTo();
                Runnable callback = smartApplianceAgent.onPowerGrantedCallbacks.remove(replyWith);
                if (callback != null) {
                    logger.debug("Callback triggered: {}", callback);
                    callback.run();
                }
                break;
            default:
                super.handleAgree(msg);
        }
    }

    @Override
    protected void handleRefuse(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "enable-passive":
                logger.warn("Coordinator REFUSED: convId=enable-passive");
                break;
            case "enable-active":
                logger.warn("Coordinator REFUSED: convId=enable-active");
                String replyWith = msg.getInReplyTo();
                Runnable callback = smartApplianceAgent.onPowerGrantedCallbacks.remove(replyWith);
                if (callback != null) {
                    logger.debug("Callback cancelled tied with request {}", replyWith);
                }
                break;
            default:
                super.handleAgree(msg);
        }
    }
}
