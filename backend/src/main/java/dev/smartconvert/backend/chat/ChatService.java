package dev.smartconvert.backend.chat;

import dev.smartconvert.backend.chat.dto.ChatResponse;
import dev.smartconvert.backend.chat.dto.CreateChatRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ChatService {

    private final ConcurrentMap<UUID, ChatSession> chats =
            new ConcurrentHashMap<>();

    public ChatResponse createChat(CreateChatRequest request) {
        Instant now = Instant.now();

        String title = request.title() == null
                || request.title().isBlank()
                ? "New Chat"
                : request.title().trim();

        ChatSession chat = new ChatSession(
                UUID.randomUUID(),
                title,
                now,
                now
        );
        chats.put(chat.id(), chat);

        return new ChatResponse(
                chat.id(),
                chat.title(),
                chat.createdAt(),
                chat.updatedAt()
        );
    }

    public ChatResponse getChat(UUID chatId) {
        ChatSession chat = chats.get(chatId);

        if (chat == null) {
            throw new ChatNotFoundException(chatId);
        }

        return new ChatResponse(
                chat.id(),
                chat.title(),
                chat.createdAt(),
                chat.updatedAt()
        );
    }
}
