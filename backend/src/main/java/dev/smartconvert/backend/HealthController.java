package dev.smartconvert.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public HealthResponse health() {
//        return Map.of(
//                "status", "UP",
//                "service", "ai-chat-workspace"
//        );

        return new HealthResponse(
                "ai-chat-workspace",
                "UP"
        );
    }
}
