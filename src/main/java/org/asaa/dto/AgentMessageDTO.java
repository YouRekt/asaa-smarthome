package org.asaa.dto;

import java.util.List;

public record AgentMessageDTO(
        String timestamp,
        String dtoSender,
        String sender,
        List<String> receiver,
        String performative,
        String conversationId,
        String content,
        boolean outgoing
) {
}
