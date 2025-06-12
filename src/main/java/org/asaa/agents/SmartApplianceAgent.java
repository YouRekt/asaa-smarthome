package org.asaa.agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.behaviours.appliances.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliances.RequestPowerBehaviour;
import org.asaa.tasks.Task;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class SmartApplianceAgent extends PhysicalAgent {
    public final Map<String, Runnable> onPowerGrantedCallbacks = new ConcurrentHashMap<>();

    protected final Map<String, List<AID>> subscribedSensors = new HashMap<>();
    protected final List<Runnable> runnables = new ArrayList<>();
    protected final Map<String, Behaviour> behaviours = new HashMap<>();
    private final Queue<ACLMessage> pendingCfpQueue = new LinkedList<>();
    @Setter
    protected Task currentTask = null;

    @Setter
    private boolean cfpInProgress = false;

    @Setter
    protected boolean isEnabled = false;
    @Setter
    protected boolean isWorking = false;
    @Setter
    protected boolean isInterruptible = true;
    @Setter
    protected boolean isFreezable = false;
    protected int idleDraw = 0;
    protected int activeDraw = 0;

    protected final long awaitEnablePeriod = 1000;

    public final void subscribeSensor(AID aid, String sensorType) {
        subscribedSensors.computeIfAbsent(sensorType, k -> new ArrayList<>()).add(aid);
    }

    @Override
    protected void handleTrigger() {
        logger.warn("I have been triggered externally: {}", (isWorking ? "active -> idle" : "idle -> active"));
        if (isWorking) {
            addBehaviour(new RelinquishPowerBehaviour(this, activeDraw, "disable-active"));
        } else {
            addBehaviour(new RequestPowerBehaviour(this, activeDraw, priority, "enable-active", ""));
        }
    }

    public void toggle() {
        handleToggle();
    }

    protected void handleToggle() {
        logger.warn("I have been {} externally: {}", (isEnabled ? "disabled" : "enabled"), (isWorking ? "active -> disabled" : "idle -> disabled"));
        if (isEnabled) {
            if (isWorking) {
                logger.error("Forced shutdown while performing an active task!!!");
                agentCommunicationController.sendError(getName(), "Forced shutdown while performing an active task");
                addBehaviour(new RelinquishPowerBehaviour(this, activeDraw, "disable-active"));
            }
            addBehaviour(new RelinquishPowerBehaviour(this, idleDraw, "disable-passive"));
        } else {
            addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));
        }
    }

    public void updateStatus()
    {
        agentCommunicationController.setAgentStatus(getName(),isEnabled,isWorking,isInterruptible,isFreezable,activeDraw,idleDraw,priority);
    }

    public void requestStartTask(Task task) {
        if (!this.isEnabled) {
            logger.warn("Request Start Task was called when the appliance is disabled!");
            return;
        }
        String replyWith = "req-" + System.currentTimeMillis();
        onPowerGrantedCallbacks.put(replyWith, task::start);
        addBehaviour(new RequestPowerBehaviour(this, activeDraw, priority, "enable-active", replyWith));
    }

}
