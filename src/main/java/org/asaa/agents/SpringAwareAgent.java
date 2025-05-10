package org.asaa.agents;

import jade.core.Agent;
import org.asaa.services.EnvironmentService;
import org.asaa.util.SpringContext;
import org.springframework.context.ApplicationContext;

public abstract class SpringAwareAgent extends Agent {
    public EnvironmentService environmentService;

    @Override
    protected void setup() {
        super.setup();
        ApplicationContext context = SpringContext.get();
        if(context != null) {
            environmentService = context.getBean(EnvironmentService.class);
            System.out.println("Environment Service: " + environmentService);
        } else {
            System.err.println("Spring ApplicationContext is null!");
        }
    }
}
