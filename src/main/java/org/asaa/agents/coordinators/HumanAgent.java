package org.asaa.agents.coordinators;

import org.asaa.agents.PhysicalAgent;
import org.asaa.behaviours.human.HandleMessageBehaviour;

public final class HumanAgent extends PhysicalAgent {
    @Override
    protected void setup() {
        super.setup();

        addBehaviour(new HandleMessageBehaviour(this));
    }

    @Override
    protected void handleTrigger() {

    }

    @Override
    protected String responseDefaultMsgContent() {
        return "I am currently in " + areaName;
    }
}
