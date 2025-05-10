package org.asaa.behaviours.scheduler;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.asaa.agents.coordinators.SchedulerAgent;
import org.asaa.services.EnvironmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.asaa.util.Util;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ScheduleLoopBehaviour extends TickerBehaviour {
    private final EnvironmentService env;
    private final Random rand = new Random();
    private final Map<String, Boolean> oneShotSchedules = new HashMap<>();
    private final Map<String, LocalDateTime> cyclicSchedules = new HashMap<>();
    private final SchedulerAgent schedulerAgent;
    private final Logger logger;

    public ScheduleLoopBehaviour(SchedulerAgent schedulerAgent, long period) {
        super(schedulerAgent, period);
        env = schedulerAgent.environmentService;
        this.schedulerAgent = schedulerAgent;
        logger = LoggerFactory.getLogger(getBehaviourName());
        initSchedulesStatus();
    }

    // Add here scheduled events
    private void initSchedulesStatus() {
        // These usually will happen once per day
        oneShotSchedules.put("routine-morning", false);

        // These are more cyclical (once 30 minutes etc.)
        cyclicSchedules.put("kitchen-temp", LocalDateTime.now());
    }

    // Reset all scheduled events so they can be executed again
    private void resetSchedulesStatus() {
        oneShotSchedules.forEach((key, value) -> {
            value = false;
        });
    }

    @Override
    public void onTick() {
        LocalDateTime currentTime = env.getSimulationTime();
        // Time-based events should go here, the schedulerAgent will send messages to coordinator (? - TBD).


        // Every 30 minutes, change the Kitchen temperature
        /*
        REVISIT: Possible (example) changes
        - If all windows in the kitchen area are closed, the temperature shall not vary much (~0.1 degrees)
        - Running an oven might slowly increase kitchen temperature by a small amount (log function to have
          some ceiling, diminishing returns)
        - After planning out our home schema, open doors with rooms with substantial temperature differences
          can affect the temperature of the kitchen
         */
        if (Duration.between(cyclicSchedules.get("kitchen-temp"), currentTime).toMinutes() >= 30) {
            cyclicSchedules.put("kitchen-temp", currentTime);
            double newTemp = 21 + rand.nextDouble() * 2;
            env.getArea("kitchen").setAttribute("temperature", newTemp);
            logger.info("Kitchen temperature updated to: {} °C", String.format("%.2f", newTemp));
        }

        // At 8AM perform Morning Schedule
        if (currentTime.getHour() >= 8 && !oneShotSchedules.get("routine-morning")) {
            oneShotSchedules.put("routine-morning", true);
            logger.info("Morning schedule started, message sent to coordinator");
            Util.SendMessage(myAgent, "", schedulerAgent.getCoordinatorAgent(), ACLMessage.INFORM, "routine-morning");
        }

        // TODO: Check if the day has ended and call resetSchedulesStatus()
    }

    private double computeTemp(int hour, double avg, double amp, int peakHour) {
        double radians = 2 * Math.PI * (hour - peakHour) / 24.0;
        return avg + amp * Math.sin(radians);
    }
}
