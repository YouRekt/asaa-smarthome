package org.asaa.dto;

import java.util.List;

public record EnvironmentDTO(
        String time,
        int credits,
        int timeDelta,
        int maxPowerCapacity,
        int currentPowerConsumption,
        int performedTasks,
        int errorTasks,
        String humanLocation,
        List<AreaDTO> areas)
{}