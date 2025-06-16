package org.asaa.agents;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.asaa.behaviours.appliances.AwaitEnableBehaviour;
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
    protected int idleDraw = 0;
    protected int activeDraw = 0;

    protected final long awaitEnablePeriod = 1000;

    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));

        addBehaviour(new AwaitEnableBehaviour(this, awaitEnablePeriod, runnables, behaviours));
    }

    public final void subscribeSensor(AID aid, String sensorType) {
        subscribedSensors.computeIfAbsent(sensorType, k -> new ArrayList<>()).add(aid);
    }

    public void updateStatus() {
        agentCommunicationController.setAgentStatus(getName(),isEnabled,isWorking, getCurrentTask() == null || getCurrentTask().isInterruptible(), getCurrentTask() == null || getCurrentTask().isResumable(),activeDraw,idleDraw,priority);
    }

}
