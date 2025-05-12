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
import org.asaa.services.EnvironmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Arrays;

public final class SchedulerAgent extends SpringAwareAgent {
    @Getter
    private AID coordinatorAgent;
    @Getter
    private final static Logger logger = LoggerFactory.getLogger("Scheduler");

    @Override
    protected void setup() {
        super.setup();

        MDC.put("agent", "Scheduler");
        MDC.put("area", "");

        logger.info("Initialized");

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

    @Override
    protected void takeDown() {
        MDC.clear();
        super.takeDown();
    }
}
