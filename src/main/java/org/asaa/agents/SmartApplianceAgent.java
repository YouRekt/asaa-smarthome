package org.asaa.agents;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import org.asaa.behaviours.appliance.HandleMessageBehaviour;
import org.asaa.environment.Area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class SmartApplianceAgent extends PhysicalAgent {
    protected final Map<String,List<AID>> subscribedSensors = new HashMap<>();

    public final void subscribeSensor(AID aid, String sensorType)
    {
        subscribedSensors.computeIfAbsent(sensorType, k -> new ArrayList<>()).add(aid);
    }
}
