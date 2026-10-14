package dev.smartconvert.backend.chat;

import java.time.Instant;
import java.util.UUID;

public record ChatMessage(
        UUID id,
        UUID chatId,
        ChatRole role,
        String content,
        Instant createdAt
) {
}
