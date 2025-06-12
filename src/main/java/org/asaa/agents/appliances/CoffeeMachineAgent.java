package org.asaa.agents.appliances;

import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.AwaitEnableBehaviour;
import org.asaa.behaviours.appliances.CoffeeMachineAgent.MessageHandlerBehaviour;
import org.asaa.behaviours.appliances.RequestPowerBehaviour;

public final class CoffeeMachineAgent extends SmartApplianceAgent {
    @Override
    protected void setup() {
        idleDraw = 5;
        activeDraw = 120;
        priority = 100;
        isInterruptible = false;

        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));

        addBehaviour(new RequestPowerBehaviour(this, idleDraw, priority, "enable-passive", ""));

        addBehaviour(new AwaitEnableBehaviour(this, awaitEnablePeriod, runnables, behaviours));
    }

    @Override
    protected String responseDefaultMsgContent() {
        return String.valueOf(isWorking);
    }
}
