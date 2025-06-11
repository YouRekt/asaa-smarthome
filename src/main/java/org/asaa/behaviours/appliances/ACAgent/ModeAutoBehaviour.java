package org.asaa.behaviours.appliances.ACAgent;

import jade.core.behaviours.TickerBehaviour;
import org.asaa.agents.appliances.ACAgent;

public class ModeAutoBehaviour extends TickerBehaviour {
    private final ACAgent agent;

    public ModeAutoBehaviour(ACAgent agent) {
        super(agent, 10000);
        this.agent = agent;
    }

    @Override
    protected void onTick() {
        agent.requestTemperature();
    }
}