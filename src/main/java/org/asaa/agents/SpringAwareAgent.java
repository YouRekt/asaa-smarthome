package org.asaa.agents;

import jade.core.Agent;
import org.asaa.services.EnvironmentService;
import org.asaa.util.AgentCommunicationController;
import org.asaa.util.AgentPresenceController;
import org.asaa.util.SpringContext;
import org.springframework.context.ApplicationContext;

public abstract class SpringAwareAgent extends Agent {
    public EnvironmentService environmentService;
    public AgentCommunicationController agentCommunicationController;
    public AgentPresenceController agentPresenceController;

    @Override
    protected void setup() {
        super.setup();
        ApplicationContext context = SpringContext.get();
        if(context != null) {
            environmentService = context.getBean(EnvironmentService.class);
            agentCommunicationController = context.getBean(AgentCommunicationController.class);
            agentPresenceController = context.getBean(AgentPresenceController.class);
//            System.out.println("Environment Service: " + environmentService);
        } else {
            System.err.println("Spring ApplicationContext is null!");
        }
    }
}
