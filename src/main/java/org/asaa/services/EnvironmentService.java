package org.asaa.services;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.asaa.environment.Area;
import org.asaa.environment.TemperatureSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Accessors(chain = true)
public class EnvironmentService {
    private static final Logger logger = LoggerFactory.getLogger("Environment");
    private final Map<String, LocalDateTime> cyclicEvents = new HashMap<>();
    @Getter
    @Setter
    private Area humanLocation;
    @Getter
    @Setter
    private Map<String, Area> areas = new HashMap<>();
    @Getter
    @Setter
    private Map<String, Integer> unitPrice = new HashMap<>(); // price per single unit
    @Getter
    @Setter
    private Map<String, Integer> batchSize = new HashMap<>(); // minimal purchase batch size per item
    // Power
    @Getter
    @Setter
    private int MAX_POWER_CAPACITY = 500;
    @Getter
    private int currentPowerConsumption = 0;
    // Money
    @Getter
    @Setter
    private int credits = 9999;
    @Getter
    private int performedTasks = 0;
    @Getter
    private int performedTasksErrors = 0;
    @Getter
    @Setter
    private int timeDelta = 1;
    @Getter
    @Setter
    private LocalDateTime simulationTime;
    @Getter
    private ScheduledFuture<?> future;
    private ScheduledExecutorService executor;
    @Setter
    private boolean configProvided = false;

    public void startSimulation() {
        if (future != null && future.isDone()) return;
        if (!configProvided) {
            simulationTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 45));
            Area kitchen = new Area("kitchen");
            kitchen.setAttribute("temperature", 21.0);
            addArea("kitchen", kitchen);
            Area bathroom = new Area("bathroom");
            bathroom.setAttribute("temperature", 21.0);
            addArea("bathroom", bathroom);
            Area bedroom1 = new Area("bedroom 1");
            bedroom1.setAttribute("temperature", 2137.0);
            addArea("bedroom 1", bedroom1);
            Area bedroom2 = new Area("bedroom 2");
            bedroom2.setAttribute("temperature", 21.0);
            addArea("bedroom 2", bedroom2);
            Area bedroom3 = new Area("bedroom 3");
            bedroom3.setAttribute("temperature", 21.0);
            addArea("bedroom 3", bedroom3);
            Area livingRoom = new Area("living room");
            livingRoom.setAttribute("temperature", 21.0);
            addArea("living room", livingRoom);
            Area beforeRoom = new Area("before room");
            beforeRoom.setAttribute("temperature", 19.0);
            addArea("before room", beforeRoom);
            humanLocation = kitchen;
            initializePriceMaps();
            initializeCyclicEvents();
        }
        executor = Executors.newSingleThreadScheduledExecutor();
        future = executor.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    public void stopSimulation() {
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        future = null;
        executor = null;
        areas.clear();
        currentPowerConsumption = 0;
    }

    private void tick() {
        simulationTime = simulationTime.plusMinutes(timeDelta);

        // Every 30 minutes, change the Kitchen temperature
        /*
        REVISIT: Possible (example) changes
        - If all windows in the kitchen area are closed, the temperature shall not vary much (~0.1 degrees)
        - Running an oven might slowly increase kitchen temperature by a small amount (log function to have
          some ceiling, diminishing returns)
        - After planning out our home schema, open doors with rooms with substantial temperature differences
          can affect the temperature of the kitchen
         */
        if (Duration.between(cyclicEvents.get("kitchen-temp"), simulationTime).toMinutes() >= 30) {
            cyclicEvents.put("kitchen-temp", simulationTime);
            double newTemp = TemperatureSimulator.simulateRoomTemperature((double)getArea("kitchen").getAttribute("temperature"), simulationTime.getHour(), 14, 21.0, 25.0);
            getArea("kitchen").setAttribute("temperature", 23.0);
            logger.info("Kitchen temperature updated to: {} °C", String.format("%.2f", 23.0));
        }
    }

    public String getSimulationTimeString() {
        return simulationTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
    }

    public String getFormattedSimulationTime() {
        return simulationTime != null ? simulationTime.toString() : "SIM_TIME_UNSET";
    }

    public synchronized int getPowerAvailability() {
        return MAX_POWER_CAPACITY - currentPowerConsumption;
    }

    public synchronized void modifyPowerConsumption(int powerConsumption) {
        currentPowerConsumption += powerConsumption;
        if (currentPowerConsumption > MAX_POWER_CAPACITY) {
            logger.error("We went over MAX_POWER_CAPACITY, something had to go wrong!!! {}", currentPowerConsumption);
        } else if (currentPowerConsumption < 0) {
            logger.error("We went into negative power consumption, something had to go wrong!!! {}", currentPowerConsumption);
        }
        logger.info("Current power consumption is {}W", currentPowerConsumption);
    }

//    public synchronized int getCredits() {
//        return credits;
//    }

    /**
     * Try to buy exactly batchSize(item) units.
     *
     * @return number of units actually bought (0 if insufficient credits)
     */
    public int buyBatch(String itemName) {
        int batch = batchSize.getOrDefault(itemName, 1);
        int pricePerUnit = unitPrice.getOrDefault(itemName, Integer.MAX_VALUE);
        int totalCost = batch * pricePerUnit;
        if (credits >= totalCost) {
            credits -= totalCost;
            return batch;
        } else {
            return 0;
        }
    }

    /**
     * Try to buy up to 'needed' units, in allowed batch multiples.
     * e.g if batch = 6 and needed = 4, you end up buying 6.
     */
    public int buyNeeded(String itemName, int needed) {
        int batch = batchSize.getOrDefault(itemName, 1);
        int pricePerUnit = unitPrice.getOrDefault(itemName, Integer.MAX_VALUE);

        int batches = (needed + batch - 1) / batch;
        int cost = batches * batch * pricePerUnit;
        if (credits >= cost) {
            credits -= cost;
            return batches * batch;
        } else {
            if (credits >= batch * pricePerUnit) {
                credits -= batch * pricePerUnit;
                return batch;
            } else {
                return 0;
            }
        }
    }

    private void initializePriceMaps() {
        unitPrice.put("Milk", 2);
        unitPrice.put("Eggs", 3);
        unitPrice.put("Butter", 5);
        unitPrice.put("Cheese", 4);
        unitPrice.put("Yogurt", 1);
        unitPrice.put("Juice", 3);

        batchSize.put("Eggs", 6);
        batchSize.put("Milk", 1);
        batchSize.put("Butter", 1);
        batchSize.put("Cheese", 1);
        batchSize.put("Yogurt", 1);
        batchSize.put("Juice", 1);
    }

    private void initializeCyclicEvents() {
        cyclicEvents.put("kitchen-temp", LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 45)));
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

    public void addPerformedTask() {
        performedTasks++;
    }

    public void addPerformedTaskError() {
        performedTasksErrors++;
    }
}
