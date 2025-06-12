package org.asaa.behaviours.appliances;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.BaseMessageHandlerBehaviour;
import org.asaa.util.Util;

public abstract class MessageHandlerBehaviour extends BaseMessageHandlerBehaviour {
    protected final SmartApplianceAgent agent;

    public MessageHandlerBehaviour(SmartApplianceAgent agent) {
        super(agent);

        this.agent = agent;
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();

        if (msg != null) {
            if (!agent.isEnabled() &&
                    (msg.getConversationId() == null ||
                            !(msg.getConversationId().equals("enable-passive") ||
                                    msg.getConversationId().equals("enable-active") ||
                                    msg.getConversationId().equals("power-relief") ||
                                    msg.getConversationId().equals("toggle") ||
                                    msg.getConversationId().equals("disable-passive") ||
                                    msg.getConversationId().equals("disable-active")))) {
                agent.getLogger().warn("{} is not enabled. Ignoring message perf={} convId={} content={}", agent.getLocalName(), Util.ConvertACLPerformativeToString(msg.getPerformative()), msg.getConversationId(), msg.getContent());
                agent.agentCommunicationController.sendError(agent.getName(), "Message sent to a disabled agent");
                return;
            }
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }

    @Override
    protected void handleRequest(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "pause-task":
                if (agent.getCurrentTask() != null) {
                    agent.getCurrentTask().pause();
                }
                break;
            case "resume-task":
                if (agent.getCurrentTask() != null) {
                    agent.getCurrentTask().resume();
                }
                break;
            case "interrupt-task":
                if (agent.getCurrentTask() != null) {
                    agent.getCurrentTask().interrupt();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        switch ((msg.getConversationId() == null ? " " : msg.getConversationId())) {
            case "enable-callback":
                agent.getLogger().info("Received enable-callback message");
                break;
            case "trigger":
                agent.trigger();
                break;
            case "toggle":
                agent.toggle();
            default:
                break;
        }
    }

    @Override
    protected void handleAgree(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "enable-passive":
                agent.getLogger().info("Coordinator AGREED: {}", msg.getContent());
                agent.setEnabled(true);
                break;
            case "enable-active":
                agent.getLogger().info("Coordinator AGREED: {}", msg.getContent());
                agent.setWorking(true);
                String replyWith = msg.getInReplyTo();
                Runnable callback = agent.onPowerGrantedCallbacks.remove(replyWith);
                if (callback != null) {
                    agent.getLogger().debug("Callback triggered: {}", callback);
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
                agent.getLogger().warn("Coordinator REFUSED enable-passive: {}", msg.getContent());
                agent.agentCommunicationController.sendError(agent.getName(), "Passive power on refused");
                break;
            case "enable-active":
                agent.getLogger().warn("Coordinator REFUSED enable-active:{}", msg.getContent());
                agent.agentCommunicationController.sendError(agent.getName(), "Active power on refused");
                String replyWith = msg.getInReplyTo();
                Runnable callback = agent.onPowerGrantedCallbacks.remove(replyWith);
                if (callback != null) {
                    agent.getLogger().warn("Callback cancelled tied with request {}", replyWith);
                    agent.agentCommunicationController.sendError(agent.getName(), "Callback action was cancelled: request " + replyWith);
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
                if (agent.isCfpInProgress()) {
                    agent.getPendingCfpQueue().add(msg);
                    agent.getLogger().warn("Deferring power relief CFP from {} because another is in progress", msg.getSender().getLocalName());
                    return;
                }
                agent.setCfpInProgress(true);
                int canFree = 0, prio = agent.getPriority();
                if (agent.isWorking()) {
                    if (agent.isInterruptible()) {
                        canFree = agent.getActiveDraw();
                        if (agent.isFreezable()) {
                            prio = agent.getPriority() % 100;
                        }
                        agent.getLogger().warn("Power relief CFP: Currently working and interruptible, will interrupt my current action on accept-proposal");
                    } else {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        agent.sendMessage(reply);
                        allowNextCfp();
                        return;
                    }
                } else if (agent.isEnabled()) {
                    canFree = agent.getIdleDraw();
                    prio = agent.getPriority() % 100 + 200;
                }

                agent.getLogger().info("Power relief CFP: {} canFree={}W, prio={}, isWorking={}", agent.getLocalName(), canFree, prio, (agent.isWorking() ? "yes" : "no"));
                ACLMessage propose = msg.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(canFree + "," + prio);
                agent.sendMessage(propose);
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
                if (agent.isWorking()) {
                    agent.addBehaviour(new RelinquishPowerBehaviour(agent, freed, "disable-active-cfp"));
                } else {
                    agent.addBehaviour(new RelinquishPowerBehaviour(agent, freed, "disable-passive-cfp"));
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
        agent.setCfpInProgress(false);
        if (!agent.getPendingCfpQueue().isEmpty()) {
            ACLMessage nextCfp = agent.getPendingCfpQueue().poll();
            agent.addBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    handleCfp(nextCfp);
                }
            });
        }
    }
}
