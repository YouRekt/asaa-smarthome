package org.asaa.agents.appliances;

import org.asaa.agents.SmartApplianceAgent;

public final class SmartLightbulbAgent extends SmartApplianceAgent {

    @Override
    protected void handleTrigger() {
        isEnabled = !isEnabled;
    }

    @Override
    protected String responseDefaultMsgContent() {
        return String.valueOf(isEnabled);
    }
}
