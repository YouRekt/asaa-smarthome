package org.asaa.controllers;

import org.asaa.dto.DeltaRequest;
import org.asaa.services.EnvironmentService;
import org.asaa.services.JadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        environmentService.initializePriceMaps();
        environmentService.startSimulation();
        jadeService.start();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stop() {
        environmentService.stopSimulation();
        jadeService.stop();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delta")
    public ResponseEntity<Void> setTime(@RequestBody DeltaRequest delta) {
        environmentService.setDelta(delta.getDelta());
        return ResponseEntity.ok().build();
    }
}
