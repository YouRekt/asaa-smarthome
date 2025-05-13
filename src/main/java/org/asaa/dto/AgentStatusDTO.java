package org.asaa.dto;

public record AgentStatusDTO(
        String aid,
        Boolean isEnabled,
        Boolean isWorking,
        Boolean isInterruptible,
        Boolean isFreezable,
        Integer activeDraw,
        Integer idleDraw,
        Integer priority
) {
}
