package dev.smartconvert.aichat

import dev.smartconvert.aichat.data.remote.RetrofitClient
import dev.smartconvert.aichat.model.ChatResponse
import dev.smartconvert.aichat.model.CreateChatRequest
import dev.smartconvert.aichat.model.HealthResponse
import dev.smartconvert.aichat.model.MessageResponse
import dev.smartconvert.aichat.model.SendMessageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okio.IOException

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

    suspend fun streamMessage(
        chatId: String,
        content: String,
        onDelta: (String) -> Unit
    ) {
        val response = chatApi.streamMessage(
            chatId = chatId,
            request = SendMessageRequest(content)
        )

        if (!response.isSuccessful) {
            response.errorBody()?.close()
            throw IOException("流式请求失败：HTTP ${response.code()}")
        }

        val body = response.body()
            ?: throw IOException("流式响应为空")

        withContext(Dispatchers.IO) {
            body.use { responseBody ->
                val source = responseBody.source()
                val eventData = mutableListOf<String>()

                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break

                    if (line.isEmpty()) {
                        if (eventData.isNotEmpty()) {
                            val data = eventData.joinToString("\n")
                            eventData.clear()

                            if (data == "[DONE]") break
                            onDelta(data)
                        }
                    } else if (line.startsWith("data:")) {
                        eventData += line
                            .removePrefix("data:")
                            .removePrefix(" ")
                    }
                    // 忽略 event: 等非 data 字段
                }
            }
        }

    }

    suspend fun getMessages(chatId: String): List<MessageResponse> {
        return chatApi.getMessages(chatId)
    }
}