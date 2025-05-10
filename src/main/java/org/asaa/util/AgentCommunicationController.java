package org.asaa.util;

import org.asaa.dto.AgentMessageDTO;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
public class AgentCommunicationController {
    private final MessageSendingOperations<String> messageSendingOperations;

    public AgentCommunicationController(MessageSendingOperations<String> messageSendingOperations) {
        this.messageSendingOperations = messageSendingOperations;
    }

    public void sendMessage(String agentName, String area, String message) {
        AgentMessageDTO dto = new AgentMessageDTO(agentName,area,message);

        messageSendingOperations.convertAndSend("/topic/agent",dto);
    }
}
