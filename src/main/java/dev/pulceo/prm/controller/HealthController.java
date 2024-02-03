package dev.pulceo.prm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

        @RequestMapping("")
        public ResponseEntity<String> health() {
            return ResponseEntity.status(200).body("OK");
        }
}
