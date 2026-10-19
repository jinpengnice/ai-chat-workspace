package dev.smartconvert.aichat.model

data class MessageResponse(
    val id: String,
    val chatId: String,
    val role: String,
    val content: String,
    val createdAt: String
)