package org.asaa.behaviours.appliances.FridgeAgent;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.appliances.FridgeAgent;
import org.asaa.util.ItemInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageHandlerBehaviour extends org.asaa.behaviours.appliances.MessageHandlerBehaviour {
    private final FridgeAgent agent;

    public MessageHandlerBehaviour(FridgeAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    protected void handleInform(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "stock-update":
                String[] items = msg.getContent().split(",");
                for (String item : items) {
                    String[] parts = item.split(":");
                    String name = parts[0];
                    int amount = Integer.parseInt(parts[1]);

                    agent.getFridgeItems().computeIfPresent(name, (k, info) -> {
                        info.increaseCount(amount);
                        return info;
                    });

                }

                agent.getLogger().info("[UPDATED] - {}", agent.responseDefaultMsgContent());
                agent.environmentService.addPerformedTask();
                break;
            default:
                break;
        }
        super.handleInform(msg);
    }

    @Override
    protected void handleRequest(ACLMessage msg) {
        switch (msg.getConversationId()) {
            case "get-missing-items":
            case "action-morning":
                agent.getLogger().info("Get missing items called - {}", agent.responseDefaultMsgContent());
                List<String> missing = new ArrayList<>();
                for (Map.Entry<String, ItemInfo> entry : agent.getFridgeItems().entrySet()) {
                    if (entry.getValue().getCount() == 0) {
                        missing.add(entry.getKey() + ":" + entry.getValue().getPriority());
                    }
                }

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                if (!missing.isEmpty()) {
                    reply.setContent(String.join(",", missing));
                    agent.sendMessage(reply);
                } else {
                    reply.setContent("");
                    agent.sendMessage(reply);
                }
                break;
            default:
                break;
        }
    }
}
