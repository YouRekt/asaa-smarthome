package org.asaa.agents.coordinators;

import jade.core.AID;
import lombok.Getter;
import org.asaa.agents.SpringAwareAgent;
import org.asaa.behaviours.scheduler.ScheduleLoopBehaviour;
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

        coordinatorAgent = findAgent("Coordinator", "");

        addBehaviour(new ScheduleLoopBehaviour(this, 500));
    }

    @Override
    protected void takeDown() {
        MDC.clear();
        super.takeDown();
    }
}
