package org.asaa.agents;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.util.Util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SmartApplianceAgent extends PhysicalAgent {
    public final Map<String, Runnable> onPowerGrantedCallbacks = new ConcurrentHashMap<>();

    protected final Map<String,List<AID>> subscribedSensors = new HashMap<>();
    @Setter @Getter
    protected boolean isEnabled = false;
    @Setter @Getter
    protected boolean isWorking = false;
    protected int idleDraw = 0;
    protected int activeDraw = 0;

    public final void subscribeSensor(AID aid, String sensorType) {
        subscribedSensors.computeIfAbsent(sensorType, k -> new ArrayList<>()).add(aid);
    }
}
