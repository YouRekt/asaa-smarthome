package org.asaa.behaviours.appliances;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import org.asaa.agents.SmartApplianceAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwaitEnableBehaviour extends TickerBehaviour {
    private final SmartApplianceAgent smartApplianceAgent;
    private final List<Runnable> runnables = new ArrayList<>();
    private final Map<String, Behaviour> behaviours = new HashMap<>();
    private boolean previouslyEnabled = false;

    public AwaitEnableBehaviour(SmartApplianceAgent smartApplianceAgent, long period, List<Runnable> runnables, Map<String, Behaviour> behaviours) {
        super(smartApplianceAgent, period);
        this.smartApplianceAgent = smartApplianceAgent;
        this.runnables.addAll(runnables);
        this.behaviours.putAll(behaviours);
    }

    @Override
    public void onTick() {
        if (smartApplianceAgent.isEnabled() && !previouslyEnabled) {
            previouslyEnabled = true;
            smartApplianceAgent.getLogger().info("{} enabled, starting runnables & behaviours", smartApplianceAgent.getLocalName());

            runnables.forEach(Runnable::run);
            behaviours.forEach((_, behaviour) -> smartApplianceAgent.addBehaviour(behaviour));
        } else if (!smartApplianceAgent.isEnabled() && previouslyEnabled) {
            previouslyEnabled = false;
            smartApplianceAgent.getLogger().info("{} disabled, stopping runnables & behaviours", smartApplianceAgent.getLocalName());

            behaviours.forEach((_, behaviour) -> smartApplianceAgent.removeBehaviour(behaviour));
        }
        smartApplianceAgent.updateStatus();
    }
}
