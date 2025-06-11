package org.asaa.behaviours.appliances;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import org.asaa.agents.SmartApplianceAgent;

import java.util.ArrayList;
import java.util.List;

public class AwaitEnableBehaviour extends TickerBehaviour {
    private final SmartApplianceAgent smartApplianceAgent;
    private final List<Runnable> runnables = new ArrayList<>();
    private final List<Behaviour> behaviours = new ArrayList<>();
    private boolean previouslyEnabled = false;

    public AwaitEnableBehaviour(SmartApplianceAgent smartApplianceAgent, long period, List<Runnable> runnables, List<Behaviour> behaviours) {
        super(smartApplianceAgent, period);
        this.smartApplianceAgent = smartApplianceAgent;
        this.runnables.addAll(runnables);
        this.behaviours.addAll(behaviours);
    }

    @Override
    public void onTick() {
        if (smartApplianceAgent.isEnabled() && !previouslyEnabled) {
            previouslyEnabled = true;
            smartApplianceAgent.getLogger().info("{} enabled, starting runnables & behaviours", smartApplianceAgent.getLocalName());

            runnables.forEach(Runnable::run);
            behaviours.forEach(smartApplianceAgent::addBehaviour);
        } else if (!smartApplianceAgent.isEnabled() && previouslyEnabled) {
            previouslyEnabled = false;
            smartApplianceAgent.getLogger().info("{} disabled, stopping runnables & behaviours", smartApplianceAgent.getLocalName());

            behaviours.forEach(smartApplianceAgent::removeBehaviour);
        }
        smartApplianceAgent.updateStatus();
    }
}
