package org.asaa.behaviours.appliances.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.behaviours.appliances.tasks.PowerProposal;
import org.asaa.behaviours.appliances.tasks.PowerRequest;
import org.asaa.behaviours.appliances.tasks.TaskBehaviour;
import org.asaa.behaviours.base.BaseMessageHandlerBehaviour;
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
                if (agent.getCurrentTaskBehaviour() != null) {
                    agent.getCurrentTaskBehaviour().pause(false);
                }
                break;
            case "resume-task":
                if (agent.getCurrentTaskBehaviour() != null) {
                    agent.getCurrentTaskBehaviour().resume();
                }
                break;
            case "interrupt-task":
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
            case "disable":
                if (agent.getCurrentTaskBehaviour() != null) {
                    agent.getLogger().error("Task is not null");
                    return;
                }
                agent.addBehaviour(new RelinquishPowerBehaviour(agent, agent.getIdleDraw(), "disable-passive"));
                break;
            case " ":
                agent.getLogger().error("CONVERSATION ID WAS NULL !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                break;
            case "power-released":
                agent.getLogger().info("Received power-released, attempting to unpause");
                if (agent.getCurrentTaskBehaviour() != null && agent.getCurrentTaskBehaviour().getStatus() == TaskBehaviour.Status.paused) {
                    agent.getCurrentTaskBehaviour().resume();
                    agent.getCurrentTaskBehaviour().restart();
                }
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
                agent.agentCommunicationController.sendError(agent.getLocalName(), "Passive power on refused", false);
                agent.getLogger().warn("Coordinator REFUSED enable-passive: {}", msg.getContent());
                break;
            case "enable-active-later":
                agent.getLogger().info("Coordinator REFUSED enable-active: reschedule at {}", msg.getContent());
                agent.addBehaviour(new WakerBehaviour(agent, Long.parseLong(msg.getContent())) {
                    @Override
                    protected void onWake() {
                        agent.addBehaviour(new RequestPowerBehaviour(agent, "enable-active", new PowerRequest(agent.getLocalName(), agent.getActiveDraw(), agent.getCurrentTaskBehaviour().getTaskInfo(), PowerRequest.Urgency.NORMAL, 10000, true
                        )));
                    }
                });
                agent.getCurrentTaskBehaviour().setStatus(TaskBehaviour.Status.waitingForPower);
                break;
            case "enable-active":
                agent.agentCommunicationController.sendError(agent.getLocalName(), "Active power on refused", false);
                agent.getLogger().warn("Coordinator REFUSED enable-active:{}", msg.getContent());
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
                if (agent.getCurrentTaskBehaviour() == null || agent.getCurrentTaskBehaviour().getStatus() != TaskBehaviour.Status.running) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    agent.sendMessage(reply);
                    allowNextCfp();
                    return;
                }
                ObjectMapper mapper = new ObjectMapper();
                try {
                    PowerProposal proposal = new PowerProposal(agent.getLocalName(), agent.getActiveDraw(), agent.getCurrentTaskBehaviour().getTaskInfo(), agent.getCurrentTaskBehaviour().isPausable() ? PowerProposal.Action.PAUSE : PowerProposal.Action.INTERRUPT, 0, agent.getCurrentTaskBehaviour().getTaskInfo().getEstimatedRemainingTime());
                    ACLMessage propose = msg.createReply();
                    propose.setPerformative(proposal.getPowerAmount() < 0 ? ACLMessage.REFUSE : ACLMessage.PROPOSE);
                    propose.setContent(mapper.writeValueAsString(proposal));
                    agent.getLogger().info("Sending proposal: {}W, Action={}, TimeToFree={}", proposal.getPowerAmount(), proposal.getAction().name(), proposal.getTimeToFree());
                    agent.sendMessage(propose);
                    if (proposal.getPowerAmount() < 0) {
                        allowNextCfp();
                    }
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
