package org.asaa.controllers;

import org.asaa.dto.ACLMessageDTO;
import org.asaa.dto.AgentMessageDTO;
import org.asaa.dto.AgentStatusDTO;
import org.asaa.dto.HumanLocationDTO;
import org.asaa.services.EnvironmentService;
import org.asaa.services.HumanCommunicationService;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
        environmentService.addPerformedTaskError();
        AgentMessageDTO dto = new AgentMessageDTO(agentName, environmentService.getSimulationTimeString(), message);

        messageSendingOperations.convertAndSend("/topic/agent-error", dto);
    }

    public void setAgentStatus(String agentName, Boolean isEnabled, Boolean isWorking, Boolean isInterruptible, Boolean isFreezable, Integer activeDraw, Integer idleDraw, Integer priority) {
        AgentStatusDTO dto = new AgentStatusDTO(agentName, isEnabled, isWorking, isInterruptible, isFreezable, activeDraw, idleDraw, priority);
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
