package dev.smartconvert.backend.chat;

import dev.smartconvert.backend.chat.dto.ChatResponse;
import dev.smartconvert.backend.chat.dto.CreateChatRequest;
import dev.smartconvert.backend.chat.dto.MessageResponse;
import dev.smartconvert.backend.chat.dto.SendMessageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatResponse> getChats() {
        return chatService.getChats();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatResponse createChat(
            @RequestBody CreateChatRequest request
    ) {
        return chatService.createChat(request);
    }

    @GetMapping("/{chatId}")
    public ChatResponse getChat(@PathVariable UUID chatId) {
        return chatService.getChat(chatId);
    }

    @PostMapping("/{chatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(
            @PathVariable UUID chatId,
            @RequestBody SendMessageRequest request
    ) {
        return chatService.sendMessage(chatId, request);
    }

    @PostMapping(
            value = "/{chatId}/messages/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public StreamingResponseBody streamMessage(
            @PathVariable UUID chatId,
            @RequestBody SendMessageRequest request
    ) {
        return outputStream ->
                chatService.streamMessage(chatId, request, outputStream);
    }

    @GetMapping("/{chatId}/messages")
    public List<MessageResponse> getMessages(
            @PathVariable UUID chatId
    ) {
        return chatService.getMessages(chatId);
    }
}
