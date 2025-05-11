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
    protected final EnvironmentService environmentService;
    //private final MessageTemplate messageTemplate = MessageTemplate.not(MessageTemplate.MatchConversationId("power-relief"));

    public HandleMessageBehaviour(CoordinatorAgent coordinatorAgent) {
        super(coordinatorAgent);
        this.coordinatorAgent = coordinatorAgent;
        this.environmentService = coordinatorAgent.environmentService;
    }

    @Override
    public void action() {
        //MessageTemplate messageTemplate = MessageTemplate.and(MessageTemplate.not(MessageTemplate.MatchConversationId("power-relief")), MessageTemplate.and(MessageTemplate.not(MessageTemplate.MatchConversationId("disable-passive-cfp")), MessageTemplate.not(MessageTemplate.MatchConversationId("disable-active-cfp"))));
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
                    coordinatorAgent.addBehaviour(new PowerNegotiationBehaviour(coordinatorAgent, msg, requiredPower - availablePower, Integer.parseInt(msg.getContent())));
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

    private static class ItemRequest {
        String name;
        int priority;

        ItemRequest(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }
    }
}
