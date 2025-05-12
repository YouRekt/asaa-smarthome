package org.asaa.util;

import org.asaa.dto.AgentMessageDTO;
import org.asaa.services.EnvironmentService;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
public class AgentCommunicationController {
    private final MessageSendingOperations<String> messageSendingOperations;
    private final EnvironmentService environmentService;

    public AgentCommunicationController(MessageSendingOperations<String> messageSendingOperations, EnvironmentService environmentService) {
        this.messageSendingOperations = messageSendingOperations;
        this.environmentService = environmentService;
    }

    public void sendMessage(String agentName, String message) {
        AgentMessageDTO dto = new AgentMessageDTO(agentName, environmentService.getSimulationTimeString(), message);

        messageSendingOperations.convertAndSend("/topic/agent-message", dto);
    }

    public void sendError(String agentName, String message) {
        AgentMessageDTO dto = new AgentMessageDTO(agentName, environmentService.getSimulationTimeString(), message);

        messageSendingOperations.convertAndSend("/topic/agent-error", dto);
    }
}
