package dev.smartconvert.backend;

public record HealthResponse(
        String service,
        String status
) {
}
