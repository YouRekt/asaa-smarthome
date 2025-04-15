package org.asaa.agents.coordinators;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.environment.Area;
import org.asaa.environment.Environment;
import org.asaa.exceptions.InvalidServiceSpecification;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class CoordinatorAgent extends Agent {
    private final Map<Area, Map<String , List<AID>>> physicalAgents = new HashMap<>();
    private Logger logger;


    //TODO: Add periodic scanning behaviour for new agents
    @Override
    protected void setup() {
        logger = LogManager.getLogger(getLocalName());
        logger.info("Initialized");

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Set<String> areas = Environment.getInstance().getAllAreaNames();

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
                final List<AID> foundAgents = Arrays.stream(DFService.search(this, dfd)).map(DFAgentDescription::getName).toList();

                for (AID agent : foundAgents)
                    agents.put(agent.getClass().getSimpleName(), foundAgents);

                logger.info("Found {} agents:\n {}", foundAgents.size(), foundAgents);
                physicalAgents.put(Environment.getInstance().getArea(area), agents);
            } catch (FIPAException e) {
                throw new InvalidServiceSpecification(e);
            }
        }

    }
}
