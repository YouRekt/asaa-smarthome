package org.asaa.behaviours.coordinators.CoordinatorAgent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.behaviours.appliances.tasks.NegotiationResult;
import org.asaa.behaviours.appliances.tasks.PowerNegotiationCoordinator;
import org.asaa.behaviours.appliances.tasks.PowerProposal;
import org.asaa.behaviours.appliances.tasks.PowerRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PowerNegotiationBehaviour extends Behaviour {
    private final CoordinatorAgent agent;
    private final ACLMessage requestMessage;
    private final PowerRequest powerRequest;
    private final int powerShortage;
    private final Runnable allowNextCfp;
    private int powerRelieved;
    private final PowerNegotiationCoordinator powerNegotiationCoordinator = new PowerNegotiationCoordinator();
    @Getter
    private final List<PowerProposal> proposals = new ArrayList<>();
    private final long responseTimeout = 5000L;
    @Getter
    @Setter
    private State state = State.collectProposals;
    private int sentMessages;
    private int receivedMessages;

    private final WakerBehaviour timeoutBehaviour = new WakerBehaviour(myAgent, responseTimeout) {
        @Override
        protected void onWake() {
            agent.getLogger().warn("Reply-by for cfp expired, received {} responses, sent {}", receivedMessages, sentMessages);
            if (state == State.collectProposals)
                state = State.processProposals;
        }
    };

    public PowerNegotiationBehaviour(CoordinatorAgent agent, ACLMessage requestMessage, PowerRequest powerRequest, int powerShortage, Runnable allowNextCfp) {
        this.agent = agent;
        this.requestMessage = requestMessage;
        this.powerRequest = powerRequest;
        this.powerShortage = powerShortage;
        this.allowNextCfp = allowNextCfp;
    }

    @Override
    public void onStart() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            cfp.setContent(mapper.writeValueAsString(powerRequest));
            cfp.setConversationId("power-relief");
            cfp.setReplyByDate(new Date(System.currentTimeMillis() + responseTimeout));
            sentMessages = (int) agent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream()).filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream()).filter(a -> !a.equals(requestMessage.getSender())).count();
            agent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream().filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream())).filter(a -> !a.equals(requestMessage.getSender())).forEach(cfp::addReceiver);
            agent.sendMessage(cfp);
            agent.getLogger().info("{}: Current state = {}", this.getClass().getSimpleName(), state);
            state = State.collectProposals;
            agent.addBehaviour(timeoutBehaviour);
        } catch (JsonProcessingException e) {
            agent.getLogger().error("{}@onStart: JsonProcessingException {}", this.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void action() {
        switch (state) {
            case collectProposals:
                if (receivedMessages >= sentMessages) {
                    state = State.processProposals;
                    break;
                }
                block();
                break;
            case processProposals:
                agent.getLogger().info("{}: Current state = {}", this.getClass().getSimpleName(), state);
                agent.removeBehaviour(timeoutBehaviour);
                sentMessages = 0;
                receivedMessages = 0;
                powerRelieved = 0;
                NegotiationResult result = powerNegotiationCoordinator.negotiatePowerAllocation(powerRequest, proposals, agent.environmentService.getPowerAvailability());
                switch (result.getOutcome()) {
                    case ACCEPT:
                        agent.getLogger().info("{}@processProposals ACCEPT", this.getClass().getSimpleName());
                        for (var proposal : proposals) {
                            powerRelieved += result.getAcceptedProposals().contains(proposal) ? proposal.getPowerAmount() : 0;
                            ACLMessage proposalReply = new ACLMessage(result.getAcceptedProposals().contains(proposal) ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL);
                            proposalReply.addReceiver(new AID(proposal.getAgentId(), AID.ISLOCALNAME));
                            proposalReply.setConversationId("power-relief");
                            proposalReply.setReplyByDate(new Date(System.currentTimeMillis() + responseTimeout));
                            agent.sendMessage(proposalReply);
                            sentMessages += proposalReply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL ? 1 : 0;
                        }
                        state = State.waitForConfirmation;
                        break;
                    case REFUSE:
                        agent.getLogger().info("{}@processProposals REFUSE", this.getClass().getSimpleName());
                        ACLMessage proposalsReply = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                        proposals.forEach(p -> proposalsReply.addReceiver(new AID(p.getAgentId(), AID.ISLOCALNAME)));
                        proposalsReply.setConversationId("power-relief");
                        agent.sendMessage(proposalsReply);
                        respondToOriginalRequest(false);
                        break;
                    case SCHEDULE_LATER:
                        agent.getLogger().error("{}@action: Not yet implemented", this.getClass().getSimpleName());
                        break;
                    default:
                        break;
                }
                agent.getLogger().info("{}: Current state = {}", this.getClass().getSimpleName(), state);
                break;
            case waitForConfirmation:
                if (receivedMessages >= sentMessages) {
                    respondToOriginalRequest(true);
                    state = State.finished;
                    break;
                }
                block();
                break;
            case finished:
                break;
            default:
                agent.getLogger().error("{}@action: Unknown state {}", this.getClass().getSimpleName(), state);
                break;
        }
    }

    @Override
    public boolean done() {
        return state == State.finished;
    }

    @Override
    public int onEnd() {
        allowNextCfp.run();
        return super.onEnd();
    }

    public void incrementReceivedMessages() {
        receivedMessages++;
    }

    private void respondToOriginalRequest(boolean success) {
        agent.removeBehaviour(timeoutBehaviour);
        if (success)
            agent.environmentService.modifyPowerConsumption(+powerRequest.getPowerAmount());
        ACLMessage reply = requestMessage.createReply();
        reply.setPerformative(success ? ACLMessage.AGREE : ACLMessage.REFUSE);
        reply.setContent(success ? "Enable " + (requestMessage.getConversationId().equals("enable-passive") ? "passive" : "active") + " accepted after relief - " + powerRequest.getPowerAmount() + "W (shortage: " + powerShortage + "W, relief " + powerRelieved + "W)" : "Enable " + (requestMessage.getConversationId().equals("enable-passive") ? "passive" : "active") + " refused even after proposed relief - " + powerRequest.getPowerAmount() + "W");
        agent.sendMessage(reply);
        state = State.finished;
    }

    public enum State {
        collectProposals, processProposals, waitForConfirmation, finished
    }
}
