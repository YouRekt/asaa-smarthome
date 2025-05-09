package org.asaa.agents.appliances;

import org.asaa.agents.SmartApplianceAgent;

public final class CoffeeMachineAgent extends SmartApplianceAgent {

    @Override
    protected void setup() {
        super.setup();
        activeDraw = 120;
    }

    @Override
    protected void handleTrigger() {

    }

    @Override
    protected String responseMsgContent() {
        return "";
    }
}
