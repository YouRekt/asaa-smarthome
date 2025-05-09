package org.asaa.behaviours.coordinator;

import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import org.asaa.agents.coordinators.CoordinatorAgent;
import org.asaa.exceptions.InvalidServiceSpecification;
import org.asaa.services.EnvironmentService;

import java.util.*;

public class AgentScanningBehaviour extends TickerBehaviour {
    private final CoordinatorAgent coordinatorAgent;
    private final EnvironmentService environmentService;

    public AgentScanningBehaviour(CoordinatorAgent coordinatorAgent, long period) {
        super(coordinatorAgent, period);
        this.coordinatorAgent = coordinatorAgent;
        this.environmentService = coordinatorAgent.environmentService;
    }

    @Override
    protected void onTick() {
        Set<String> areas = environmentService.getAllAreaNames();

        for (String area : areas) {
            Map<String, List<AID>> agents = new HashMap<>();
            final Property property = new Property();
            property.setName("areaName");
            property.setValue(area);

            final ServiceDescription sd = new ServiceDescription();
            sd.addProperties(property);

            try {
                final DFAgentDescription dfd = new DFAgentDescription();
                dfd.addServices(sd);
                final List<AID> foundAgents = Arrays.stream(DFService.search(coordinatorAgent, dfd)).map(DFAgentDescription::getName).toList();

                for (AID agent : foundAgents)
                    agents.put(agent.getClass().getSimpleName(), foundAgents);

                //coordinatorAgent.getLogger().info("Found {} agents:\n {}", foundAgents.size(), foundAgents);
                coordinatorAgent.getPhysicalAgents().put(environmentService.getArea(area), agents);
            } catch (FIPAException e) {
                throw new InvalidServiceSpecification(e);
            }
        }
    }
}
