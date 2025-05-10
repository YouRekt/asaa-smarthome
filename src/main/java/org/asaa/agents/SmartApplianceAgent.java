package org.asaa.agents;

import jade.core.AID;
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
    @Setter
    @Getter
    protected boolean isEnabled = false;
    @Setter
    @Getter
    protected boolean isWorking = false;
    @Setter
    @Getter
    protected int priority = 0;
    protected int idleDraw = 0;
    protected int activeDraw = 0;

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
