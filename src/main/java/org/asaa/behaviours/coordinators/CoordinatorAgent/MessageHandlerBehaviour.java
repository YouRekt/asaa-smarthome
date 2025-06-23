package org.asaa.behaviours.coordinators.CoordinatorAgent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.behaviours.appliances.tasks.PowerProposal;
import org.asaa.behaviours.appliances.tasks.PowerRequest;
import org.asaa.behaviours.base.BaseMessageHandlerBehaviour;

import java.util.*;
import java.util.stream.Collectors;

public class MessageHandlerBehaviour extends BaseMessageHandlerBehaviour {
    protected final CoordinatorAgent agent;

    public MessageHandlerBehaviour(CoordinatorAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void action() {
        final ACLMessage msg = agent.receive();
        if (msg != null) {
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }

    @Override
    protected void handleRequest(ACLMessage msg) {
        int availablePower = agent.environmentService.getPowerAvailability();
        String convId = msg.getConversationId();
        switch (convId) {
            case "enable-passive":
            case "enable-active":
                ObjectMapper mapper = new ObjectMapper();
                try {
                    PowerRequest powerRequest = mapper.readValue(msg.getContent(), PowerRequest.class);
                    if (availablePower >= powerRequest.getPowerAmount()) {
                        agent.environmentService.modifyPowerConsumption(+powerRequest.getPowerAmount());
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setContent("Enable " + (convId.equals("enable-passive") ? "passive" : "active") + " approved - " + powerRequest.getPowerAmount() + "W");
                        agent.sendMessage(reply);
                    } else if (powerRequest.getTaskInfo() == null) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Enable " + (convId.equals("enable-passive") ? "passive" : "active") + " refused - " + powerRequest.getPowerAmount() + "W");
                        agent.sendMessage(reply);
                    } else {
                        if (agent.isCfpInProgress()) {
                            agent.getPendingCfpQueue().add(msg);
                            agent.getLogger().warn("Deferring power relief CFP from {} because another is in progress", msg.getSender().getLocalName());
                            return;
                        }
                        agent.setCfpInProgress(true);
                        agent.getLogger().info("Entering agent negotiation phase for {}", msg.getSender().getLocalName());
                        agent.setPowerNegotiationBehaviour(new PowerNegotiationBehaviour(agent, msg, powerRequest, powerRequest.getPowerAmount() - availablePower, this::allowNextCfp));
                        agent.addBehaviour(agent.getPowerNegotiationBehaviour());
                    }
                } catch (JsonProcessingException e) {
                    agent.getLogger().error("{}@handleRequest: JsonProcessingException {}", this.getClass().getSimpleName(), e.getMessage());
                }
                break;
            case "add-to-await-power-list":
                agent.getAppliancesAwaitingPower().put(msg.getSender(), Integer.parseInt(msg.getContent()));
                agent.getLogger().info("Adding agent {} to appliances awaiting power list", msg.getSender().getLocalName());
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        int returnedPower;
        switch (msg.getConversationId()) {
            case "disable-passive-cfp":
            case "disable-active-cfp":
                returnedPower = Integer.parseInt(msg.getContent());
                agent.environmentService.modifyPowerConsumption(-returnedPower);
                ACLMessage replycfp = msg.createReply();
                replycfp.setPerformative(ACLMessage.CONFIRM);
                replycfp.setContent(msg.getContent());
                agent.sendMessage(replycfp);
                agent.getPowerNegotiationBehaviour().incrementReceivedMessages();
                agent.getPowerNegotiationBehaviour().restart();
                break;
            case "disable-passive":
            case "disable-active":
                returnedPower = Integer.parseInt(msg.getContent());
                agent.environmentService.modifyPowerConsumption(-returnedPower);
                if (!agent.getAppliancesAwaitingPower().isEmpty()) {
                    int availablePower = agent.environmentService.getPowerAvailability();
                    List<AID> candidates = new ArrayList<>();

                    // TODO: Add priority sorting - pass TaskInfo instead of Integer into the map and improve the logic here
                    for (var entry : agent.getAppliancesAwaitingPower().entrySet()) {
                        agent.getLogger().info("Comparing {} to {}, req={} avail={}", entry.getKey().getLocalName(), msg.getSender().getLocalName(), entry.getValue(), availablePower);
                        if (entry.getValue() <= availablePower && !Objects.equals(entry.getKey().getLocalName(), msg.getSender().getLocalName())) {
                            candidates.add(entry.getKey());
                            availablePower -= entry.getValue();
                        }
                    }

                    if (!candidates.isEmpty()) {
                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        message.setConversationId("power-released");
                        candidates.forEach(message::addReceiver);
                        agent.getLogger().info("Notifying {} agents of {}W returned power", candidates.size(), returnedPower);
                        candidates.forEach(agent.getAppliancesAwaitingPower()::remove);
                        agent.sendMessage(message);
                    }
                }
                ACLMessage reply = msg.createReply(ACLMessage.CONFIRM);
                reply.setContent(msg.getContent());
                agent.sendMessage(reply);
                break;
            case "get-missing-items":
            case "action-morning":
                if (msg.getContent().isEmpty()) {
                    agent.getLogger().info("No missing items in fridge to buy");
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
                    int bought = agent.environmentService.buyBatch(item.name);
                    if (bought > 0) {
                        purchased.merge(item.name, bought, Integer::sum);
                    } else {
                        agent.getLogger().warn("Item {} could not be bought", item.name);
                        agent.agentCommunicationController.sendError(agent.getLocalName(), "Item " + item.name + " could not be bought", false);
                    }
                }

                agent.getLogger().info("Bought items: {}", purchased);

                if (!purchased.isEmpty()) {
                    ACLMessage updateMsg = new ACLMessage(ACLMessage.INFORM);
                    updateMsg.addReceiver(msg.getSender());
                    updateMsg.setConversationId("stock-update");
                    updateMsg.setContent(purchased.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")));
                    agent.sendMessage(updateMsg);
                }
                break;
            case "routine-morning":
                agent.performMorningRoutine();
                break;
            case "human-not-home-lights":
                agent.toggleRandomLight(msg.getContent());
                break;
            default:
                break;
        }
    }

    @Override
    protected void handlePropose(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "power-relief":
                ObjectMapper mapper = new ObjectMapper();
                try {
                    PowerProposal powerProposal = mapper.readValue(msg.getContent(), PowerProposal.class);
                    agent.getPowerNegotiationBehaviour().incrementReceivedMessages();
                    agent.getPowerNegotiationBehaviour().getProposals().add(powerProposal);
                    agent.getPowerNegotiationBehaviour().restart();
                } catch (JsonProcessingException e) {
                    agent.getLogger().error("{}@handlePropose: JsonProcessingException {}", this.getClass().getSimpleName(), e.getMessage());
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
                agent.getPowerNegotiationBehaviour().incrementReceivedMessages();
                agent.getPowerNegotiationBehaviour().restart();
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

    public void allowNextCfp() {
        agent.setCfpInProgress(false);
        if (!agent.getPendingCfpQueue().isEmpty()) {
            ACLMessage nextCfp = agent.getPendingCfpQueue().poll();
            handleCfp(nextCfp);
        }
    }
}
