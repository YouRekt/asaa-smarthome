package org.asaa.dto;

import org.asaa.environment.Area;

import java.util.Map;

public record AreaDTO(
        String name,
        Map<String, Object> attributes
) {
    public static AreaDTO from(Area area) {
        return new AreaDTO(area.getName(), area.getAllAttributes());
    }
}