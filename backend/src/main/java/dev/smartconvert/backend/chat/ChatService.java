package dev.smartconvert.backend.chat;

import dev.smartconvert.backend.chat.dto.ChatResponse;
import dev.smartconvert.backend.chat.dto.CreateChatRequest;
import dev.smartconvert.backend.chat.dto.MessageResponse;
import dev.smartconvert.backend.chat.dto.SendMessageRequest;
import dev.smartconvert.backend.chat.repository.ChatMessageRepository;
import dev.smartconvert.backend.chat.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository
    ) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }


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
        ChatSession saveChat = chatSessionRepository.save(chat);

        return new ChatResponse(
                saveChat.getId(),
                saveChat.getTitle(),
                saveChat.getCreatedAt(),
                saveChat.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ChatResponse getChat(UUID chatId) {
        ChatSession chat = chatSessionRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

        return new ChatResponse(
                chat.getId(),
                chat.getTitle(),
                chat.getCreatedAt(),
                chat.getUpdatedAt()
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
        ChatMessage saveMessage = chatMessageRepository.save(message);

        return new MessageResponse(
                saveMessage.getId(),
                saveMessage.getChatId(),
                saveMessage.getRole(),
                saveMessage.getContent(),
                saveMessage.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(UUID chatId) {
        // 会话不存在返回 404
        getChat(chatId);


        return chatMessageRepository
                .findByChatIdOrderByCreatedAtAsc(chatId)
                .stream()
                .map(messages -> new MessageResponse(
                        messages.getId(),
                        messages.getChatId(),
                        messages.getRole(),
                        messages.getContent(),
                        messages.getCreatedAt()
                ))
                .toList();
    }
}
