package org.asaa.behaviours.scheduler;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.SchedulerAgent;
import org.asaa.services.EnvironmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.asaa.util.Util;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ScheduleLoopBehaviour extends TickerBehaviour {
    private final EnvironmentService env;
    private final Random rand = new Random();
    private final Map<String, Boolean> oneShotSchedules = new HashMap<>();
    private final Map<String, LocalDateTime> cyclicSchedules = new HashMap<>();
    private final SchedulerAgent schedulerAgent;
    private LocalDateTime previousTime;
    private LocalDateTime currentTime;

    public ScheduleLoopBehaviour(SchedulerAgent schedulerAgent, long period) {
        super(schedulerAgent, period);
        env = schedulerAgent.environmentService;
        currentTime = env.getSimulationTime();
        this.schedulerAgent = schedulerAgent;
        initSchedulesStatus();
    }

    // Add here scheduled events
    private void initSchedulesStatus() {
        // These usually will happen once per day
        oneShotSchedules.put("routine-morning", false);

        // These are more cyclical (once 30 minutes etc.)
        // cyclicSchedules.put("kitchen-temp", LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 45)));
    }

    // Reset all scheduled events so they can be executed again
    private void resetSchedulesStatus() {
        oneShotSchedules.forEach((key, value) -> {
            value = false;
        });
    }

    @Override
    public void onTick() {
        previousTime = currentTime;
        currentTime = env.getSimulationTime();
        // Time-based events should go here, the schedulerAgent will send messages to coordinator (? - TBD).

        // At 8AM perform Morning Schedule
        if (currentTime.getHour() >= 8 && !oneShotSchedules.get("routine-morning")) {
            oneShotSchedules.put("routine-morning", true);
            SchedulerAgent.getLogger().info("Morning schedule started, message sent to coordinator");
            Util.SendMessage(schedulerAgent, "", schedulerAgent.getCoordinatorAgent(), ACLMessage.INFORM, "routine-morning");
        }

        if (currentTime.toLocalDate().isAfter(previousTime.toLocalDate())) {
            SchedulerAgent.getLogger().info("Day has ended, resetting schedules status");
            resetSchedulesStatus();
        }
    }
}
