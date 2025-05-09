package org.asaa.services;

import lombok.Getter;
import lombok.Setter;
import org.asaa.environment.Area;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class EnvironmentService {
    private static final Logger logger = LoggerFactory.getLogger("Environment");
    private final Map<String, Area> areas = new HashMap<>();
    private final Random rand = new Random();
    @Setter
    private int delta = 1;

    @Getter
    private LocalDateTime simulationTime;
    private ScheduledFuture<?> future;

    public void startSimulation() {
        if (future != null && future.isDone()) return;

        simulationTime = LocalDateTime.now();
        Area kitchen = new Area("kitchen");
        kitchen.setAttribute("temperature", 20.0);
        kitchen.setAttribute("human", false);
        addArea("kitchen", kitchen);
        future = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    public void stopSimulation() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    private void tick() {
        simulationTime = simulationTime.plusMinutes(delta);

        if (simulationTime.getMinute() % 30 == 0) {
            double newTemp = 21 + rand.nextDouble() * 6; // random temp 18–24
            logger.info("Kitchen temperature updated to: {} °C", String.format("%.2f", newTemp));
            getArea("kitchen").setAttribute("temperature", newTemp);
        }
        if (rand.nextInt(10) == 0) {
            boolean isHumanInTheKitchen = (boolean) getArea("kitchen").getAttribute("human");
            logger.info("Human {} the kitchen", !isHumanInTheKitchen ? "entered" : "left");
            getArea("kitchen").setAttribute("human", !isHumanInTheKitchen);
        }
    }

    public String getSimulationTimeString() {
        return simulationTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
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
