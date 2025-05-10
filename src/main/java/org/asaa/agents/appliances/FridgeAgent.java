package org.asaa.agents.appliances;

import jade.lang.acl.ACLMessage;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliance.AwaitEnableBehaviour;
import org.asaa.behaviours.appliance.HandleMessageBehaviour;
import org.asaa.behaviours.appliance.RequestPowerBehaviour;
import org.asaa.util.ItemInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FridgeAgent extends SmartApplianceAgent {
    private final Map<String, ItemInfo> fridgeItems = new HashMap<>();

    @Override
    protected void setup() {
        super.setup();

        idleDraw = 200; // We assume that the fridge is always fully on or off
        activeDraw = 0;

        addBehaviour(new HandleMessageBehaviour(this) {
            @Override
            protected void handleInform(ACLMessage msg) {
                switch (msg.getConversationId()) {
                    case "stock-update":
                        String[] items = msg.getContent().split(",");
                        for (String item : items) {
                            String[] parts = item.split(":");
                            String name = parts[0];
                            int amount = Integer.parseInt(parts[1]);

                            fridgeItems.computeIfPresent(name, (k, info) -> {
                                info.increaseCount(amount);
                                return info;
                            });

                            logger.info(responseDefaultMsgContent());
                        }
                    default:
                        break;
                }
            }

            @Override
            protected void handleRequest(ACLMessage msg) {
                switch (msg.getConversationId()) {
                    case "get-missing-items":
                        List<String> missing = new ArrayList<>();
                        for (Map.Entry<String, ItemInfo> entry : fridgeItems.entrySet()) {
                            if (entry.getValue().getCount() == 0) {
                                missing.add(entry.getKey() + ":" + entry.getValue().getPriority());
                            }
                        }

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(String.join(",", missing));
                        send(reply);
                        break;
                    default:
                        break;
                }
            }
        });
        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));
        addBehaviour(new AwaitEnableBehaviour(this, () -> {
            initializeFridgeItems();
        }));
    }

    private void initializeFridgeItems() {
        fridgeItems.put("Milk", new ItemInfo(2, 5));
        fridgeItems.put("Eggs", new ItemInfo(12, 4));
        fridgeItems.put("Butter", new ItemInfo(1, 3));
        fridgeItems.put("Cheese", new ItemInfo(0, 4));
        fridgeItems.put("Yogurt", new ItemInfo(3, 2));
        fridgeItems.put("Juice", new ItemInfo(0, 1));
    }

    @Override
    protected void handleTrigger() {

    }

    @Override
    protected String responseDefaultMsgContent() {
        if (fridgeItems.isEmpty()) {
            return "Fridge was not initialized";
        }

        StringBuilder status = new StringBuilder("Current fridge stock:\n");
        for (Map.Entry<String, ItemInfo> entry : fridgeItems.entrySet()) {
            String item = entry.getKey();
            ItemInfo itemInfo = entry.getValue();
            status.append(String.format("- %s: %d units (Priority %d)\n", item, itemInfo.getCount(), itemInfo.getPriority()));
        }
        return status.toString();
    }
}
