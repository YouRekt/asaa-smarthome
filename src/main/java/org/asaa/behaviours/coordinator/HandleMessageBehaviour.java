package org.asaa.behaviours.coordinator;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.behaviours.BaseMessageHandler;
import org.asaa.services.EnvironmentService;

import java.util.*;
import java.util.stream.Collectors;

public class HandleMessageBehaviour extends BaseMessageHandler {
    protected final CoordinatorAgent coordinatorAgent;
    protected final EnvironmentService environmentService;

    @Getter
    private boolean cfpPassive;
    @Getter
    private int cfpResponses;
    @Getter
    private int cfpInformResponses;
    @Getter
    @Setter
    private int cfpSentProposals;
    @Getter
    private int cfpSent;
    @Getter
    private int cfpShortage;
    @Getter
    private long cfpStartTime;
    @Getter
    @Setter
    private long cfpInformStartTime;
    @Getter
    private ACLMessage cfpMessage;
    @Getter
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

    // TODO: Add negotiations
    @Override
    protected void handleCfp(ACLMessage msg) {
        int availablePower = environmentService.getPowerAvailability(), requiredPower, priority;
        String[] parts = msg.getContent().split(",");
        switch (msg.getConversationId()) {
            case "enable-passive":
                requiredPower = Integer.parseInt(parts[0]);
                priority = Integer.parseInt(parts[1]);
                if (availablePower >= requiredPower) {
                    environmentService.modifyPowerConsumption(requiredPower);
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("Enable passive approved - " + requiredPower + "W");
                    coordinatorAgent.send(reply);
                } else {
                    cfpPassive = true;
                    cfpResponses = 0;
                    cfpInformResponses = 0;
                    cfpSentProposals = 0;
                    cfpShortage = requiredPower - availablePower;
                    cfpStartTime = System.currentTimeMillis();
                    cfpSent = (int)coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream()).filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream()).filter(a -> !a.equals(msg.getSender())).count();
                    cfpMessage = msg;
                    cfpProposals.clear();

                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.setConversationId("power-relief");
                    cfp.setContent(Integer.toString(cfpShortage));
                    coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream().filter(entry -> !entry.getKey().contains("Sensor")).flatMap(entry -> entry.getValue().stream())).filter(aid -> !aid.equals(msg.getSender())).forEach(cfp::addReceiver);                    coordinatorAgent.send(cfp);
                    coordinatorAgent.send(cfp);

                    coordinatorAgent.addBehaviour(new ReliefNegotiationBehaviour(coordinatorAgent, 250, this));
//                    ACLMessage reply = msg.createReply();
//                    reply.setPerformative(ACLMessage.REFUSE);
//                    reply.setContent("Enable active refused - " + requiredPower + "W");
//                    coordinatorAgent.send(reply);
                }
                break;
            case "enable-active":
                requiredPower = Integer.parseInt(parts[0]);
                priority = Integer.parseInt(parts[1]);
                if (availablePower >= requiredPower) {
                    environmentService.modifyPowerConsumption(requiredPower);
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("Enable active approved - " + requiredPower + "W");
                    coordinatorAgent.send(reply);
                } else {
                    cfpPassive = false;
                    cfpResponses = 0;
                    cfpInformResponses = 0;
                    cfpSentProposals = 0;
                    cfpShortage = requiredPower - availablePower;
                    cfpStartTime = System.currentTimeMillis();
                    //cfpSent = coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.values().stream().flatMap(List::stream)).filter(a -> !a.equals(msg.getSender())).toList().size();
                    cfpSent = (int)coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream()).filter(e -> !e.getKey().contains("Sensor")).flatMap(e -> e.getValue().stream()).filter(a -> !a.equals(msg.getSender())).count();
                    cfpMessage = msg;
                    cfpProposals.clear();
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.setConversationId("power-relief");
                    cfp.setContent(Integer.toString(cfpShortage));
                    //coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.values().stream().flatMap(List::stream)).filter(aid -> !aid.equals(msg.getSender())).forEach(cfp::addReceiver);
                    coordinatorAgent.getPhysicalAgents().values().stream().flatMap(m -> m.entrySet().stream().filter(entry -> !entry.getKey().contains("Sensor")).flatMap(entry -> entry.getValue().stream())).filter(aid -> !aid.equals(msg.getSender())).forEach(cfp::addReceiver);                    coordinatorAgent.send(cfp);

                    coordinatorAgent.addBehaviour(new ReliefNegotiationBehaviour(coordinatorAgent, 250, this));

//                    ACLMessage reply = msg.createReply();
//                    reply.setPerformative(ACLMessage.REFUSE);
//                    reply.setContent("Enable active refused - " + requiredPower + "W");
//                    coordinatorAgent.send(reply);
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
                cfpProposals.put(msg.getSender(), Integer.parseInt(msg.getContent()));
                cfpResponses++;
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleRefuse(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
                cfpResponses++;
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        String[] parts = msg.getContent().split(",");
        int returnedPower;
        switch (msg.getConversationId()) {
            case "disable-passive":
            case "disable-active":
                returnedPower = Integer.parseInt(parts[0]);
                environmentService.modifyPowerConsumption(-returnedPower);
                break;
            case "disable-passive-cfp":
            case "disable-active-cfp":
                returnedPower = Integer.parseInt(parts[0]);
                environmentService.modifyPowerConsumption(-returnedPower);
                cfpInformResponses++;
                break;
            case "get-missing-items":
            case "action-morning":
                if (msg.getContent().isEmpty()) {
                    logger.info("No missing items in fridge to buy");
                    return;
                }
                List<ItemRequest> missingItems = new ArrayList<>();

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

    private static class ItemRequest {
        String name;
        int priority;

        ItemRequest(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }
    }
}

