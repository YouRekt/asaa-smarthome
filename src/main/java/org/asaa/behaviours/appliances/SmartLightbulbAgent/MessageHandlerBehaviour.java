package org.asaa.behaviours.appliances.SmartLightbulbAgent;

import org.asaa.agents.appliances.SmartLightbulbAgent;

public class MessageHandlerBehaviour extends org.asaa.behaviours.appliances.MessageHandlerBehaviour {
    private final SmartLightbulbAgent agent;

    public MessageHandlerBehaviour(SmartLightbulbAgent agent) {
        super(agent);
        this.agent = agent;
    }
}
