package org.asaa.agents.coordinators;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.behaviours.coordinator.AgentScanningBehaviour;
import org.asaa.behaviours.coordinator.HandleMessageBehaviour;
import org.asaa.environment.Area;
import org.asaa.exceptions.InvalidServiceSpecification;

import java.util.*;

@Getter
public final class CoordinatorAgent extends Agent {
    private final Map<Area, Map<String , List<AID>>> physicalAgents = new HashMap<>();
    private Logger logger;

    @Override
    protected void setup() {
        logger = LogManager.getLogger(getLocalName());
        logger.info("Initialized");

        registerCoordinatorAgent();

        addBehaviour(new AgentScanningBehaviour(this, 5000));
        addBehaviour(new HandleMessageBehaviour(this));
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

    public void performMorningRoutine() {
        /*
        TODO: Implement functionality:
        - Check if human is home
        - Based on the day of the week maybe do different stuff
        - Some common functionalities:
            * Start making coffee
            * Notify the user about today's weather and upcoming events
            * Open the blinds
            * Play morning playlist
            * Perform resource check (for simplicity now just check the fridge and maybe order missing items)
         */
    }
}
