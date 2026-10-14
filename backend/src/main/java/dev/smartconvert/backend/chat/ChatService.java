package dev.smartconvert.backend.chat;

import dev.smartconvert.backend.chat.dto.ChatResponse;
import dev.smartconvert.backend.chat.dto.CreateChatRequest;
import dev.smartconvert.backend.chat.dto.MessageResponse;
import dev.smartconvert.backend.chat.dto.SendMessageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ChatService {
    private final ConcurrentMap<UUID, ChatSession> chats = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ChatMessage> messages = new ConcurrentHashMap<>();


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

    public MessageResponse sendMessage(UUID chatId, SendMessageRequest request) {
        // 先确认会话存在
        getChat(chatId);

        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("Message content must not be blank");
        }

        Instant now = Instant.now();

        ChatMessage message = new ChatMessage(
                UUID.randomUUID(),
                chatId,
                ChatRole.USER,
                request.content().trim(),
                now
        );
        messages.put(message.id(), message);

        return new MessageResponse(
                message.id(),
                message.chatId(),
                message.role(),
                message.content(),
                message.createdAt()
        );
    }

    public List<MessageResponse> getMessages(UUID chatId) {
        // 会话不存在返回 404
        getChat(chatId);

        return messages.values().stream()
                .filter(message -> message.chatId().equals(chatId))
                .sorted(Comparator.comparing(ChatMessage::createdAt))
                .map(messages -> new MessageResponse(
                        messages.id(),
                        messages.chatId(),
                        messages.role(),
                        messages.content(),
                        messages.createdAt()
                ))
                .toList();
    }
}
