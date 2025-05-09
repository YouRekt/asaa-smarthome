package org.asaa.agents.appliances;

import org.asaa.agents.SmartApplianceAgent;

public final class SmartLightbulbAgent extends SmartApplianceAgent {
    private boolean isTurnedOn = false;

    @Override
    protected void handleTrigger() {
        isTurnedOn = !isTurnedOn;
    }

    @Override
    protected String responseMsgContent() {
        return String.valueOf(isTurnedOn);
    }
}
