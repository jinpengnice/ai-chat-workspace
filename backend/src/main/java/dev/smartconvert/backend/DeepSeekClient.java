package dev.smartconvert.backend;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class DeepSeekClient {

    private final AiApiProperties properties;
    private final RestClient restClient;

    public DeepSeekClient(AiApiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create(properties.baseUrl());
    }

    public String chat(List<ChatMessage> messages) {
        ChatRequest request = new ChatRequest(
                properties.model(),
                messages.toArray(ChatMessage[]::new)
        );

        ChatResponse response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + properties.key())
                .body(request)
                .retrieve()
                .body(ChatResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().length == 0) {
            throw new IllegalStateException("AI provider returned an empty response");
        }
        return response.choices()[0].message().content();
    }


    public record ChatMessage(String role, String content) {

    }

    public record Choice(ChatMessage message) {

    }

    public record ChatRequest(String model, ChatMessage[] messages) {

    }

    public record ChatResponse(Choice[] choices) {

    }
}
