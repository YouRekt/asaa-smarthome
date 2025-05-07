package org.asaa.behaviours.scheduler;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asaa.agents.coordinators.SchedulerAgent;
import org.asaa.environment.Environment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ScheduleLoopBehaviour extends TickerBehaviour {
    private final Environment env;
    private final Random rand = new Random();
    // TODO: PLEASE THINK OF SOME BETTER NAMES FOR THESE MAPS...
    private final Map<String, Boolean> oneTimeSchedules = new HashMap<>();
    private final Map<String, LocalDateTime> lastSchedulesExecutionTime = new HashMap<>();
    private final SchedulerAgent schedulerAgent;
    private final Logger logger;

    public ScheduleLoopBehaviour(SchedulerAgent schedulerAgent, long period) {
        super(schedulerAgent, period);
        env = Environment.getInstance();
        this.schedulerAgent = schedulerAgent;
        logger = LogManager.getLogger(getBehaviourName());
        initSchedulesStatus();
    }

    // <> Add here scheduled events
    private void initSchedulesStatus() {
        // These usually will happen once per day
        oneTimeSchedules.put("routine-morning", false);

        // These are more cyclical (once 30 minutes etc.)
        lastSchedulesExecutionTime.put("kitchen-temp", LocalDateTime.now());
    }

    //  <> Reset all scheduled events so they can be executed again
    private void resetSchedulesStatus() {
        oneTimeSchedules.forEach((key, value) -> {value = false;});
    }

    @Override
    public void onTick() {
        LocalDateTime currentTime = Environment.getSimulationTime();
        // Time-based events should go here, the schedulerAgent will send messages to coordinator (? - TBD).

        /*
        <> Every 30 minutes, change the Kitchen temperature
        REVISIT: Possible (example) changes
        - If all windows in the kitchen area are closed, the temperature shall not vary much (~0.1 degrees)
        - Running an oven might slowly increase kitchen temperature by a small amount (log function to have
          some ceiling, diminishing returns)
        - After planning out our home schema, open doors with rooms with substantial temperature differences
          can affect the temperature of the kitchen
        CRITICAL: The scheduler agent might "miss" a certain time (eg. it fails to catch exactly mod(30) == 0)
        TODO: Implement something storing previous time function was executed, check if it has been over 30 minutes.
        */
//        if (currentTime.getMinute() % 30 == 0) {
//            double newTemp = 21 + rand.nextDouble() * 6;
//            env.getArea("kitchen").setAttribute("temperature", newTemp);
//            logger.info("Kitchen temperature updated to: {} °C", String.format("%.2f", newTemp));
//        }
        // TODO: Check whether this works as outlined above, this is a possible solution
        if (Duration.between(lastSchedulesExecutionTime.get("kitchen-temp"), currentTime).toMinutes() >= 30)
        {
            lastSchedulesExecutionTime.put("kitchen-temp", currentTime);
            double newTemp = 21 + rand.nextDouble() * 6;
            env.getArea("kitchen").setAttribute("temperature", newTemp);
            logger.info("Kitchen temperature updated to: {} °C", String.format("%.2f", newTemp));
        }

        /*
        <> At 8AM perform Morning Schedule
         */
        if (currentTime.getHour() >= 8 && !oneTimeSchedules.get("routine-morning")) {
            oneTimeSchedules.put("routine-morning", true);
            startMorningSchedule();
        }

        // TODO: Check if the day has ended and call resetSchedulesStatus()
    }

    private void startMorningSchedule() {
        // CRITICAL: Think about how we are going to filter different messages:
        // Currently we use performatives, but that means we need to include all info (so what to do)
        // in the message content, which includes annoying parsing every single time
        // Would be better to use ConversationIds? Then we use separate functions for everything -> much simpler
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(schedulerAgent.getCoordinatorAgent());
        schedulerAgent.send(msg);
        logger.info("Morning schedule started, message sent to coordinator");
    }
}
