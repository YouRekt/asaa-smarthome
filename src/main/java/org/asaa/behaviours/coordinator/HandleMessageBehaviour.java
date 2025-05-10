package org.asaa.behaviours.coordinator;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.behaviours.BaseMessageHandler;
import org.asaa.services.EnvironmentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HandleMessageBehaviour extends BaseMessageHandler {
    protected final CoordinatorAgent coordinatorAgent;
    protected final EnvironmentService environmentService;

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
            switch (msg.getConversationId()) {
                case "routine-morning" -> coordinatorAgent.performMorningRoutine();
                default -> super.processMsg(msg);
            }
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
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Enable active refused - " + requiredPower + "W");
                    coordinatorAgent.send(reply);
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
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Enable active refused - " + requiredPower + "W");
                    coordinatorAgent.send(reply);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        String[] parts = msg.getContent().split(",");
        switch (msg.getConversationId()) {
            case "disable-active":
                var returnedPower = Integer.parseInt(parts[0]);
                environmentService.modifyPowerConsumption(-returnedPower);
                break;
            case "get-missing-items":
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

