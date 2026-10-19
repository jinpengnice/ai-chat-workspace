package dev.smartconvert.backend.chat;

import dev.smartconvert.backend.DeepSeekClient;
import dev.smartconvert.backend.chat.dto.ChatResponse;
import dev.smartconvert.backend.chat.dto.CreateChatRequest;
import dev.smartconvert.backend.chat.dto.MessageResponse;
import dev.smartconvert.backend.chat.dto.SendMessageRequest;
import dev.smartconvert.backend.chat.repository.ChatMessageRepository;
import dev.smartconvert.backend.chat.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DeepSeekClient deepSeekClient;

    public ChatService(
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository,
            DeepSeekClient deepSeekClient
    ) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.deepSeekClient = deepSeekClient;
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

        String userContent = request.content().trim();

        List<DeepSeekClient.ChatMessage> promptMessages = new ArrayList<>(
                chatMessageRepository.findByChatIdOrderByCreatedAtAsc(chatId)
                        .stream()
                        .map(message -> new DeepSeekClient.ChatMessage(
                                message.getRole().name().toLowerCase(Locale.ROOT),
                                message.getContent()
                        ))
                        .toList()
        );
        promptMessages.add(new DeepSeekClient.ChatMessage("user", userContent));

        String assistantContent = deepSeekClient.chat(promptMessages);

        Instant userTime = Instant.now();
        ChatMessage userMessage = new ChatMessage(
                UUID.randomUUID(),
                chatId,
                ChatRole.USER,
                userContent,
                userTime
        );
        chatMessageRepository.save(userMessage);

        Instant assistantTime = Instant.now();
        ChatMessage assistantMessage = new ChatMessage(
                UUID.randomUUID(),
                chatId,
                ChatRole.ASSISTANT,
                assistantContent,
                assistantTime
        );
        ChatMessage saveAssistantMessage = chatMessageRepository.save(assistantMessage);

        return new MessageResponse(
                saveAssistantMessage.getId(),
                saveAssistantMessage.getChatId(),
                saveAssistantMessage.getRole(),
                saveAssistantMessage.getContent(),
                saveAssistantMessage.getCreatedAt()
        );
    }

    public void streamMessage(
            UUID chatId,
            SendMessageRequest request,
            OutputStream outputStream
    ) throws IOException {
        getChat(chatId);

        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("Message content must not be blank");
        }

        String userContent = request.content().trim();

        ArrayList<DeepSeekClient.ChatMessage> promptMessage = new ArrayList<>(
                chatMessageRepository.findByChatIdOrderByCreatedAtAsc(chatId)
                        .stream()
                        .map(message -> new DeepSeekClient.ChatMessage(
                                message.getRole().name().toLowerCase(Locale.ROOT),
                                message.getContent()
                        ))
                        .toList()
        );
        promptMessage.add(new DeepSeekClient.ChatMessage("user", userContent));

        StringBuilder fullAnswer = new StringBuilder();

        try {
            deepSeekClient.streamChat(promptMessage, delta -> {
                fullAnswer.append(delta);
                try {
                    writeSseData(outputStream, delta);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }

        Instant now = Instant.now();
        chatMessageRepository.save(new ChatMessage(
                UUID.randomUUID(), chatId, ChatRole.USER, userContent, now
        ));

        ChatMessage assistantMessage = chatMessageRepository.save(new ChatMessage(
                UUID.randomUUID(),
                chatId,
                ChatRole.ASSISTANT,
                fullAnswer.toString(),
                Instant.now()
        ));

        outputStream.write(
                "event: done\ndata: [DONE]\n\n".getBytes(StandardCharsets.UTF_8)
        );
        outputStream.flush();
    }

    private void writeSseData(OutputStream outputStream, String text) throws IOException {
        String normalized = text
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        for (String line : normalized.split("\n", -1)) {
            outputStream.write(
                    ("data: " + line + "\n").getBytes(StandardCharsets.UTF_8)
            );
        }

        outputStream.write('\n');
        outputStream.flush();
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

    @Transactional(readOnly = true)
    public List<ChatResponse> getChats() {
        return chatSessionRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(chat -> new ChatResponse(
                        chat.getId(),
                        chat.getTitle(),
                        chat.getCreatedAt(),
                        chat.getUpdatedAt()
                ))
                .toList();
    }
}
