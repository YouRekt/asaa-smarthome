package org.asaa.behaviours.appliances;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import org.asaa.agents.base.SmartApplianceAgent;


public class TaskManagerBehaviour extends TickerBehaviour {
    private final SmartApplianceAgent agent;

    public TaskManagerBehaviour(SmartApplianceAgent agent) {
        super(agent, 500);
        this.agent = agent;
    }

    @Override
    public void onTick() {
        if ((agent.getCurrentTaskBehaviour() == null || agent.getCurrentTaskBehaviour().done())) {
            if (agent.getTaskBehaviourQueue().isEmpty()) {
                agent.setCurrentTaskBehaviour(null);
            } else {
                TaskBehaviour<?> nextTask = agent.getTaskBehaviourQueue().poll();
                agent.setCurrentTaskBehaviour(nextTask);
                agent.addBehaviour(nextTask);
            }
        }
    }
}
