package com.oussama_chatri.productivityx.features.ai.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.MessageRole
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.ai.presentation.components.ActionCard
import com.oussama_chatri.productivityx.features.ai.presentation.components.ChatBubble
import com.oussama_chatri.productivityx.features.ai.presentation.components.ContextPanel
import com.oussama_chatri.productivityx.features.ai.presentation.components.MessageInputBar
import com.oussama_chatri.productivityx.features.ai.presentation.components.MessageStatus
import com.oussama_chatri.productivityx.features.ai.presentation.components.StreamingBubble
import com.oussama_chatri.productivityx.features.ai.presentation.components.WelcomeState
import com.oussama_chatri.productivityx.features.ai.presentation.event.AiUiEvent
import com.oussama_chatri.productivityx.features.ai.presentation.viewmodel.AiViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(
    conversationId: UUID? = null,
    onNavigateToHistory: () -> Unit,
    viewModel: AiViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        conversationId?.let { viewModel.loadConversation(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    LaunchedEffect(state.messages.size, state.isStreaming) {
        val itemCount = state.messages.size + if (state.isStreaming) 1 else 0
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    Scaffold(
        containerColor = PxColors.Background,
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ai_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = PxColors.OnBackground,
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Outlined.FormatListBulleted, stringResource(R.string.ai_conversations_title), tint = PxColors.OnSurfaceDim)
                    }
                    IconButton(onClick = { viewModel.onEvent(AiUiEvent.NewConversation) }) {
                        Icon(Icons.Outlined.Refresh, stringResource(R.string.ai_new_conversation), tint = PxColors.OnSurfaceDim)
                    }
                    if (state.conversationId != null) {
                        IconButton(onClick = { /* confirm delete */ }) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.ai_delete_conversation_title), tint = PxColors.OnSurfaceDim)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background),
            )
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                MessageInputBar(
                    value = state.inputText,
                    onValueChange = { viewModel.onEvent(AiUiEvent.InputChanged(it)) },
                    onSend = { viewModel.onEvent(AiUiEvent.SendMessage) },
                    isStreaming = state.isStreaming,
                    replyToMessage = state.replyToMessage,
                    onCancelReply = { viewModel.onEvent(AiUiEvent.CancelReply) },
                    suggestions = state.suggestions,
                    onSuggestionClick = { text -> viewModel.onEvent(AiUiEvent.SendSuggestion(text)) },
                )
            }
        }
    ) { paddingValues ->
        if (state.messages.isEmpty() && !state.isStreaming) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                WelcomeState(
                    onSuggestionClick = { text ->
                        viewModel.onEvent(AiUiEvent.SendSuggestion(text))
                    },
                    personaType = state.personaType,
                    modifier = Modifier.weight(1f),
                )
                ContextPanel(
                    context = state.context,
                    isLoading = state.isContextLoading,
                    onRefresh = { viewModel.onEvent(AiUiEvent.RefreshContext) },
                    personaType = state.personaType,
                    onPersonaChange = { viewModel.onEvent(AiUiEvent.SelectPersona(it)) },
                    isExpanded = state.isContextExpanded,
                    onToggleExpanded = { viewModel.onEvent(AiUiEvent.ToggleContextPanel) },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                var lastRole: MessageRole? = null
                state.messages.forEach { message ->
                    val isGrouped = message.role == lastRole && message.role == MessageRole.ASSISTANT
                    lastRole = message.role

                    item(key = message.id) {
                        ChatBubble(
                            message = message,
                            isGrouped = isGrouped,
                            status = when {
                                message.isStreaming -> MessageStatus.SENDING
                                else -> MessageStatus.SENT
                            },
                            onCopy = { viewModel.onEvent(AiUiEvent.CopyMessage(message.id)) },
                            onRegenerate = { viewModel.onEvent(AiUiEvent.RegenerateResponse(message.id)) },
                            onReply = { viewModel.onEvent(AiUiEvent.ReplyToMessage(message)) },
                            onLongPress = { viewModel.onEvent(AiUiEvent.ToggleEmojiReaction) },
                        )

                        if (message.actionBlock != null && message.actionBlock == state.pendingAction) {
                            Spacer(Modifier.height(8.dp))
                            ActionCard(
                                action = message.actionBlock,
                                onConfirm = { viewModel.onEvent(AiUiEvent.ConfirmAction(it)) },
                                onDismiss = { viewModel.onEvent(AiUiEvent.DismissAction) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (state.isStreaming) {
                    item(key = "streaming") {
                        StreamingBubble(content = state.streamingContent)
                    }
                }

                item(key = "context_panel") {
                    Spacer(Modifier.height(8.dp))
                    ContextPanel(
                        context = state.context,
                        isLoading = state.isContextLoading,
                        onRefresh = { viewModel.onEvent(AiUiEvent.RefreshContext) },
                        personaType = state.personaType,
                        onPersonaChange = { viewModel.onEvent(AiUiEvent.SelectPersona(it)) },
                        isExpanded = state.isContextExpanded,
                        onToggleExpanded = { viewModel.onEvent(AiUiEvent.ToggleContextPanel) },
                    )
                }
            }
        }
    }
}
