package org.asaa.behaviours.appliances.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.behaviours.appliances.tasks.PowerProposal;
import org.asaa.behaviours.appliances.tasks.PowerProposalGenerator;
import org.asaa.behaviours.appliances.tasks.PowerRequest;
import org.asaa.behaviours.appliances.tasks.TaskBehaviour;
import org.asaa.behaviours.base.BaseMessageHandlerBehaviour;
import org.asaa.util.Util;

import java.util.LinkedList;
import java.util.Queue;

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
            if (!agent.isEnabled() && (msg.getConversationId() == null || !(msg.getConversationId().equals("enable-passive") || msg.getConversationId().equals("enable-active") || msg.getConversationId().equals("power-relief") || msg.getConversationId().equals("toggle") || msg.getConversationId().equals("disable-passive") || msg.getConversationId().equals("disable-active")))) {
                agent.getLogger().warn("{} is not enabled. Ignoring message perf={} convId={} content={}", agent.getLocalName(), Util.ConvertACLPerformativeToString(msg.getPerformative()), msg.getConversationId(), msg.getContent());
                agent.agentCommunicationController.sendError(agent.getLocalName(), "Message sent to a disabled agent", false);
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
            case "toggle":
                agent.handleToggle(msg.getContent());
                break;
            case "pause-task":
//                if (agent.getCurrentTask() != null) {
//                    agent.getCurrentTask().pause(false);
//                }
                if (agent.getCurrentTaskBehaviour() != null) {
                    agent.getCurrentTaskBehaviour().pause(false);
                }
                break;
            case "resume-task":
//                if (agent.getCurrentTask() != null) {
//                    agent.getCurrentTask().resume();
//                }
                if (agent.getCurrentTaskBehaviour() != null) {
                    agent.getCurrentTaskBehaviour().resume();
                }
                break;
            case "interrupt-task":
//                if (agent.getCurrentTask() != null) {
//                    agent.getCurrentTask().interrupt(false);
//                }
                if (agent.getCurrentTaskBehaviour() != null) {
                    agent.getCurrentTaskBehaviour().interrupt(false, false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        switch ((msg.getConversationId() == null ? " " : msg.getConversationId())) {
            case "retard":
//                if (agent.getCurrentTask() != null) {
//                    agent.getLogger().error("Task is not null");
//                    return;
//                }
                if (agent.getCurrentTaskBehaviour() != null) {
                    agent.getLogger().error("Task is not null");
                    return;
                }
                agent.addBehaviour(new RelinquishPowerBehaviour(agent, agent.getIdleDraw(), "disable-passive"));
                break;
            case " ":
                agent.getLogger().error("CONVERSATION ID WAS NULL !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                break;
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
//                String replyWith = msg.getInReplyTo();
//                Runnable callback = agent.onPowerGrantedCallbacks.remove(replyWith);
//                if (callback != null) {
//                    agent.getLogger().debug("Callback triggered: {}", callback);
//                    callback.run();
//                }
                agent.getCurrentTaskBehaviour().setStatus(TaskBehaviour.Status.powerGranted);
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
                agent.agentCommunicationController.sendError(agent.getLocalName(), "Passive power on refused", false);
                break;
            case "enable-active":
                agent.getLogger().warn("Coordinator REFUSED enable-active:{}", msg.getContent());
                agent.agentCommunicationController.sendError(agent.getLocalName(), "Active power on refused", false);
//                String replyWith = msg.getInReplyTo();
//                Runnable callback = agent.onPowerGrantedCallbacks.remove(replyWith);
//                if (callback != null) {
//                    agent.getLogger().warn("Callback cancelled tied with request {}", replyWith);
//                    agent.agentCommunicationController.sendError(agent.getLocalName(), "Callback action was cancelled: request " + replyWith);
//                }
                agent.getCurrentTaskBehaviour().setStatus(TaskBehaviour.Status.powerRefused);
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
                if (agent.getCurrentTaskBehaviour() == null || agent.getCurrentTaskBehaviour().done()) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    agent.sendMessage(reply);
                    return;
                }
                ObjectMapper mapper = new ObjectMapper();
                try {
                    PowerRequest powerRequest = mapper.readValue(msg.getContent(),  PowerRequest.class);
                    PowerProposalGenerator proposalGenerator = new PowerProposalGenerator();
                    PowerProposal proposal = proposalGenerator.generateProposal(agent.getAID().toString(), agent.getCurrentTaskBehaviour().getTaskInfo(), agent.getActiveDraw(), powerRequest);
                    ACLMessage propose = msg.createReply();
                    propose.setPerformative(ACLMessage.PROPOSE);
                    propose.setContent(mapper.writeValueAsString(proposal));
                    agent.getLogger().info("Sending proposal: {}W, Action={}, TimeToFree={}", proposal.getPowerAmount(), proposal.getAction().name(), proposal.getTimeToFree());
                    agent.sendMessage(propose);
                } catch (JsonProcessingException e) {
                    agent.getLogger().error("{}@handleCfp: JsonProcessingException {}", this.getClass().getSimpleName(), e.getMessage());
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleAcceptProposal(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
//                if (agent.getCurrentTask() != null && agent.getCurrentTask().isResumable()) {
//                    agent.getCurrentTask().pause(true);
                if (agent.getCurrentTaskBehaviour() != null && agent.getCurrentTaskBehaviour().isPausable()) {
                    agent.getCurrentTaskBehaviour().pause(true);
                } else if (agent.getCurrentTaskBehaviour() == null) {
                    agent.addBehaviour(new RelinquishPowerBehaviour(agent, agent.getIdleDraw(), "disable-passive-cfp"));
                } else {
                    agent.getLogger().error("{}@handleAcceptProposal: Could not free power!! Most likely task changed state to unpausable after sending proposal", this.getClass().getSimpleName());
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
