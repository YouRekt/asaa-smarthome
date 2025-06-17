package org.asaa.controllers;

import org.asaa.dto.*;
import org.asaa.services.EnvironmentService;
import org.asaa.services.HumanCommunicationService;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.List;

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

    public void sendMessage(String currentAgent, String sender, List<String> receiver, String performative, String conversationId, String content, boolean outgoing) {
        AgentMessageDTO dto = new AgentMessageDTO(environmentService.getSimulationTimeString(), currentAgent, sender, receiver, performative, conversationId, content, outgoing);

        messageSendingOperations.convertAndSend("/topic/agent-message", dto);
    }

    public void sendError(String sender, String message) {
        environmentService.addPerformedTaskError();
        AgentErrorDTO dto = new AgentErrorDTO(environmentService.getSimulationTimeString(), sender, message);

        messageSendingOperations.convertAndSend("/topic/agent-error", dto);
    }

    public void setAgentStatus(String agentName, Boolean isEnabled, Boolean isWorking, Boolean isTaskInterruptible, Boolean isTaskResumable, Integer activeDraw, Integer idleDraw, Integer priority) {
        AgentStatusDTO dto = new AgentStatusDTO(agentName, isEnabled, isWorking, isTaskInterruptible, isTaskResumable, activeDraw, idleDraw, priority);
        messageSendingOperations.convertAndSend("/topic/agent-status", dto);
    }

    @MessageMapping("/agent-message")
    public void readAgentMessage(@Payload ACLMessageDTO dto) {
        humanCommunicationService.receiveMessage(dto);
    }

    @MessageMapping("/human-location")
    public void updateHumanLocation(@Payload HumanLocationDTO dto) {
        environmentService.setHumanLocation(environmentService.getArea(dto.area()));
    }
}
