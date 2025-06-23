package org.asaa.agents.appliances;

import org.asaa.agents.base.SmartApplianceAgent;
import org.asaa.behaviours.appliances.SmartLightbulbAgent.LightTask;
import org.asaa.behaviours.appliances.SmartLightbulbAgent.MessageHandlerBehaviour;

public final class SmartLightbulbAgent extends SmartApplianceAgent {
    @Override
    protected void setup() {
        idleDraw = 1;
        activeDraw = 5;
        priority = 70;

        super.setup();

        addBehaviour(new MessageHandlerBehaviour(this));
    }

    @Override
    public void handleToggle(String message) {
        if (isEnabled) {
            if (currentTaskBehaviour == null || currentTaskBehaviour.done()) {
                taskBehaviourQueue.add(new LightTask(this, Long.parseLong(message)));
            } else {
                logger.warn("Task is already running {}", message);
                currentTaskBehaviour.interrupt(false, false);
            }
        } else {
            super.handleToggle(message);
        }
    }
}
