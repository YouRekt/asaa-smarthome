package org.asaa.agents.appliances;

import lombok.Getter;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.FridgeAgent.MessageHandlerBehaviour;
import org.asaa.util.ItemInfo;

import java.util.HashMap;
import java.util.Map;

@Getter
public final class FridgeAgent extends SmartApplianceAgent {
    private final Map<String, ItemInfo> fridgeItems = new HashMap<>();

    @Override
    protected void setup() {
        idleDraw = 200;
        activeDraw = 0;
        priority = 999;

        runnables.add(this::initializeFridgeItems);

        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));
    }

    private void initializeFridgeItems() {
        fridgeItems.put("Milk", new ItemInfo(2, 5));
        fridgeItems.put("Eggs", new ItemInfo(12, 4));
        fridgeItems.put("Butter", new ItemInfo(1, 3));
        fridgeItems.put("Cheese", new ItemInfo(0, 4));
        fridgeItems.put("Yogurt", new ItemInfo(3, 2));
        fridgeItems.put("Juice", new ItemInfo(0, 1));
    }

    public String fridgeStock() {
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
