package org.asaa.agents.coordinators;

import jade.core.AID;
import jade.core.Agent;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.behaviours.coordinator.AgentScanningBehaviour;
import org.asaa.behaviours.coordinator.HandleMessageBehaviour;
import org.asaa.environment.Area;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public final class CoordinatorAgent extends Agent {
    private final Map<Area, Map<String , List<AID>>> physicalAgents = new HashMap<>();
    private Logger logger;

    @Override
    protected void setup() {
        logger = LogManager.getLogger(getLocalName());
        logger.info("Initialized");

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        addBehaviour(new AgentScanningBehaviour(this, 5000));

        addBehaviour(new HandleMessageBehaviour(this) {

        });

    }
}
