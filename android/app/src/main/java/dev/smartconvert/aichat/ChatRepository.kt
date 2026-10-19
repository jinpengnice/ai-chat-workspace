package dev.smartconvert.aichat

import dev.smartconvert.aichat.data.remote.RetrofitClient
import dev.smartconvert.aichat.model.ChatResponse
import dev.smartconvert.aichat.model.CreateChatRequest
import dev.smartconvert.aichat.model.HealthResponse
import dev.smartconvert.aichat.model.MessageResponse
import dev.smartconvert.aichat.model.SendMessageRequest

class ChatRepository {
    private val chatApi = RetrofitClient.chatApi

    suspend fun getHealth(): HealthResponse {
        return chatApi.getHealth()
    }

    suspend fun getChats(): List<ChatResponse> {
        return chatApi.getChats();
    }

    suspend fun createChat(title: String): ChatResponse {
        return chatApi.createChat(CreateChatRequest(title))
    }

    suspend fun sendMessage(chatId: String, content: String): MessageResponse {
        return chatApi.sendMessage(
            chatId = chatId,
            request = SendMessageRequest(content)
        )
    }

    suspend fun getMessages(chatId: String): List<MessageResponse> {
        return chatApi.getMessages(chatId)
    }
}