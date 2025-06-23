package org.asaa.behaviours.coordinators.SchedulerAgent;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.SchedulerAgent;
import org.asaa.environment.Area;
import org.asaa.services.EnvironmentService;
import org.asaa.util.Util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class ScheduleLoopBehaviour extends TickerBehaviour {
    private final EnvironmentService env;
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
        cyclicSchedules.put("human-not-home-lights", env.getSimulationTime());
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
        if (currentTime.getHour() >= 8 && currentTime.getHour() <= 9 && !oneShotSchedules.get("routine-morning")) {
            oneShotSchedules.put("routine-morning", true);
            agent.getLogger().info("Morning schedule started, message sent to coordinator");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(agent.getCoordinatorAgent());
            msg.setConversationId("routine-morning");
            agent.sendMessage(msg);
        }

        if (env.getHumanLocation() == null && Duration.between(cyclicSchedules.get("human-not-home-lights"), currentTime).toMinutes() >= 5 ) {
            cyclicSchedules.put("human-not-home-lights", currentTime);
            agent.getLogger().info("Human is not home, turning random light on/off");
            Map.Entry<String, Area> randomArea = Util.getRandomEntry(env.getAreas());
            if (randomArea == null) {
                agent.getLogger().warn("ScheduleLoopBehaviour@onTick there are no areas in the environment");
                return;
            }
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(agent.getCoordinatorAgent());
            msg.setConversationId("human-not-home-lights");
            msg.setContent(randomArea.getValue().getName());
            agent.sendMessage(msg);
        }

        if (currentTime.toLocalDate().isAfter(previousTime.toLocalDate())) {
            agent.getLogger().info("Day has ended, resetting schedules status");
            resetSchedulesStatus();
        }
    }
}
