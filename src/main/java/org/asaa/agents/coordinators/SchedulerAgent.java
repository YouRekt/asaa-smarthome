package org.asaa.agents.coordinators;

import jade.core.AID;
import lombok.Getter;
import org.asaa.agents.base.SpringAwareAgent;
import org.asaa.behaviours.coordinators.SchedulerAgent.ScheduleLoopBehaviour;
import org.slf4j.MDC;

@Getter
public final class SchedulerAgent extends SpringAwareAgent {
    private AID coordinatorAgent;

    @Override
    protected void setup() {
        super.setup();

        MDC.put("agent", "Scheduler");
        MDC.put("area", "----------");

        logger.info("Initialized");

        coordinatorAgent = findAgent("CoordinatorAgent", "", true);

        addBehaviour(new ScheduleLoopBehaviour(this, 500));
    }

    @Override
    protected void takeDown() {
        MDC.clear();
        super.takeDown();
    }
}
