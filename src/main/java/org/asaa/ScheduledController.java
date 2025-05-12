package org.asaa;

import org.asaa.dto.AreaDTO;
import org.asaa.dto.EnvironmentDTO;
import org.asaa.services.EnvironmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledController {
    private final static Logger logger = LoggerFactory.getLogger(ScheduledController.class);
    private final MessageSendingOperations<String> messageSendingOperations;
    private final EnvironmentService environmentService;

    public ScheduledController(MessageSendingOperations<String> messageSendingOperations, EnvironmentService environmentService) {
        this.messageSendingOperations = messageSendingOperations;
        this.environmentService = environmentService;
    }

    @Scheduled(fixedDelay = 500)
    public void sendPeriodicMessages() {
        if (environmentService.getFuture() == null || environmentService.getFuture().isDone()) return;
        EnvironmentDTO dto = new EnvironmentDTO(environmentService.getSimulationTimeString(), environmentService.getCredits(), environmentService.getTimeDelta(), environmentService.getMAX_POWER_CAPACITY(), environmentService.getCurrentPowerConsumption(), environmentService.getAllAreaNames().stream().map(name -> AreaDTO.from(environmentService.getArea(name))).toList());

        this.messageSendingOperations.convertAndSend("/topic/environment", dto);
    }
}