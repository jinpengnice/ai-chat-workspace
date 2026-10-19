package dev.smartconvert.aichat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.smartconvert.aichat.ui.theme.AiChatWorkspaceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiChatWorkspaceTheme {
                ChatScreen(
                    modifier = Modifier.fillMaxSize()
                )

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // 底部输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.input,
                    onValueChange = viewModel::onInputChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("输出消息") }
                )

                Button(
                    onClick = {
                        viewModel.sendMessage()
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    enabled = uiState.activeChatId != null &&
                            !uiState.isSendingMessage &&
                            uiState.input.isNotBlank()
                ) {
                    Text(
                        if (uiState.isSendingMessage) "发送中…" else "发送"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)

        ) {
            // 标题 & 检查后端连接
            Text(
                text = "AI Chat Workspace",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = uiState.healthMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 9.dp, bottom = 8.dp),
            )
            Button(
                onClick = viewModel::checkBackendHealth,
                enabled = !uiState.isCheckingHealth,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(if (uiState.isCheckingHealth) "检查中…" else "检查后端连接")
            }

            // 创建会话提示
            Text(
                text = uiState.chatMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )

            // 按钮操作区域
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = viewModel::createChat,
                    enabled = !uiState.isCreatingChat
                ) {
                    Text(
                        text = if (uiState.isCreatingChat) "创建中…" else "新建会话"
                    )
                }

                Button(
                    onClick = viewModel::loadChats,
                    enabled = !uiState.isLoadingChats
                ) {
                    Text(
                        if (uiState.isLoadingChats) "加载中…" else "加载会话"
                    )
                }

                Button(
                    onClick = viewModel::loadMessages,
                    enabled = uiState.activeChatId != null &&
                            !uiState.isLoadingMessages
                ) {
                    Text(
                        if (uiState.isLoadingMessages) "加载中…" else "刷新历史"
                    )
                }
            }

            // GET /api/messages 提示
            if (uiState.messageLoadMessage.isNotBlank()) {
                Text(
                    text = uiState.messageLoadMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 会话列表
            Text(
                text = uiState.chatListMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = uiState.chats,
                    key = { chat -> chat.id }
                ) { chat ->
                    OutlinedButton(
                        onClick = { viewModel.selectChat(chat) },
                        enabled = !uiState.isLoadingMessages
                    ) {
                        val selectMark = if (chat.id == uiState.activeChatId) " ✓" else ""
                        Text("${chat.title}$selectMark")
                    }
                }
            }


            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.messages) { message ->
                    Text(message)
                }
            }
        }
    }
}
