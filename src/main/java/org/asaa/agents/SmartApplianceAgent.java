package org.asaa.agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import lombok.Getter;
import lombok.Setter;
import org.asaa.behaviours.appliance.RelinquishPowerBehaviour;
import org.asaa.behaviours.appliance.RequestPowerBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SmartApplianceAgent extends PhysicalAgent {
    public final Map<String, Runnable> onPowerGrantedCallbacks = new ConcurrentHashMap<>();

    protected final Map<String, List<AID>> subscribedSensors = new HashMap<>();
    protected final List<Runnable> runnables = new ArrayList<>();
    protected final List<Behaviour> behaviours = new ArrayList<>();
    @Setter
    @Getter
    protected boolean isEnabled = false;
    @Setter
    @Getter
    protected boolean isWorking = false;
    @Setter
    @Getter
    protected boolean isInterruptible = true;
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
        logger.warn("I have been triggered without a purpose! {}", (isWorking ? "active -> idle" : "idle -> active"));
        if (isWorking) {
            addBehaviour(new RelinquishPowerBehaviour(this, activeDraw, "disable-active"));
        } else {
            addBehaviour(new RequestPowerBehaviour(this, activeDraw, priority, "enable-active", ""));
        }
    }
}
