package dev.smartconvert.aichat

import dev.smartconvert.aichat.model.ChatResponse

data class ChatUiState(
    val input: String = "",
    val messages: List<String> = emptyList(),

    val isCheckingHealth: Boolean = false,
    val healthMessage: String = "尚未检查后端连接",

    val chats: List<ChatResponse> = emptyList(),
    val isLoadingChats: Boolean = false,
    val chatListMessage: String = "尚未加载会话列表",

    val activeChatId: String? = null,
    val chatMessage: String = "尚未创建会话",
    val isCreatingChat: Boolean = false,
    val isSendingMessage: Boolean = false,

    val isLoadingMessages: Boolean = false,
    val messageLoadMessage: String = ""
)