package dev.smartconvert.backend.chat.dto;

import dev.smartconvert.backend.chat.ChatRole;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID chatId,
        ChatRole role,
        String content,
        Instant createdAt
) {
}
