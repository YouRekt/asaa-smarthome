package org.asaa.tasks;

import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.RelinquishPowerBehaviour;

public abstract class Task {
    @Getter
    @Setter
    protected boolean resumable;
    @Getter
    @Setter
    protected boolean interruptible;

    public void start(SmartApplianceAgent agent) {
        agent.setCurrentTask(this);
    }

    public void end(SmartApplianceAgent agent) {
        agent.setCurrentTask(null);
        agent.addBehaviour(new RelinquishPowerBehaviour(agent, agent.getActiveDraw(), "disable-active"));
    }

    public abstract void start();
    public abstract void pause();
    public abstract void resume();
    public abstract void interrupt();
    public abstract void wake();

}
