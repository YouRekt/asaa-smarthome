package org.asaa.environment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Environment {
    private static Environment instance;

    private final Map<String, Area> areas = new HashMap<>();
    private final Logger logger = LogManager.getLogger(Environment.class);

    private LocalDateTime simulationTime = LocalDateTime.now();

    private final int MAX_POWER_CAPACITY = 2000;
    private int currentPowerConsumption = 0;

    private Environment() {
    }

    public static synchronized Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
        }
        return instance;
    }

    public static synchronized LocalDateTime getSimulationTime() {
        return getInstance().simulationTime;
    }

    public static synchronized String getSimulationTimeString() {
        return getSimulationTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
    }

    public synchronized void advanceSimulationTime(Duration duration) {
        simulationTime = simulationTime.plus(duration);
    }

    public synchronized int getPowerAvailability() {
        return MAX_POWER_CAPACITY - currentPowerConsumption;
    }

    public synchronized void modifyPowerConsumption(int powerConsumption) {
        currentPowerConsumption += powerConsumption;
        if (currentPowerConsumption > MAX_POWER_CAPACITY) {
            logger.error("We went over MAX_POWER_CAPACITY, something had to go wrong!!!");
            currentPowerConsumption = MAX_POWER_CAPACITY;
        } else if (currentPowerConsumption < 0) {
            logger.error("We went into negative power consumption, something had to go wrong!!!");
            currentPowerConsumption = 0;
        }
    }

    public void addArea(String name, Area area) {
        areas.put(name, area);
    }

    public Area getArea(String name) {
        return areas.get(name);
    }

    public Set<String> getAllAreaNames() {
        return areas.keySet();
    }
}
