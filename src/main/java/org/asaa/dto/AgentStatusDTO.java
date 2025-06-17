package org.asaa.dto;

public record AgentStatusDTO(
        String aid,
        Boolean isEnabled,
        Boolean isWorking,
        Boolean isTaskInterruptible,
        Boolean isTaskResumable,
        Integer activeDraw,
        Integer idleDraw,
        Integer priority
) {
}
