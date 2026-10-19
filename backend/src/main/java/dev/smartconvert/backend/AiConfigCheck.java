package dev.smartconvert.backend;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfigCheck {

    @Bean
    ApplicationRunner checkAiConfig(AiApiProperties properties) {
        return args -> {
            if (properties.key() == null || properties.key().isBlank()) {
                throw new IllegalStateException("AI_API_KEY is not configured");
            }
            System.out.println("AI config loaded. Model: " + properties.model());
        };
    }
}
