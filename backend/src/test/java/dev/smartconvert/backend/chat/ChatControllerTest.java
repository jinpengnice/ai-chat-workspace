package dev.smartconvert.backend.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateChat() throws Exception {
        mockMvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Java 面试复习"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Java 面试复习"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void shouldUseDefaultTitleWhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title" : " "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Chat"));
    }

    void shouldGetExistingChat() throws Exception {
        String response = mockMvc.perform(post(("/api/chats"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "查询测试"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.fasterxml.jackson.databind.JsonNode json =
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(response);


        String chatId = json.get("id").asText();

        mockMvc.perform(get("/api/chats/{chatId}", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chatId))
                .andExpect(jsonPath("$.title").value("查询测试"));
    }

    @Test
    void shouldReturn404WhenChatDoesNotExist() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(get("/api/chats/{chatId}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldSendMessageToExistingChat() throws Exception {
        String response = mockMvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "消息测试"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = new ObjectMapper().readTree(response);

        String chatId = json.get("id").asText();

        mockMvc.perform(post("/api/chats/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "什么是 Java 线程池？"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatId").value(chatId))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.content").value("什么是 Java 线程池？"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldGetMessageForExistingChat() throws Exception {
        String response = mockMvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "历史消息测试"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = new ObjectMapper().readTree(response);

        String chatId = json.get("id").asText();

        mockMvc.perform(post("/api/chats/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "content": "第一条消息"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/chats/{chatId}/messages", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("第一条消息"));
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {
        String response = mockMvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "title": "空消息测试"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = new ObjectMapper().readTree(response);
        String chatId = json.get("id").asText();

        mockMvc.perform(post("/api/chats/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "content": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
