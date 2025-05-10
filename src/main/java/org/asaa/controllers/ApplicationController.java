package org.asaa.controllers;

import org.asaa.dto.ConfigDTO;
import org.asaa.environment.Area;
import org.asaa.services.EnvironmentService;
import org.asaa.services.JadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/system")
public class ApplicationController {

    private final EnvironmentService environmentService;

    private final JadeService jadeService;

    public ApplicationController(EnvironmentService environmentService, JadeService jadeService) {
        this.environmentService = environmentService;
        this.jadeService = jadeService;
    }

    @PostMapping("/start")
    public ResponseEntity<Void> start() {
        environmentService.startSimulation();
        jadeService.start();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/config")
    public ResponseEntity<Void> config(@RequestBody ConfigDTO config) {
        Map<String, Integer> unitPrice = new HashMap<>();
        Map<String, Integer> batchSize = new HashMap<>();
        Map<String, Area> areas = new HashMap<>();

        for (ConfigDTO.PriceEntry entry : config.getUnitPrices()) {
            unitPrice.put(entry.getName(), entry.getValue());
        }

        for (ConfigDTO.PriceEntry entry : config.getBatchSizes()) {
            batchSize.put(entry.getName(), entry.getValue());
        }

        for (ConfigDTO.AreaAttributesEntry entry : config.getAreaAttributes()) {
            Area area = areas.computeIfAbsent(entry.getArea(), Area::new);

            for (ConfigDTO.Attribute attr : entry.getAttributes()) {
                area.setAttribute(attr.getKey(), attr.getValue());
            }
        }

        environmentService.setMAX_POWER_CAPACITY(config.getMaxPowerCapacity()).setCredits(config.getCredits()).setTimeDelta(config.getDelta()).setSimulationTime(config.getStartTime()).setUnitPrice(unitPrice).setBatchSize(batchSize).setAreas(areas).setConfigProvided(true);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/config/partial")
    public ResponseEntity<Void> updatePartialConfig(
            @RequestBody ConfigDTO config,
            @RequestParam List<String> fields) {

        if (fields.contains("maxPowerCapacity") && config.getMaxPowerCapacity() != null) {
            environmentService.setMAX_POWER_CAPACITY(config.getMaxPowerCapacity());
        }

        if (fields.contains("credits") && config.getCredits() != null) {
            environmentService.setCredits(config.getCredits());
        }

        if (fields.contains("delta") && config.getDelta() != null) {
            environmentService.setTimeDelta(config.getDelta());
        }

        if (fields.contains("startTime") && config.getStartTime() != null) {
            environmentService.setSimulationTime(config.getStartTime());
        }

        if (fields.contains("unitPrices") && config.getUnitPrices() != null) {
            Map<String, Integer> unitPrice = new HashMap<>();
            for (ConfigDTO.PriceEntry entry : config.getUnitPrices()) {
                unitPrice.put(entry.getName(), entry.getValue());
            }
            environmentService.setUnitPrice(unitPrice);
        }

        if (fields.contains("batchSizes") && config.getBatchSizes() != null) {
            Map<String, Integer> batchSize = new HashMap<>();
            for (ConfigDTO.PriceEntry entry : config.getBatchSizes()) {
                batchSize.put(entry.getName(), entry.getValue());
            }
            environmentService.setBatchSize(batchSize);
        }

        if (fields.contains("areaAttributes") && config.getAreaAttributes() != null) {
            Map<String, Area> areas = new HashMap<>();
            for (ConfigDTO.AreaAttributesEntry entry : config.getAreaAttributes()) {
                Area area = areas.computeIfAbsent(entry.getArea(), Area::new);
                for (ConfigDTO.Attribute attr : entry.getAttributes()) {
                    area.setAttribute(attr.getKey(), attr.getValue());
                }
            }
            environmentService.setAreas(areas);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stop() {
        jadeService.stop();
        environmentService.stopSimulation();
        return ResponseEntity.ok().build();
    }
}
