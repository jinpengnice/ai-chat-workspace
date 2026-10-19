package dev.smartconvert.backend;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.api")
public record AiApiProperties(
        String baseUrl,
        String key,
        String model
) {
}
