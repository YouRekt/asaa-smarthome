package org.asaa.agents.coordinators;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import lombok.Getter;
import org.asaa.agents.SpringAwareAgent;
import org.asaa.behaviours.scheduler.ScheduleLoopBehaviour;
import org.asaa.exceptions.InvalidServiceSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public final class SchedulerAgent extends SpringAwareAgent {
    @Getter
    private AID coordinatorAgent;
    private final static Logger logger = LoggerFactory.getLogger("Scheduler");

    @Override
    protected void setup() {
        super.setup();
        logger.info("Initialized");

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        final ServiceDescription sd = new ServiceDescription();
        sd.setType("CoordinatorAgent");

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
