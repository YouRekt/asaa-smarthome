package org.asaa.controllers;

import org.asaa.dto.AreaDTO;
import org.asaa.dto.EnvironmentDTO;
import org.asaa.services.EnvironmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class EnvironmentWebSocketController {
    private final static Logger logger = LoggerFactory.getLogger(EnvironmentWebSocketController.class);
    private final EnvironmentService environmentService;

    public EnvironmentWebSocketController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @MessageMapping("/env") // mapped to /app/env
    @SendTo("/topic/environment")
    public EnvironmentDTO getEnvironment() {
        EnvironmentDTO dto = createEnvironmentDTO();
        logger.info(dto.toString());
        return dto;
    }

    private EnvironmentDTO createEnvironmentDTO() {
        return new EnvironmentDTO(
                environmentService.getSimulationTimeString(),
                environmentService.getCredits(),
                environmentService.getTimeDelta(),
                environmentService.getMAX_POWER_CAPACITY(),
                environmentService.getCurrentPowerConsumption(),
                environmentService.getAllAreaNames().stream()
                        .map(name -> AreaDTO.from(environmentService.getArea(name)))
                        .toList()
        );
    }
}
