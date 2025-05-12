package org.asaa.agents.appliances;

import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliance.AwaitEnableBehaviour;
import org.asaa.behaviours.appliance.HandleMessageBehaviour;
import org.asaa.behaviours.appliance.RequestPowerBehaviour;

public final class SmartLightbulbAgent extends SmartApplianceAgent {
    @Override
    protected void setup() {
        idleDraw = 5;
        activeDraw = 0;
        priority = 100;

        super.setup();

        addBehaviour(new HandleMessageBehaviour(this));

        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));

        addBehaviour(new AwaitEnableBehaviour(this, awaitEnablePeriod, runnables, behaviours));
    }

    @Override
    protected String responseDefaultMsgContent() {
        return String.valueOf(isEnabled);
    }
}
