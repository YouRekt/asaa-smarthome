package org.asaa.behaviours.coordinator;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.sniffer.Message;
import org.asaa.agents.coordinators.CoordinatorAgent;

import java.util.*;

public class PowerNegotiationBehaviour extends CyclicBehaviour {
    private final CoordinatorAgent coordinatorAgent;
    private final ACLMessage cfpMessage;
    private final long cfpResponseTimeout = 5000;
    private final int cfpShortage;
    private final int cfpRequiredPower;
    private final int cfpSenderPriority;
    private int cfpReceivedResponses = 0;
    private int cfpSentProposals = 0;
    private int cfpRelievedPower = 0;
    private boolean cfpProposalsProcessed = false;
    private final Map<AID, ProposalData> cfpProposals = new HashMap<>();
    private final WakerBehaviour cfpTimeoutBehaviour = new WakerBehaviour(myAgent, cfpResponseTimeout) {
        @Override
        protected void onWake() {
            CoordinatorAgent.getLogger().warn("Reply-by for cfp expired, received {} responses, sent {}", cfpReceivedResponses, cfpSentProposals);
            if (cfpProposalsProcessed)
                cfpRespondToSender();
            else
                cfpProcessProposals();
        }
    };

    public PowerNegotiationBehaviour(CoordinatorAgent coordinatorAgent, ACLMessage cfpMessage, int cfpShortage, int cfpRequiredPower, int cfpSenderPriority) {
        super(coordinatorAgent);

        this.coordinatorAgent = coordinatorAgent;
        this.cfpMessage = cfpMessage;
        this.cfpShortage = cfpShortage;
        this.cfpRequiredPower = cfpRequiredPower;
        this.cfpSenderPriority = cfpSenderPriority;
    }

    @Override
    public void onStart() {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setConversationId("power-relief");
        cfp.setContent(Integer.toString(cfpShortage));
        cfp.setReplyByDate(new Date(System.currentTimeMillis() + cfpResponseTimeout));
        cfpSentProposals = (int)coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream()).filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream()).filter(a -> !a.equals(cfpMessage.getSender())).count();
        coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream().filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream())).filter(a -> !a.equals(cfpMessage.getSender())).forEach(cfp::addReceiver);
        coordinatorAgent.send(cfp);

        coordinatorAgent.addBehaviour(cfpTimeoutBehaviour);
    }

    @Override
    public void action() {
        MessageTemplate mt = new MessageTemplate((MessageTemplate.MatchExpression) msg -> msg.getConversationId() != null &&
                msg.getConversationId().equals("power-relief") ||
                msg.getConversationId().equals("disable-passive-cfp") ||
                msg.getConversationId().equals("disable-active-cfp"));

        final ACLMessage msg = coordinatorAgent.receive(mt);

        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.REFUSE -> handleRefuse(msg);
                case ACLMessage.PROPOSE -> handlePropose(msg);
                case ACLMessage.INFORM -> handleInform(msg);
            }
        } else {
            block();
        }
    }

    protected void handleRefuse(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
                cfpReceivedResponses++;
                if (cfpReceivedResponses >= cfpSentProposals && !cfpProposalsProcessed) {
                    cfpProcessProposals();
                }
                break;
            default:
                break;
        }
    }

    protected void handlePropose(ACLMessage msg) {
        String[] msgParts = msg.getContent().split(",");
        switch (msg.getConversationId()) {
            case "power-relief":
                cfpReceivedResponses++;
                cfpProposals.put(msg.getSender(), new ProposalData(Integer.parseInt(msgParts[0]), Integer.parseInt(msgParts[1])));
                if (cfpReceivedResponses >= cfpSentProposals && !cfpProposalsProcessed) {
                    cfpProcessProposals();
                }
                break;
            default:
                break;
        }
    }

    protected void handleInform(ACLMessage msg) {
        int returnedPower;
        switch (msg.getConversationId()) {
            case "disable-passive-cfp":
            case "disable-active-cfp":
                cfpReceivedResponses++;
                returnedPower = Integer.parseInt(msg.getContent());
                coordinatorAgent.environmentService.modifyPowerConsumption(-returnedPower);
                if (cfpReceivedResponses >= cfpSentProposals) {
                    cfpRespondToSender();
                }
                break;
            default:
                break;
        }
    }

    private void cfpProcessProposals() {
        cfpProposalsProcessed = true;
        coordinatorAgent.removeBehaviour(cfpTimeoutBehaviour);
        cfpReceivedResponses = 0;
        cfpSentProposals = 0;
        List<Map.Entry<AID, ProposalData>> sortedProposals = new ArrayList<>(cfpProposals.entrySet());
        sortedProposals.sort(Comparator.comparingInt(e -> e.getValue().getPriority()));
        cfpRelievedPower = 0;
        Set<AID> accepted = new HashSet<>();
        for (var proposal : sortedProposals) {
            if (cfpRelievedPower >= cfpShortage)
                break;
            if (proposal.getValue().getPriority() > cfpSenderPriority) {
                CoordinatorAgent.getLogger().warn("Proposal of {} is higher prio ({}) than {} ({}), skipping", proposal.getKey().getLocalName(), proposal.getValue().getPriority(), cfpMessage.getSender().getLocalName(), cfpSenderPriority);
                break;
            }
            cfpRelievedPower += proposal.getValue().getCanFree();
            accepted.add(proposal.getKey());
        }

        if (cfpRelievedPower < cfpShortage) {
            ACLMessage reply = cfpMessage.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("Enable " + (cfpMessage.getConversationId().equals("enable-passive") ? "passive" : "active") + " refused even after proposed relief - " + cfpRequiredPower + "W");
            coordinatorAgent.send(reply);
        } else {
            for (var proposal : cfpProposals.entrySet()) {
                ACLMessage proposalReply = new ACLMessage(accepted.contains(proposal.getKey()) ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL);
                proposalReply.addReceiver(proposal.getKey());
                proposalReply.setConversationId("power-relief");
                proposalReply.setContent(Integer.toString(proposal.getValue().getCanFree()));
                proposalReply.setReplyByDate(new Date(System.currentTimeMillis() + cfpResponseTimeout));
                coordinatorAgent.send(proposalReply);
                cfpSentProposals += accepted.contains(proposal.getKey()) ? 1 : 0;

                coordinatorAgent.addBehaviour(cfpTimeoutBehaviour);
            }
        }
    }

    private void cfpRespondToSender() {
        coordinatorAgent.removeBehaviour(cfpTimeoutBehaviour);
        coordinatorAgent.environmentService.modifyPowerConsumption(+cfpRequiredPower);
        ACLMessage reply = cfpMessage.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        reply.setContent("Enable " + (cfpMessage.getConversationId().equals("enable-passive") ? "passive" : "active") + " accepted after relief - " + cfpRequiredPower + "W (shortage: " + cfpShortage + "W, relief " + cfpRelievedPower + "W)");
        coordinatorAgent.send(reply);
        coordinatorAgent.removeBehaviour(this);
    }

    private class ProposalData {
        private final int canFree;
        private final int priority;

        ProposalData(int canFree, int priority) {
            this.canFree = canFree;
            this.priority = priority;
        }

        int getCanFree() {
            return canFree;
        }

        int getPriority() {
            return priority;
        }
    }
}
