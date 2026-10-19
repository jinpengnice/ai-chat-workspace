package dev.smartconvert.backend;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@Service
public class DeepSeekClient {

    private final AiApiProperties properties;
    private final RestClient restClient;
    private JsonMapper jsonMapper;

    public DeepSeekClient(AiApiProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.restClient = RestClient.create(properties.baseUrl());
    }

    public String chat(List<ChatMessage> messages) {
        ChatRequest request = new ChatRequest(
                properties.model(),
                messages.toArray(ChatMessage[]::new),
                false
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

    public void streamChat(
            List<ChatMessage> messages,
            Consumer<String> onDelta
    ) {
        ChatRequest request = new ChatRequest(
                properties.model(),
                messages.toArray(ChatMessage[]::new),
                true
        );

        try {
            InputStream body = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + properties.key())
                    .header("Accept", "text/event-stream")
                    .body(request)
                    .retrieve()
                    .body(InputStream.class);

            if (body == null) {
                throw new IllegalStateException("AI provider returned an empty stream");
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(body, StandardCharsets.UTF_8)
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }

                    String data = line.substring("data:".length()).trim();

                    if (data.isEmpty()) {
                        continue;
                    }
                    if ("[DONE]".equals(data)) {
                        break;
                    }

                    JsonNode chunk = jsonMapper.readTree(data);
                    JsonNode content = chunk
                            .path("choices")
                            .path(0)
                            .path("delta")
                            .path("content");

                    if (content.isTextual() && !content.asText().isEmpty()) {
                        onDelta.accept(content.asText());
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read AI response stream", e);
        }

    }


    public record ChatMessage(String role, String content) {

    }

    public record Choice(ChatMessage message) {

    }

    public record ChatRequest(
            String model,
            ChatMessage[] messages,
            boolean stream
    ) {

    }

    public record ChatResponse(Choice[] choices) {

    }
}
