package com.strangequark.telemetryservice.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telemetry/health")
public class HealthController {
    @GetMapping()
    public ResponseEntity<String> healthcheck() {
        return ResponseEntity.ok("200 OK");
    }
}
