package org.asaa.behaviours.appliances;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import org.asaa.agents.SmartApplianceAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwaitEnableBehaviour extends TickerBehaviour {
    private final SmartApplianceAgent agent;
    private final List<Runnable> runnables = new ArrayList<>();
    private final Map<String, Behaviour> behaviours = new HashMap<>();
    private boolean previouslyEnabled = false;

    public AwaitEnableBehaviour(SmartApplianceAgent agent, long period, List<Runnable> runnables, Map<String, Behaviour> behaviours) {
        super(agent, period);
        this.agent = agent;
        this.runnables.addAll(runnables);
        this.behaviours.putAll(behaviours);
    }

    @Override
    public void onTick() {
        if (agent.isEnabled() && !previouslyEnabled) {
            previouslyEnabled = true;
            agent.getLogger().info("{} enabled, starting runnables & behaviours", agent.getLocalName());

            runnables.forEach(Runnable::run);
            behaviours.forEach((_, behaviour) -> agent.addBehaviour(behaviour));
        } else if (!agent.isEnabled() && previouslyEnabled) {
            previouslyEnabled = false;
            agent.getLogger().info("{} disabled, stopping runnables & behaviours", agent.getLocalName());

            behaviours.forEach((_, behaviour) -> agent.removeBehaviour(behaviour));
        }
        agent.updateStatus();
    }
}
