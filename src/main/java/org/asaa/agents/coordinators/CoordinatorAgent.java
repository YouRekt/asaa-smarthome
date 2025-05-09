package org.asaa.agents.coordinators;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import lombok.Getter;
import org.asaa.agents.SpringAwareAgent;
import org.asaa.behaviours.coordinator.AgentScanningBehaviour;
import org.asaa.behaviours.coordinator.HandleMessageBehaviour;
import org.asaa.environment.Area;
import org.asaa.exceptions.InvalidServiceSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Getter
public final class CoordinatorAgent extends SpringAwareAgent {
    private final Map<Area, Map<String , List<AID>>> physicalAgents = new HashMap<>();
    private final static Logger logger = LoggerFactory.getLogger("Coordinator");

    @Override
    protected void setup() {
        super.setup();
        logger.info("Initialized");

        registerCoordinatorAgent();

        addBehaviour(new AgentScanningBehaviour(this, 5000));

        addBehaviour(new HandleMessageBehaviour(this) {

        });

    }

    private void registerCoordinatorAgent() {
        final ServiceDescription sd = new ServiceDescription();
        sd.setType(getClass().getSimpleName());
        sd.setName(getLocalName());
        sd.setOwnership(getName());

        try {
            final DFAgentDescription dfd = new DFAgentDescription();
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            throw new InvalidServiceSpecification(e);
        }
    }
}
