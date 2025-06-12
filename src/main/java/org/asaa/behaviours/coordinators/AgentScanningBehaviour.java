package org.asaa.behaviours.coordinators;

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
import org.asaa.controllers.AgentPresenceController;

import java.util.*;

public class AgentScanningBehaviour extends TickerBehaviour {
    private final CoordinatorAgent agent;
    private final EnvironmentService environmentService;
    private final AgentPresenceController agentPresenceController;

    public AgentScanningBehaviour(CoordinatorAgent agent, long period) {
        super(agent, period);
        this.agent = agent;
        this.environmentService = agent.environmentService;
        this.agentPresenceController = agent.agentPresenceController;
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

                DFAgentDescription[] results = DFService.search(agent, dfd);

                for (DFAgentDescription desc : results) {
                    jade.util.leap.Iterator it = desc.getAllServices();
                    while (it.hasNext()) {
                        ServiceDescription service = (ServiceDescription) it.next();
                        String type = service.getType();

                        agents.computeIfAbsent(type, k -> new ArrayList<>()).add(desc.getName());
                    }
                }

                agent.getLogger().debug("Found {} agents in {}:\n {}", agents.size(), area, agents);

                Map<String, List<AID>> oldAgentsMap = agent.getPhysicalAgents().get(environmentService.getArea(area));
                if (oldAgentsMap != null) {
                    for (var entry : agents.entrySet()) {
                        String agentClass = entry.getKey();
                        List<AID> newList = entry.getValue();

                        List<AID> oldList = oldAgentsMap.getOrDefault(agentClass, Collections.emptyList());

                        for (AID aid : newList) {
                            if (!oldList.contains(aid)) {
                                agentPresenceController.updatePresence(aid.getName(), area, agentClass);
                            }
                        }
                    }
                } else {
                    for (var entry : agents.entrySet()) {
                        String agentClass = entry.getKey();
                        for (AID aid : entry.getValue()) {
                            agentPresenceController.updatePresence(aid.getName(), area, agentClass);
                        }
                    }
                }

                agent.getPhysicalAgents().put(environmentService.getArea(area), agents);
            } catch (FIPAException e) {
                throw new InvalidServiceSpecification(e);
            }
        }
    }
}
