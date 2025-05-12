package org.asaa.behaviours.coordinator;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.behaviours.BaseMessageHandler;
import org.asaa.services.EnvironmentService;

import java.util.*;
import java.util.stream.Collectors;

public class HandleMessageBehaviour extends BaseMessageHandler {
    protected final CoordinatorAgent coordinatorAgent;

    public HandleMessageBehaviour(CoordinatorAgent coordinatorAgent) {
        super(coordinatorAgent);
        this.coordinatorAgent = coordinatorAgent;
    }

    @Override
    public void action() {
        MessageTemplate mt = new MessageTemplate((MessageTemplate.MatchExpression) msg -> msg.getConversationId() != null &&
                !msg.getConversationId().equals("power-relief") &&
                !msg.getConversationId().equals("disable-passive-cfp") &&
                !msg.getConversationId().equals("disable-active-cfp"));

        final ACLMessage msg = coordinatorAgent.receive(mt);
        if (msg != null) {
            // Here we can add a specialized switch if needed (default -> processMsg(msg);)
            super.processMsg(msg);
        } else {
            block();
        }
    }

    @Override
    protected void handleCfp(ACLMessage msg) {
        int availablePower = coordinatorAgent.environmentService.getPowerAvailability(), requiredPower, priority;
        String convId = msg.getConversationId();
        String[] msgParts = msg.getContent().split(",");
        switch (convId) {
            case "enable-passive":
            case "enable-active":
                requiredPower = Integer.parseInt(msgParts[0]);
                priority = Integer.parseInt(msgParts[1]);
                if (availablePower >= requiredPower) {
                    coordinatorAgent.environmentService.modifyPowerConsumption(+requiredPower);
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("Enable " + (convId.equals("enable-passive") ? "passive" : "active") + " approved - " + requiredPower + "W");
                    coordinatorAgent.sendMessage(reply);
                } else {
                    coordinatorAgent.addBehaviour(new PowerNegotiationBehaviour(coordinatorAgent, msg, requiredPower - availablePower, requiredPower, priority));
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
                coordinatorAgent.environmentService.modifyPowerConsumption(-returnedPower);
                if (!coordinatorAgent.getAppliancesAwaitingCallback().getOrDefault(msg.getSender(), Collections.emptyList()).isEmpty()) {
                    ACLMessage callback = new ACLMessage(ACLMessage.INFORM);
                    callback.setConversationId("enable-callback");
                    callback.setContent(msg.getSender().getName());
                    coordinatorAgent.getAppliancesAwaitingCallback().get(msg.getSender()).forEach(callback::addReceiver);
                    CoordinatorAgent.getLogger().info("Sending out {} callbacks after {} returned power", coordinatorAgent.getAppliancesAwaitingCallback().get(msg.getSender()).size(), msg.getSender().getLocalName());
                    coordinatorAgent.sendMessage(callback);
                    coordinatorAgent.getAppliancesAwaitingCallback().getOrDefault(msg.getSender(), Collections.emptyList()).clear();
                }
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent(msg.getContent());
                coordinatorAgent.sendMessage(reply);
                break;
            case "get-missing-items":
            case "action-morning":
                if (msg.getContent().isEmpty()) {
                    CoordinatorAgent.getLogger().info("No missing items in fridge to buy");
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
                    int bought = coordinatorAgent.environmentService.buyBatch(item.name);
                    if (bought > 0) {
                        purchased.merge(item.name, bought, Integer::sum);
                    } else {
                        CoordinatorAgent.getLogger().warn("Item {} could not be bought", item.name);
                        coordinatorAgent.agentCommunicationController.sendError(coordinatorAgent.getName(), "Item " + item.name + " could not be bought");
                    }
                }

                CoordinatorAgent.getLogger().info("Bought items: {}", purchased);

                if (!purchased.isEmpty()) {
                    ACLMessage updateMsg = new ACLMessage(ACLMessage.INFORM);
                    updateMsg.addReceiver(msg.getSender());
                    updateMsg.setConversationId("stock-update");
                    updateMsg.setContent(purchased.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")));
                    coordinatorAgent.sendMessage(updateMsg);
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
