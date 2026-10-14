package dev.smartconvert.backend.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatResponse(
        UUID id,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
}
