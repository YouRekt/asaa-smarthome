package org.asaa.dto;

public record ACLMessageDTO(
        String aid,
        String performative,
        String conversationId,
        String message
) {}
