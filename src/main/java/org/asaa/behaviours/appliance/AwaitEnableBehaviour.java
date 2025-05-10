package org.asaa.behaviours.appliance;

import jade.core.behaviours.Behaviour;
import org.asaa.agents.SmartApplianceAgent;
import org.asaa.util.Util;

public class AwaitEnableBehaviour extends Behaviour {
    private final SmartApplianceAgent smartApplianceAgent;
    private final Runnable action;
    private boolean executed = false;

    public AwaitEnableBehaviour(SmartApplianceAgent smartApplianceAgent, Runnable action) {
        super(smartApplianceAgent);
        this.smartApplianceAgent = smartApplianceAgent;
        this.action = action;
    }

    @Override
    public void action() {
        if (smartApplianceAgent.isEnabled() && !executed) {
            action.run();
            executed = true;
        } else {
            block(Util.AWAIT_ENABLE_BLOCK_TIME);
        }
    }

    @Override
    public boolean done() {
        return executed;
    }
}
