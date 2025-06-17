package org.asaa.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ConfigDTO {
    private List<AreaEntry> areas;
    private List<AgentEntry> agents;

    @Data
    public static class AreaEntry {
        private String name;
        private Map<String, Object> attributes;
    }

    @Data
    public static class AgentEntry {
        private String aid;
        private String area;
        private String name;
    }
}