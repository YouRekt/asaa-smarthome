package org.asaa.controllers;

import org.asaa.dto.ConfigDTO;
import org.asaa.dto.SystemStatusDTO;
import org.asaa.environment.Area;
import org.asaa.services.EnvironmentService;
import org.asaa.services.JadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/system")
public class ApplicationController {

    private final EnvironmentService environmentService;
    private final MessageSendingOperations<String> messageSendingOperations;
    private final JadeService jadeService;

    public ApplicationController(EnvironmentService environmentService, MessageSendingOperations<String> messageSendingOperations, JadeService jadeService) {
        this.environmentService = environmentService;
        this.messageSendingOperations = messageSendingOperations;
        this.jadeService = jadeService;
    }

    @PostMapping("/start")
    public ResponseEntity<Void> start() {
        messageSendingOperations.convertAndSend("/topic/system",new SystemStatusDTO("starting"));
        environmentService.startSimulation();
        jadeService.start();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/config")
    public ResponseEntity<Void> config(@RequestBody ConfigDTO config) {
        Map<String, Area> areas = new HashMap<>();

        for (ConfigDTO.AreaEntry areaEntry : config.getAreas()) {
            Area area = new Area(areaEntry.getName());

            for (Map.Entry<String, Object> attr : areaEntry.getAttributes().entrySet()) {
                area.setAttribute(attr.getKey(), Double.parseDouble(attr.getValue().toString()));
            }

            areas.put(areaEntry.getName(), area);
        }

        environmentService.setCredits(config.getCredits());

        environmentService.setMAX_POWER_CAPACITY(config.getMaxPowerCapacity());

        environmentService.setSimulationTime(config.getSimulationStartTime());

        jadeService.configureAgentsFromDTO(config.getAgents());

        environmentService.setAreas(areas);

        environmentService.setConfigProvided(true);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stop() {
        messageSendingOperations.convertAndSend("/topic/system",new SystemStatusDTO("stopping"));
        jadeService.stop();
        environmentService.stopSimulation();
        return ResponseEntity.ok().build();
    }
}
