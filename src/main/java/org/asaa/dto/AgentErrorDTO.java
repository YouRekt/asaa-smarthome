package org.asaa.dto;

public record AgentErrorDTO(
        String timestamp,
        String sender,
        String message
) {
}
