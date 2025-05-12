package org.asaa.behaviours.appliance;

import jade.core.behaviours.OneShotBehaviour;
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
                            !(msg.getConversationId().equals("enable-passive") || msg.getConversationId().equals("enable-active") || msg.getConversationId().equals("power-relief")))) {
                smartApplianceAgent.logger.warn("{} is not enabled. Ignoring message {} {}", smartApplianceAgent.getLocalName(), msg.getConversationId(), msg.getContent());
                return;
            }
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        switch ((msg.getConversationId() == null ? " " : msg.getConversationId())) {
            case "enable-callback":
                smartApplianceAgent.logger.info("Received enable-callback message");
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleAgree(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "enable-passive":
                smartApplianceAgent.logger.info("Coordinator AGREED: {}", msg.getContent());
                smartApplianceAgent.setEnabled(true);
                break;
            case "enable-active":
                smartApplianceAgent.logger.info("Coordinator AGREED: {}", msg.getContent());
                smartApplianceAgent.setWorking(true);
                String replyWith = msg.getInReplyTo();
                Runnable callback = smartApplianceAgent.onPowerGrantedCallbacks.remove(replyWith);
                if (callback != null) {
                    smartApplianceAgent.logger.debug("Callback triggered: {}", callback);
                    callback.run();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleRefuse(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "enable-passive":
                smartApplianceAgent.logger.warn("Coordinator REFUSED: convId=enable-passive");
                break;
            case "enable-active":
                smartApplianceAgent.logger.warn("Coordinator REFUSED: convId=enable-active");
                String replyWith = msg.getInReplyTo();
                Runnable callback = smartApplianceAgent.onPowerGrantedCallbacks.remove(replyWith);
                if (callback != null) {
                    smartApplianceAgent.logger.debug("Callback cancelled tied with request {}", replyWith);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleCfp(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
                if (smartApplianceAgent.isCfpInProgress()) {
                    smartApplianceAgent.getPendingCfpQueue().add(msg);
                    smartApplianceAgent.logger.warn("Deferring power relief CFP from {} because another is in progress", msg.getSender().getLocalName());
                    return;
                }
                smartApplianceAgent.setCfpInProgress(true);
                int canFree = 0, prio = smartApplianceAgent.getPriority();
                if (smartApplianceAgent.isWorking()) {
                    if (smartApplianceAgent.isInterruptible()) {
                        canFree = smartApplianceAgent.getActiveDraw();
                        if (smartApplianceAgent.isFreezable()) {
                            prio = smartApplianceAgent.getPriority() % 100;
                        }
                    } else {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        smartApplianceAgent.sendMessage(reply);
                        allowNextCfp();
                        return;
                    }
                } else if (smartApplianceAgent.isEnabled()) {
                    canFree = smartApplianceAgent.getIdleDraw();
                    prio = smartApplianceAgent.getPriority() % 100 + 200;
                }

                smartApplianceAgent.logger.info("Power relief CFP: {} canFree={}W, prio={}, isWorking={}", smartApplianceAgent.getLocalName(), canFree, prio, (smartApplianceAgent.isWorking() ? "yes" : "no"));
                ACLMessage propose = msg.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(canFree + "," + prio);
                smartApplianceAgent.sendMessage(propose);
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleAcceptProposal(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
                int freed = Integer.parseInt(msg.getContent());
                if (smartApplianceAgent.isWorking()) {
                    smartApplianceAgent.addBehaviour(new RelinquishPowerBehaviour(smartApplianceAgent, freed, "disable-active-cfp"));
                } else {
                    smartApplianceAgent.addBehaviour(new RelinquishPowerBehaviour(smartApplianceAgent, freed, "disable-passive-cfp"));
                }
                allowNextCfp();
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleRejectProposal(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
                allowNextCfp();
                break;
            default:
                break;
        }
    }

    private void allowNextCfp() {
        smartApplianceAgent.setCfpInProgress(false);
        if (!smartApplianceAgent.getPendingCfpQueue().isEmpty()) {
            ACLMessage nextCfp = smartApplianceAgent.getPendingCfpQueue().poll();
            smartApplianceAgent.addBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    handleCfp(nextCfp);
                }
            });
        }
    }
}
