package dev.smartconvert.backend.chat.repository;

import dev.smartconvert.backend.chat.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
}
