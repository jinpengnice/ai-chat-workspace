package dev.smartconvert.backend.chat;

import java.time.Instant;
import java.util.UUID;

public record ChatSession(
        UUID id,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
}
