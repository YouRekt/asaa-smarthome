package org.asaa.agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.behaviours.appliance.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliance.RequestPowerBehaviour;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SmartApplianceAgent extends PhysicalAgent {
    public final Map<String, Runnable> onPowerGrantedCallbacks = new ConcurrentHashMap<>();

    protected final Map<String, List<AID>> subscribedSensors = new HashMap<>();
    protected final List<Runnable> runnables = new ArrayList<>();
    protected final List<Behaviour> behaviours = new ArrayList<>();
    @Getter
    private final Queue<ACLMessage> pendingCfpQueue = new LinkedList<>();

    @Setter
    @Getter
    private boolean cfpInProgress = false;

    @Setter
    @Getter
    protected boolean isEnabled = false;
    @Setter
    @Getter
    protected boolean isWorking = false;
    @Setter
    @Getter
    protected boolean isInterruptible = true;
    @Setter
    @Getter
    protected boolean isFreezable = false;
    @Getter
    protected int idleDraw = 0;
    @Getter
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
                logger.warn("Forced shutdown while performing an active task!!!");
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

}
