package org.asaa.environment;

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

    private LocalDateTime simulationTime = LocalDateTime.now();

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
