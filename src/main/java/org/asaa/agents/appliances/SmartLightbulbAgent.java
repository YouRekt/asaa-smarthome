package org.asaa.agents.appliances;

import org.asaa.agents.SmartApplianceAgent;
import org.asaa.behaviours.appliances.SmartLightbulbAgent.MessageHandlerBehaviour;

public final class SmartLightbulbAgent extends SmartApplianceAgent {
    @Override
    protected void setup() {
        idleDraw = 1;
        activeDraw = 5;
        priority = 100;

        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));
    }

}
