package org.asaa.behaviours.scheduler;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.SchedulerAgent;
import org.asaa.services.EnvironmentService;
import org.asaa.util.Util;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ScheduleLoopBehaviour extends TickerBehaviour {
    private final EnvironmentService env;
    private final Random rand = new Random();
    private final Map<String, Boolean> oneShotSchedules = new HashMap<>();
    private final Map<String, LocalDateTime> cyclicSchedules = new HashMap<>();
    private final SchedulerAgent agent;
    private LocalDateTime previousTime;
    private LocalDateTime currentTime;

    public ScheduleLoopBehaviour(SchedulerAgent agent, long period) {
        super(agent, period);
        env = agent.environmentService;
        currentTime = env.getSimulationTime();
        this.agent = agent;
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
            agent.getLogger().info("Morning schedule started, message sent to coordinator");
            Util.SendMessage(agent, "", agent.getCoordinatorAgent(), ACLMessage.INFORM, "routine-morning");
        }

        if (currentTime.toLocalDate().isAfter(previousTime.toLocalDate())) {
            agent.getLogger().info("Day has ended, resetting schedules status");
            resetSchedulesStatus();
        }
    }
}
