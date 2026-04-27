package com.oussama_chatri.productivityx.features.ai.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.util.DateTimeUtils
import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation
import com.oussama_chatri.productivityx.features.ai.presentation.event.ConversationListUiEvent
import com.oussama_chatri.productivityx.features.ai.presentation.viewmodel.ConversationListViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onBack            : () -> Unit,
    onOpenConversation: (UUID) -> Unit,
    onNewConversation : () -> Unit,
    viewModel         : ConversationListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color(0xFF0F0F14),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Conversations",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFEEEEF5),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = Color(0xFF888899))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F14)),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = onNewConversation,
                containerColor   = Color(0xFF6366F1),
                contentColor     = Color.White,
            ) {
                Icon(Icons.Outlined.Add, "New conversation")
            }
        }
    ) { padding ->
        if (state.conversations.isEmpty() && !state.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "No conversations yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888899),
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(state.conversations, key = { it.id }) { conversation ->
                    ConversationItem(
                        conversation       = conversation,
                        onClick            = { onOpenConversation(conversation.id) },
                        onDelete           = {
                            viewModel.onEvent(
                                ConversationListUiEvent.DeleteConversation(conversation.id)
                            )
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationItem(
    conversation : Conversation,
    onClick      : () -> Unit,
    onDelete     : () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state          = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF7F1D1D))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Outlined.Delete, null, tint = Color.White)
            }
        },
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F14))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A24)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint               = Color(0xFF6366F1),
                    modifier           = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = conversation.title ?: "New conversation",
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = Color(0xFFEEEEF5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!conversation.lastMessage.isNullOrBlank()) {
                    Text(
                        text     = conversation.lastMessage,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = Color(0xFF888899),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text  = DateTimeUtils.formatDateShort(conversation.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF888899),
            )
        }
    }
}
