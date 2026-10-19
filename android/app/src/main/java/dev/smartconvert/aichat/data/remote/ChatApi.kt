package dev.smartconvert.aichat.data.remote

import dev.smartconvert.aichat.model.ChatResponse
import dev.smartconvert.aichat.model.CreateChatRequest
import dev.smartconvert.aichat.model.HealthResponse
import dev.smartconvert.aichat.model.MessageResponse
import dev.smartconvert.aichat.model.SendMessageRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ChatApi {

    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @GET("api/chats")
    suspend fun getChats(): List<ChatResponse>

    @POST("api/chats")
    suspend fun createChat(
        @Body request: CreateChatRequest
    ): ChatResponse

    @GET("api/chats/{chatId}")
    suspend fun getChat(
        @Path("chatId") chatId: String
    ): ChatResponse

    @POST("api/chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body request: SendMessageRequest
    ): MessageResponse

    @Streaming
    @POST("api/chats/{chatId}/messages/stream")
    suspend fun streamMessage(
        @Path("chatId") chatId: String,
        @Body request: SendMessageRequest
    ): Response<ResponseBody>

    @GET("api/chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: String
    ): List<MessageResponse>
}