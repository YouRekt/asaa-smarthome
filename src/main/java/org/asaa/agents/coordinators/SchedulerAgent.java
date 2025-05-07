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
import org.asaa.behaviours.scheduler.ScheduleLoopBehaviour;
import org.asaa.exceptions.InvalidServiceSpecification;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SchedulerAgent extends Agent {
    @Getter
    private AID coordinatorAgent;
    private Logger logger;

    @Override
    protected void setup() {
        logger = LogManager.getLogger(getLocalName());
        logger.info("Initialized");

        final ServiceDescription sd = new ServiceDescription();
        sd.setType("CoordinatorAgent");

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            final DFAgentDescription dfd = new DFAgentDescription();
            dfd.addServices(sd);
            coordinatorAgent = Arrays.stream(DFService.search(this, dfd)).map(DFAgentDescription::getName).toList().getFirst();
            logger.info("Found coordinator agent {}", coordinatorAgent.getLocalName());
        } catch (FIPAException e) {
            throw new InvalidServiceSpecification(e);
        }

        addBehaviour(new ScheduleLoopBehaviour(this, 500));
    }
}
