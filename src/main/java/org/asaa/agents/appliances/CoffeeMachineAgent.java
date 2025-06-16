package org.asaa.agents.appliances;

import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.CoffeeMachineAgent.MessageHandlerBehaviour;

public final class CoffeeMachineAgent extends SmartApplianceAgent {
    @Override
    protected void setup() {
        idleDraw = 3;
        activeDraw = 122;
        priority = 100;

        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));
    }

}
