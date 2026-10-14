package dev.smartconvert.backend.chat;

import dev.smartconvert.backend.chat.dto.ChatResponse;
import dev.smartconvert.backend.chat.dto.CreateChatRequest;
import dev.smartconvert.backend.chat.dto.MessageResponse;
import dev.smartconvert.backend.chat.dto.SendMessageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
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

    @GetMapping("/{chatId}/messages")
    public List<MessageResponse> getMessages(
            @PathVariable UUID chatId
    ) {
        return chatService.getMessages(chatId);
    }
}
