package org.asaa.behaviours.coordinator;

import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.behaviours.BaseMessageHandler;
import org.asaa.services.EnvironmentService;

import java.util.*;
import java.util.stream.Collectors;
// TODO: Make sure CFP's are handled one-by-one to avoid overriding variables
public class HandleMessageBehaviour extends BaseMessageHandler {
    protected final CoordinatorAgent coordinatorAgent;
    protected final EnvironmentService environmentService;

    private final long cfpResponseTimeout = 5000;
    private int cfpShortage;
    private int cfpRequiredPower;
    private int cfpReceivedResponses;
    private int cfpSentProposals;
    private int cfpRelievedPower;
    private boolean cfpProposalsProcessed;
    private ACLMessage cfpMessage;
    private final WakerBehaviour cfpTimeoutBehaviour = new WakerBehaviour(myAgent, cfpResponseTimeout) {
        @Override
        protected void onWake() {
            logger.warn("Reply-by for cfp expired, received {} responses, sent {}", cfpReceivedResponses, cfpSentProposals);
            if (cfpProposalsProcessed)
                cfpRespondToSender();
            else
                cfpProcessProposals();
        }
    };
    private final Map<AID, Integer> cfpProposals = new HashMap<>();


    public HandleMessageBehaviour(CoordinatorAgent coordinatorAgent) {
        super(coordinatorAgent);
        this.coordinatorAgent = coordinatorAgent;
        this.environmentService = coordinatorAgent.environmentService;
    }

    @Override
    public void action() {
        final ACLMessage msg = coordinatorAgent.receive();

        if (msg != null) {
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }

    @Override
    protected void handleCfp(ACLMessage msg) {
        int availablePower = environmentService.getPowerAvailability(), requiredPower, priority;
        String convId = msg.getConversationId();

        switch (convId) {
            case "enable-passive":
            case "enable-active":
                requiredPower = Integer.parseInt(msg.getContent());
                if (availablePower >= requiredPower) {
                    environmentService.modifyPowerConsumption(+requiredPower);
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("Enable " + (convId.equals("enable-passive") ? "passive" : "active") + " approved - " + requiredPower + "W");
                    coordinatorAgent.send(reply);
                } else {
                    cfpShortage = requiredPower - availablePower;
                    cfpRequiredPower = requiredPower;
                    cfpReceivedResponses = 0;
                    cfpSentProposals = (int)coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream()).filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream()).filter(a -> !a.equals(msg.getSender())).count();
                    cfpProposalsProcessed = false;
                    cfpMessage = msg;
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.setConversationId("power-relief");
                    cfp.setContent(Integer.toString(cfpShortage));
                    cfp.setReplyByDate(new Date(System.currentTimeMillis() + cfpResponseTimeout));
                    coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream().filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream())).filter(a -> !a.equals(msg.getSender())).forEach(cfp::addReceiver);
                    coordinatorAgent.send(cfp);

                    coordinatorAgent.addBehaviour(cfpTimeoutBehaviour);
                }
                break;
            default:
                break;
        }
    }

    @Override
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

    @Override
    protected void handlePropose(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
                cfpReceivedResponses++;
                cfpProposals.put(msg.getSender(), Integer.parseInt(msg.getContent()));
                if (cfpReceivedResponses >= cfpSentProposals && !cfpProposalsProcessed) {
                    cfpProcessProposals();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        int returnedPower;
        switch (msg.getConversationId()) {
            case "disable-passive":
            case "disable-active":
                returnedPower = Integer.parseInt(msg.getContent());
                environmentService.modifyPowerConsumption(-returnedPower);
                break;
            case "disable-passive-cfp":
            case "disable-active-cfp":
                cfpReceivedResponses++;
                returnedPower = Integer.parseInt(msg.getContent());
                environmentService.modifyPowerConsumption(-returnedPower);
                if (cfpReceivedResponses >= cfpSentProposals) {
                    cfpRespondToSender();
                }
                break;
            case "get-missing-items":
            case "action-morning":
                if (msg.getContent().isEmpty()) {
                    logger.info("No missing items in fridge to buy");
                    return;
                }
                List<ItemRequest> missingItems = new ArrayList<>();
                String[] parts = msg.getContent().split(",");

                for (String part : parts) {
                    String[] item = part.split(":");
                    String name = item[0];
                    int priority = Integer.parseInt(item[1]);
                    missingItems.add(new ItemRequest(name, priority));
                }

                missingItems.sort((a, b) -> Integer.compare(b.priority, a.priority));

                Map<String, Integer> purchased = new HashMap<>();
                for (ItemRequest item : missingItems) {
                    int bought = environmentService.buyBatch(item.name);
                    if (bought > 0) {
                        purchased.merge(item.name, bought, Integer::sum);
                    } else {
                        logger.warn("Item {} could not be bought", item.name);
                    }
                }

                logger.info("Bought items: {}", purchased);

                if (!purchased.isEmpty()) {
                    ACLMessage updateMsg = new ACLMessage(ACLMessage.INFORM);
                    updateMsg.addReceiver(msg.getSender());
                    updateMsg.setConversationId("stock-update");
                    updateMsg.setContent(purchased.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")));
                    coordinatorAgent.send(updateMsg);
                }
                break;
            case "routine-morning":
                coordinatorAgent.performMorningRoutine();
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
        List<Map.Entry<AID, Integer>> sortedProposals = new ArrayList<>(cfpProposals.entrySet());
        sortedProposals.sort(Comparator.comparing(e -> coordinatorAgent.getPriority(e.getKey())));
        cfpRelievedPower = 0;
        Set<AID> accepted = new HashSet<>();
        for (var proposal : sortedProposals) {
            if (cfpRelievedPower >= cfpShortage)
                break;
            cfpRelievedPower += proposal.getValue();
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
                proposalReply.setContent(Integer.toString(proposal.getValue()));
                proposalReply.setReplyByDate(new Date(System.currentTimeMillis() + cfpResponseTimeout));
                coordinatorAgent.send(proposalReply);
                cfpSentProposals += accepted.contains(proposal.getKey()) ? 1 : 0;

                coordinatorAgent.addBehaviour(cfpTimeoutBehaviour);
            }
        }
    }

    private void cfpRespondToSender() {
        coordinatorAgent.removeBehaviour(cfpTimeoutBehaviour);
        environmentService.modifyPowerConsumption(+cfpRequiredPower);
        ACLMessage reply = cfpMessage.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        reply.setContent("Enable " + (cfpMessage.getConversationId().equals("enable-passive") ? "passive" : "active") + " accepted after relief - " + cfpRequiredPower + "W (shortage: " + cfpShortage + "W, relief " + cfpRelievedPower + "W)");
        coordinatorAgent.send(reply);
    }

    private static class ItemRequest {
        String name;
        int priority;

        ItemRequest(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }
    }
}
