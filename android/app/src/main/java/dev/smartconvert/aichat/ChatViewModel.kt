package dev.smartconvert.aichat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.smartconvert.aichat.model.ChatResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        _uiState.update { state ->
            state.copy(input = value)
        }
    }

    fun checkBackendHealth() {
        if (_uiState.value.isCheckingHealth) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isCheckingHealth = true,
                    healthMessage = "正在连接后端…",
                )
            }

            try {
                val health = repository.getHealth()
                _uiState.update { state ->
                    state.copy(
                        isCheckingHealth = false,
                        healthMessage = "连接成功：${health.status} ${health.service}"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isCheckingHealth = false,
                        healthMessage = "连接失败： ${e.message ?: "请检查网络和后端服务"} "
                    )
                }
            }
        }
    }

    fun loadChats() {
        if (_uiState.value.isLoadingChats) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingChats = true,
                    chatListMessage = "正在加载会话…"
                )
            }

            try {
                val chats = repository.getChats()
                _uiState.update {
                    it.copy(
                        chats = chats,
                        isLoadingChats = false,
                        chatListMessage = "共 ${chats.size} 个会话"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingChats = false,
                        chatListMessage = "加载失败：${e.message ?: "请检查后端服务"}"
                    )
                }
            }
        }
    }

    fun createChat() {
        if (_uiState.value.isCreatingChat) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isCreatingChat = true,
                    chatMessage = "正在创建会话…"
                )
            }

            try {
                val chat = repository.createChat("新会话")
                _uiState.update { state ->
                    state.copy(
                        activeChatId = chat.id,
                        messages = emptyList(),
                        isCreatingChat = false,
                        chatMessage = "会话已创建: ${chat.id}"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isCreatingChat = false,
                        chatMessage = "创建失败: ${e.message ?: "请检查后端服务"}"
                    )
                }
            }
        }
    }

    fun selectChat(chat: ChatResponse) {
        if (_uiState.value.isLoadingMessages) return

        _uiState.update {
            it.copy(
                activeChatId = chat.id,
                messages = emptyList(),
                chatMessage = "当前会话: ${chat.title}",
                messageLoadMessage = ""
            )
        }

        loadMessageForChat(chat.id)
    }

    fun loadMessage() {
        val chatId = _uiState.value.activeChatId
        if (chatId == null) {
            _uiState.update {
                it.copy(messageLoadMessage = "请先选择和创建会话")
            }
            return
        }

        loadMessageForChat(chatId)
    }

    fun loadMessageForChat(chatId: String) {
        if (_uiState.value.isLoadingMessages) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMessages = true,
                    messageLoadMessage = "正在加载历史消息…"
                )
            }

            try {
                val messages = repository.getMessages(chatId)
                _uiState.update { current ->
                    if (current.activeChatId != chatId) {
                        current.copy(isLoadingMessages = false)
                    } else {
                        current.copy(
                            isLoadingMessages = false,
                            messageLoadMessage = "已加载 ${messages.size} 条消息",
                            messages = messages.map { message ->
                                UiChatMessage(
                                    id = message.id,
                                    role = when (message.role.uppercase()) {
                                        "USER" -> ChatMessageRole.USER
                                        else -> ChatMessageRole.ASSISTANT
                                    },
                                    content = message.content
                                )
                            }
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMessages = false,
                        messageLoadMessage = "加载失败：${e.message ?: "请检查后端服务"}"
                    )
                }
            }
        }
    }


    fun sendMessage() {
        val state = _uiState.value
        val chatId = state.activeChatId
        val content = state.input.trim()

        if (content.isBlank() || state.isSendingMessage) return

        if (chatId == null) {
            _uiState.update {
                it.copy(chatMessage = "请先创建会话，再发送消息")
            }
            return
        }

        viewModelScope.launch {
            val assistantMessageId = UUID.randomUUID().toString()

            val userMessage = UiChatMessage(
                id = UUID.randomUUID().toString(),
                role = ChatMessageRole.USER,
                content = content
            )

            val assistantMessage = UiChatMessage(
                id = assistantMessageId,
                role = ChatMessageRole.ASSISTANT,
                content = "",
                isStreaming = true
            )

            _uiState.update { current ->
                current.copy(
                    messages = current.messages + listOf(userMessage, assistantMessage),
                    isSendingMessage = true
                )
            }

            try {
                repository.streamMessage(chatId, content) { delta ->
                    _uiState.update { current ->
                        val lastMessage = current.messages.lastOrNull()

                        if (
                            current.activeChatId != chatId ||
                            lastMessage?.id != assistantMessageId ||
                            lastMessage.role != ChatMessageRole.ASSISTANT
                        ) {
                            current
                        } else {
                            current.copy(
                                messages = current.messages.dropLast(1) + lastMessage.copy(
                                    content = lastMessage.content + delta
                                )
                            )
                        }
                    }
                }

                _uiState.update { current ->
                    current.copy(
                        messages = current.messages.map { message ->
                            if (message.id == assistantMessageId) {
                                message.copy(isStreaming = false)
                            } else {
                                message
                            }
                        },
                        input = if (current.input.trim() == content) "" else current.input,
                        isSendingMessage = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { current ->
                    current.copy(
                        messages = current.messages.map { message ->
                            if (message.id == assistantMessageId) {
                                message.copy(isStreaming = false)
                            } else {
                                message
                            }
                        },
                        isSendingMessage = false,
                        chatMessage = "发送失败：${e.message ?: "请检查后端服务"}"
                    )
                }
            }
        }
    }

    fun loadMessages() {
        val state = _uiState.value
        val chatId = state.activeChatId ?: run {
            _uiState.update {
                it.copy(messageLoadMessage = "请先创建会话")
            }
            return
        }

        if (state.isLoadingMessages) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMessages = true,
                    messageLoadMessage = "正在加载历史消息…"
                )
            }

            try {
                val messages = repository.getMessages(chatId)

                _uiState.update {
                    it.copy(
                        isLoadingMessages = false,
                        messageLoadMessage = "已从服务端加载 ${messages.size} 条消息",
                        messages = messages.map { message ->
                            UiChatMessage(
                                id = message.id,
                                role = when (message.role.uppercase()) {
                                    "USER" -> ChatMessageRole.USER
                                    else -> ChatMessageRole.ASSISTANT
                                },
                                content = message.content
                            )
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMessages = false,
                        messageLoadMessage = "加载失败：${e.message ?: "请检查后端服务"}"
                    )
                }
            }
        }
    }

}