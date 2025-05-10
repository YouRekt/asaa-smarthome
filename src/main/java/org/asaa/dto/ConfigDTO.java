package org.asaa.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConfigDTO {
    private Integer maxPowerCapacity;
    private Integer delta;
    private Integer credits;
    private LocalDateTime startTime;
    private List<PriceEntry> unitPrices;
    private List<PriceEntry> batchSizes;
    private List<AreaAttributesEntry> areaAttributes;

    @Data
    public static class PriceEntry {
        private String name;
        private Integer value;
    }

    @Data
    public static class AreaAttributesEntry {
        private String area;
        private List<Attribute> attributes;
    }

    @Data
    public static class Attribute {
        private String key;
        private Object value;
    }
}