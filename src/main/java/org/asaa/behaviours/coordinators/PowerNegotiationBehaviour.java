package org.asaa.behaviours.coordinators;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.asaa.agents.coordinators.CoordinatorAgent;

import java.util.*;

public class PowerNegotiationBehaviour extends CyclicBehaviour {
    private final CoordinatorAgent coordinatorAgent;
    private final ACLMessage cfpMessage;
    private final long cfpResponseTimeout = 5000;
    private final int cfpShortage;
    private final int cfpRequiredPower;
    private final int cfpSenderPriority;
    private final Map<AID, ProposalData> cfpProposals = new HashMap<>();
    private int cfpReceivedResponses = 0;
    private int cfpSentProposals = 0;
    private int cfpRelievedPower = 0;
    private boolean cfpProposalsProcessed = false;

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
        cfpSentProposals = (int) coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream()).filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream()).filter(a -> !a.equals(cfpMessage.getSender())).count();
        coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream().filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream())).filter(a -> !a.equals(cfpMessage.getSender())).forEach(cfp::addReceiver);
        coordinatorAgent.send(cfp);

        coordinatorAgent.addBehaviour(cfpTimeoutBehaviour);
    }    private final WakerBehaviour cfpTimeoutBehaviour = new WakerBehaviour(myAgent, cfpResponseTimeout) {
        @Override
        protected void onWake() {
            CoordinatorAgent.getLogger().warn("Reply-by for cfp expired, received {} responses, sent {}", cfpReceivedResponses, cfpSentProposals);
            if (cfpProposalsProcessed) cfpRespondToSender();
            else cfpProcessProposals();
        }
    };

    @Override
    public void action() {
        MessageTemplate mt = new MessageTemplate((MessageTemplate.MatchExpression) msg -> msg.getConversationId() != null && msg.getConversationId().equals("power-relief") || msg.getConversationId().equals("disable-passive-cfp") || msg.getConversationId().equals("disable-active-cfp"));

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
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent(msg.getContent());
                coordinatorAgent.send(reply);
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
        sortedProposals.sort(Comparator.comparingInt(e -> e.getValue().priority()));
        cfpRelievedPower = 0;
        Set<AID> accepted = new HashSet<>();
        List<AID> awaitingCallback = new ArrayList<>();
        for (var proposal : sortedProposals) {
            if (cfpRelievedPower >= cfpShortage) break;
            if (proposal.getValue().priority() > cfpSenderPriority) {
                CoordinatorAgent.getLogger().warn("Proposal of {} is higher prio ({}) than {} ({}), skipping", proposal.getKey().getLocalName(), proposal.getValue().priority(), cfpMessage.getSender().getLocalName(), cfpSenderPriority);
                break;
            }
            cfpRelievedPower += proposal.getValue().canFree();
            accepted.add(proposal.getKey());
            if (proposal.getValue().priority() < 100) awaitingCallback.add(proposal.getKey());
        }
        coordinatorAgent.getAppliancesAwaitingCallback().put(cfpMessage.getSender(), awaitingCallback);

        for (var proposal : cfpProposals.entrySet()) {
            ACLMessage proposalReply = new ACLMessage(cfpRelievedPower >= cfpShortage ? (accepted.contains(proposal.getKey()) ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL) : ACLMessage.REJECT_PROPOSAL);
            proposalReply.addReceiver(proposal.getKey());
            proposalReply.setConversationId("power-relief");
            proposalReply.setContent(Integer.toString(proposal.getValue().canFree()));
            proposalReply.setReplyByDate(new Date(System.currentTimeMillis() + cfpResponseTimeout));
            coordinatorAgent.send(proposalReply);
            cfpSentProposals += accepted.contains(proposal.getKey()) ? 1 : 0;

        }
        if (cfpRelievedPower < cfpShortage) {
            ACLMessage reply = cfpMessage.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("Enable " + (cfpMessage.getConversationId().equals("enable-passive") ? "passive" : "active") + " refused even after proposed relief - " + cfpRequiredPower + "W");
            coordinatorAgent.send(reply);
            coordinatorAgent.removeBehaviour(this);
        } else coordinatorAgent.addBehaviour(cfpTimeoutBehaviour);
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

    private record ProposalData(int canFree, int priority) {

    }


}
