package org.asaa.util;

import org.asaa.dto.AgentDTO;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
public class AgentPresenceController {
    private final MessageSendingOperations<String> messageSendingOperations;

    public AgentPresenceController(MessageSendingOperations<String> messageSendingOperations) {
        this.messageSendingOperations = messageSendingOperations;
    }

    public void updatePresence(String aid, String area, String agentName) {
        AgentDTO dto = new AgentDTO(aid, area, agentName);

        messageSendingOperations.convertAndSend("/topic/agent", dto);
    }
}
