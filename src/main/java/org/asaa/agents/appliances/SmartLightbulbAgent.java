package org.asaa.agents.appliances;

import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.behaviours.appliances.SmartLightbulbAgent.MessageHandlerBehaviour;
import org.asaa.tasks.appliances.SmartLightbultAgent.LightTask;

public final class SmartLightbulbAgent extends SmartApplianceAgent {
    @Override
    protected void setup() {
        idleDraw = 1;
        activeDraw = 5;
        priority = 100;

        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));
    }

    @Override
    public void handleToggle(String message) {
        if (isEnabled) {
            if (currentTask == null) {
                new LightTask(this, Long.parseLong(message)).start();
            } else {
                logger.error("LightTask is already running {}",message);
                currentTask.interrupt(message != null && !message.isEmpty());
            }
        } else {
            super.handleToggle(message);
        }
    }
}
