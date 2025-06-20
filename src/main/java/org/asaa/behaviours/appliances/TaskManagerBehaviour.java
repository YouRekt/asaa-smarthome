package org.asaa.behaviours.appliances;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.tasks.Task;

public class TaskManagerBehaviour extends CyclicBehaviour {
    private final SmartApplianceAgent agent;

    public TaskManagerBehaviour(SmartApplianceAgent agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void action() {
        if((agent.getCurrentTaskBehaviour() == null || agent.getCurrentTaskBehaviour().done()) && !agent.getTaskBehaviourQueue().isEmpty())
        {
            TaskBehaviour nextTask = agent.getTaskBehaviourQueue().poll();
            agent.setCurrentTaskBehaviour(nextTask);
            agent.addBehaviour(nextTask);
        } else
        {
            block(100); // I guess we can block the behavior so it isn't constantly polling
        }
    }
}
