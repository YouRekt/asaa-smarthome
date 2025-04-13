package org.asaa.environment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Simulator {
    private final static Logger logger = LogManager.getLogger("Simulator");

    public static void startSimulation() {
        new Thread(() -> {
            Random rand = new Random();
            Environment env = Environment.getInstance();
            Area kitchen = env.getArea("kitchen");

            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    env.advanceSimulationTime(Duration.ofMinutes(1));

                    if (Environment.getSimulationTime().getMinute() % 30 == 0) {
                        double newTemp = 18 + rand.nextDouble() * 6; // random temp 18–24
                        logger.info("Kitchen temperature updated to: {} °C", String.format("%.2f", newTemp));
                        kitchen.setAttribute("temperature", newTemp);
                    }
                    if (rand.nextInt(10) == 0) {
                        boolean isHumanInTheKitchen = (boolean) kitchen.getAttribute("human");
                        logger.info("Human {} the kitchen", !isHumanInTheKitchen ? "entered" : "left");
                        kitchen.setAttribute("human", !isHumanInTheKitchen);

                    }
                } catch (InterruptedException e) {
                    logger.error("Simulator interrupted", e);
                }
            }
        }).start();
    }
}
