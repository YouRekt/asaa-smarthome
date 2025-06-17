package org.asaa.tasks;

import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.behaviours.appliances.base.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliances.base.RequestPowerBehaviour;

/**
 * Simple abstract class that is the base of the task that a SmartApplianceAgent can be performing
 */
public abstract class Task {
    private final SmartApplianceAgent agent;

    @Getter
    @Setter
    protected boolean resumable;
    @Getter
    @Setter
    protected boolean interruptible;

    protected boolean paused = false;
    protected boolean interrupted = false;
    protected boolean awaitingWake = false;

    protected Task(SmartApplianceAgent agent, boolean resumable, boolean interruptible) {
        this.agent = agent;
        this.resumable = resumable;
        this.interruptible = interruptible;
    }

    public void start() {
        if (agent.getCurrentTask() != null && !agent.getCurrentTask().equals(this)) {
            agent.getLogger().error("Another task is already running when trying to start a new one! Aborting...");
            return;
        }

        agent.getLogger().info("{} {}, requesting power", paused ? "Resuming" : "Starting", this.getClass().getSimpleName());
        String replyWith = "req-" + System.currentTimeMillis();
        agent.onPowerGrantedCallbacks.put(replyWith, this::onPowerGranted);
        agent.addBehaviour(new RequestPowerBehaviour(agent, agent.getActiveDraw(), agent.getPriority(), "enable-active", replyWith));
    }

    protected void onPowerGranted() {
        agent.getLogger().info("{} has been granted power", this.getClass().getSimpleName());
        agent.setCurrentTask(this);
        execute();
    }

    protected abstract void execute();

    public void pause(boolean isCfpCall) {
        if (resumable && !paused) {
            agent.getLogger().info("{} paused", this.getClass().getSimpleName());
            paused = true;
            agent.addBehaviour(new RelinquishPowerBehaviour(agent, agent.getActiveDraw(), "disable-active" + (isCfpCall ? "-cfp" : "")));
        } else {
            agent.getLogger().warn("{} was already, or can not be, paused", this.getClass().getSimpleName());
        }
    }

    public void resume() {
        if (paused) {
            start();
            paused = false;
        }
    }

    public void interrupt() {
        if (interruptible && !interrupted) {
            agent.getLogger().warn("{} interrupted", this.getClass().getSimpleName());
            interrupted = true;
            end(false);
        } else {
            agent.getLogger().warn("{} has already been, or can not be, interrupted", this.getClass().getSimpleName());
        }
    }

    public void wake() {}

    protected void end(boolean success) {
        if (success) {
            agent.environmentService.addPerformedTask();
        } else {
            agent.agentCommunicationController.sendError(agent.getName(), this.getClass().getSimpleName() + " failed");
        }
        agent.setCurrentTask(null);
        agent.addBehaviour(new RelinquishPowerBehaviour(agent, agent.getActiveDraw(), "disable-active"));
    }
}
