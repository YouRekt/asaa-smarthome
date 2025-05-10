package org.asaa.services;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.asaa.environment.Area;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Accessors(chain = true)
public class EnvironmentService {
    private static final Logger logger = LoggerFactory.getLogger("Environment");
    private final Random rand = new Random();
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
    private int MAX_POWER_CAPACITY = 400;
    @Getter
    private int currentPowerConsumption = 0;
    // Money
    @Getter
    @Setter
    private int credits = 9999;
    @Getter
    @Setter
    private int timeDelta = 1;
    @Getter
    @Setter
    private LocalDateTime simulationTime;
    @Getter
    private ScheduledFuture<?> future;
    @Setter
    private boolean configProvided = false;

    public void startSimulation() {
        if (future != null && future.isDone()) return;
        if (!configProvided) {
            simulationTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 45));
            Area kitchen = new Area("kitchen");
            kitchen.setAttribute("temperature", 20.0);
            kitchen.setAttribute("human", false);
            addArea("kitchen", kitchen);
            initializePriceMaps();
        }
        future = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    public void stopSimulation() {
        if (future != null) {
            future.cancel(true);
            future = null;
            areas.clear();
            currentPowerConsumption = 0;
        }
    }

    private void tick() {
        simulationTime = simulationTime.plusMinutes(timeDelta);
    }

    public String getSimulationTimeString() {
        return simulationTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
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
        logger.info("Current power consumption is {}", currentPowerConsumption);
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

    public void initializePriceMaps() {
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
