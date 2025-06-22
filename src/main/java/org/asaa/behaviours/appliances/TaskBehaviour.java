package org.asaa.behaviours.appliances;

import jade.core.behaviours.Behaviour;
import lombok.Getter;
import lombok.Setter;
import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.behaviours.appliances.base.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliances.base.RequestPowerBehaviour;
import org.asaa.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

public abstract class TaskBehaviour<T extends SmartApplianceAgent> extends Behaviour implements Comparable<TaskBehaviour<?>> {
    protected final T agent;
    protected final Map<String, TaskBehaviour<?>> definedErrors = new HashMap<>();
    private final String taskName;
    private final Random random = new Random();
    private final long delayTime = 5000;
    @Getter
    protected boolean pausable;
    @Getter
    protected boolean interruptible;
    @Getter
    @Setter
    protected int priority;
    protected int powerUsage;
    private boolean awaitingDelay = false;
    private long nextWakeTime = 0;
    @Getter
    @Setter
    private Status status = Status.waitingForPower;
    @Getter
    private String error = null;

    protected TaskBehaviour(T agent, String taskName, int priority, boolean pausable, boolean interruptible) {
        this.agent = agent;
        this.taskName = taskName;
        this.pausable = pausable;
        this.interruptible = interruptible;
        this.priority = priority;
        this.powerUsage = agent.getActiveDraw();
    }

    protected void registerError(String error, TaskBehaviour<?> resolutionBehaviour) {
        definedErrors.put(error, resolutionBehaviour);
    }

    @Override
    public int compareTo(TaskBehaviour<?> o) {
        return Integer.compare(o.priority, priority);
    }

    protected TaskBehaviour<?> resumeWith(int priority) {
        return null;
    }

    public boolean simulateError() {
        if (random.nextDouble() < 0.2) {
            error = Util.getRandomEntry(definedErrors).getKey(); // we can ignore this since the map SURELY will never be empty
            status = Status.error;
            agent.getLogger().error("Task {}: encountered {} error", taskName, error);
            agent.agentCommunicationController.sendError(agent.getLocalName(), String.format("Task %s: encountered %s", taskName, error), true);
            return true;
        }
        return false;
    }

    public void simulateError(String error) {
        agent.getLogger().error("Task {}: {}", taskName, definedErrors);
        if (definedErrors.containsKey(error)) {
            this.error = error;
            status = Status.error;
            agent.getLogger().error("Task {}: encountered {}", taskName, error);
            agent.agentCommunicationController.sendError(agent.getLocalName(), String.format("Task %s: encountered %s", taskName, error), true);
        } else {
            agent.getLogger().error("Task {}: Invalid error name \"{}\" specified.", taskName, error);
        }
    }

    public void simulateCriticalError(String error) {
        this.error = error;
        status = Status.criticalError;
        agent.getLogger().error("Task {}: encountered {} critical error. Human intervention necessary", taskName, error);
    }

    public void interrupt(boolean withError, boolean shouldRequeue) {
        if (shouldRequeue) {
            agent.getTaskBehaviourQueue().add(resumeWith(priority));
        }
        if (withError) {
            status = Status.error;
        } else {
            status = Status.finished;
        }
    }

    public void pause() {
        if (pausable && status != Status.paused) {
            status = Status.poweringOff;
            agent.getLogger().info("Task {}: paused", taskName);
            onPause();
        } else {
            agent.getLogger().error("Task {}: Task is not pausable or is already paused", taskName);
        }
    }

    protected void onPause() {
    }

    public void resume() {
        if (pausable && status == Status.paused) {
            agent.addBehaviour(new RequestPowerBehaviour(agent, powerUsage, priority, "enable-active", ""));
            status = Status.waitingForPower;
            agent.getLogger().info("Task {}: resumed", taskName);
        } else {
            agent.getLogger().error("Task {}: Cannot resume task that is not paused", taskName);
        }
    }

    /**
     * The returned value tells us whether the execution has been finished. This can come in handy when the task has
     * several stages or an error occurs during execution of the task.
     *
     * @return Flag telling weather the execution of the task has finished or should it be continued when the task is
     * drawn from the agent's behaviour pool
     */
    protected abstract boolean execute();

    @Override
    public void onStart() {
        agent.getLogger().info("Starting {}", taskName);
        agent.addBehaviour(new RequestPowerBehaviour(agent, powerUsage, priority, "enable-active", ""));
    }

    @Override
    public void action() {
        switch (status) {
            case powerGranted:
                status = Status.running;
                break;
            case powerRefused:
                if (awaitingDelay) {
                    if (System.currentTimeMillis() >= nextWakeTime) {
                        awaitingDelay = false;
                    } else {
                        block(nextWakeTime - System.currentTimeMillis());
                        return;
                    }
                }

                nextWakeTime = System.currentTimeMillis() + delayTime;
                awaitingDelay = true;
                agent.addBehaviour(new RequestPowerBehaviour(agent, powerUsage, priority, "enable-active", ""));
                status = Status.waitingForPower;
                break;
            case running:
                if (execute()) {
                    status = Status.finished;
                }
                break;
            case poweringOff:
                agent.addBehaviour(new RelinquishPowerBehaviour(agent, powerUsage, "disable-active"));
                status = Status.paused;
                break;
            case waitingForPower:
            case paused:
                block(500);
                break;
            case error:
                PriorityBlockingQueue<TaskBehaviour<?>> queue = agent.getTaskBehaviourQueue();
                TaskBehaviour<?> nextTask = queue.peek();
                int basePriority = (nextTask != null) ? nextTask.getPriority() : priority;

                TaskBehaviour<?> errorResolution = definedErrors.get(error);
                errorResolution.setPriority(basePriority + 2);
                queue.add(errorResolution);

                queue.add(resumeWith(basePriority + 1));
                status = Status.finished;
                break;
            case criticalError:
            case finished:
            default:
                break;
        }
    }

    @Override
    public int onEnd() {
        agent.addBehaviour(new RelinquishPowerBehaviour(agent, powerUsage, "disable-active"));
        if (status == Status.finished) {
            if (error == null) {
                agent.environmentService.addPerformedTask();
            }
        } else if (status == Status.criticalError) {
            agent.environmentService.addPerformedTaskError();
            agent.agentCommunicationController.sendError(agent.getLocalName(), taskName + " encountered a critical error that can't be resolved automatically.", false);
        }
        return super.onEnd();
    }

    @Override
    public boolean done() {
        return status == Status.finished || status == Status.criticalError;
    }

    public enum Status {
        waitingForPower, powerGranted, running, paused, error, criticalError, finished, powerRefused, poweringOff
    }
}
