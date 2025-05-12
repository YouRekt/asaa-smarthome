package org.asaa.controllers;

import org.asaa.dto.ACLMessageDTO;
import org.asaa.dto.AgentMessageDTO;
import org.asaa.dto.EnvironmentDTO;
import org.asaa.services.EnvironmentService;
import org.asaa.services.HumanCommunicationService;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class AgentCommunicationController {
    private final MessageSendingOperations<String> messageSendingOperations;
    private final EnvironmentService environmentService;
    private final HumanCommunicationService humanCommunicationService;

    public AgentCommunicationController(MessageSendingOperations<String> messageSendingOperations, EnvironmentService environmentService, HumanCommunicationService humanCommunicationService) {
        this.messageSendingOperations = messageSendingOperations;
        this.environmentService = environmentService;
        this.humanCommunicationService = humanCommunicationService;
    }

    public void sendMessage(String agentName, String message) {
        AgentMessageDTO dto = new AgentMessageDTO(agentName, environmentService.getSimulationTimeString(), message);

        messageSendingOperations.convertAndSend("/topic/agent-message", dto);
    }

    public void sendError(String agentName, String message) {
        AgentMessageDTO dto = new AgentMessageDTO(agentName, environmentService.getSimulationTimeString(), message);

        messageSendingOperations.convertAndSend("/topic/agent-error", dto);
    }

    @MessageMapping("/agent-message") // mapped to /app/env
    public void readAgentMessage(@Payload ACLMessageDTO dto) {
        humanCommunicationService.receiveMessage(dto);
    }
}
