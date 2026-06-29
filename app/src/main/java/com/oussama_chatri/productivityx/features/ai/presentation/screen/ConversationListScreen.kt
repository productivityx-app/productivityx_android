package com.oussama_chatri.productivityx.features.ai.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.DateTimeUtils
import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation
import com.oussama_chatri.productivityx.features.ai.presentation.event.ConversationListUiEvent
import com.oussama_chatri.productivityx.features.ai.presentation.state.AiPersonaType
import com.oussama_chatri.productivityx.features.ai.presentation.state.ConversationListUiState
import com.oussama_chatri.productivityx.features.ai.presentation.viewmodel.ConversationListViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onBack: () -> Unit,
    onOpenConversation: (UUID) -> Unit,
    onNewConversation: () -> Unit,
    viewModel: ConversationListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            if (state.isSearchVisible) {
                SearchTopBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onEvent(ConversationListUiEvent.SearchQueryChanged(it)) },
                    onClose = { viewModel.onEvent(ConversationListUiEvent.ToggleSearch) },
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "Conversations",
                            style = MaterialTheme.typography.titleLarge,
                            color = PxColors.OnBackground,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, "Back", tint = PxColors.OnSurfaceDim)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onEvent(ConversationListUiEvent.ToggleSearch) }) {
                            Icon(Icons.Outlined.Search, "Search", tint = PxColors.OnSurfaceDim)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background),
                )
            }
        },
        floatingActionButton = {
            FabWithOptions(
                isExpanded = state.fabExpanded,
                onToggle = { viewModel.onEvent(ConversationListUiEvent.ToggleFab) },
                onNewChat = onNewConversation,
                onQuickAction = { viewModel.onEvent(ConversationListUiEvent.CreateConversation) },
            )
        }
    ) { padding ->
        if (state.conversations.isEmpty() && !state.isLoading) {
            EmptyConversations(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (state.searchQuery.isNotBlank() && state.conversations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No results for \"${state.searchQuery}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PxColors.OnSurfaceDim,
                            )
                        }
                    }
                }

                if (state.pinnedConversations.isNotEmpty() && state.searchQuery.isBlank()) {
                    item {
                        SectionHeader(title = "Pinned")
                    }
                    items(state.pinnedConversations, key = { it.id }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            searchQuery = state.searchQuery,
                            onClick = { onOpenConversation(conversation.id) },
                            onDelete = { viewModel.onEvent(ConversationListUiEvent.DeleteConversation(conversation.id)) },
                            onArchive = { viewModel.onEvent(ConversationListUiEvent.ArchiveConversation(conversation.id)) },
                            onPin = { viewModel.onEvent(ConversationListUiEvent.UnpinConversation(conversation.id)) },
                        )
                    }
                    item { HorizontalDivider(color = PxColors.SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
                }

                if (state.todayConversations.isNotEmpty() && state.searchQuery.isBlank()) {
                    item {
                        SectionHeader(title = "Today")
                    }
                    items(state.todayConversations, key = { it.id }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            searchQuery = state.searchQuery,
                            onClick = { onOpenConversation(conversation.id) },
                            onDelete = { viewModel.onEvent(ConversationListUiEvent.DeleteConversation(conversation.id)) },
                            onArchive = { viewModel.onEvent(ConversationListUiEvent.ArchiveConversation(conversation.id)) },
                            onPin = { viewModel.onEvent(ConversationListUiEvent.PinConversation(conversation.id)) },
                        )
                    }
                }

                if (state.yesterdayConversations.isNotEmpty() && state.searchQuery.isBlank()) {
                    item {
                        SectionHeader(title = "Yesterday")
                    }
                    items(state.yesterdayConversations, key = { it.id }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            searchQuery = state.searchQuery,
                            onClick = { onOpenConversation(conversation.id) },
                            onDelete = { viewModel.onEvent(ConversationListUiEvent.DeleteConversation(conversation.id)) },
                            onArchive = { viewModel.onEvent(ConversationListUiEvent.ArchiveConversation(conversation.id)) },
                            onPin = { viewModel.onEvent(ConversationListUiEvent.PinConversation(conversation.id)) },
                        )
                    }
                }

                if (state.earlierConversations.isNotEmpty() && state.searchQuery.isBlank()) {
                    item {
                        SectionHeader(title = "Earlier")
                    }
                    items(state.earlierConversations, key = { it.id }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            searchQuery = state.searchQuery,
                            onClick = { onOpenConversation(conversation.id) },
                            onDelete = { viewModel.onEvent(ConversationListUiEvent.DeleteConversation(conversation.id)) },
                            onArchive = { viewModel.onEvent(ConversationListUiEvent.ArchiveConversation(conversation.id)) },
                            onPin = { viewModel.onEvent(ConversationListUiEvent.PinConversation(conversation.id)) },
                        )
                    }
                }

                if (state.searchQuery.isBlank() && state.pinnedConversations.isEmpty() && state.todayConversations.isEmpty() && state.yesterdayConversations.isEmpty() && state.earlierConversations.isEmpty()) {
                    items(state.conversations, key = { it.id }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            searchQuery = state.searchQuery,
                            onClick = { onOpenConversation(conversation.id) },
                            onDelete = { viewModel.onEvent(ConversationListUiEvent.DeleteConversation(conversation.id)) },
                            onArchive = { viewModel.onEvent(ConversationListUiEvent.ArchiveConversation(conversation.id)) },
                            onPin = { viewModel.onEvent(ConversationListUiEvent.PinConversation(conversation.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PxColors.Background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search conversations...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurfaceDim,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = PxColors.OnBackground),
                cursorBrush = SolidColor(PxColors.Primary),
                singleLine = true,
            )
        }
        IconButton(onClick = onClose) {
            Icon(Icons.Outlined.Close, "Close search", tint = PxColors.OnSurfaceDim)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = PxColors.OnSurfaceDim,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp, paddingStart = 16.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationItem(
    conversation: Conversation,
    searchQuery: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onPin: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                SwipeToDismissBoxValue.StartToEnd -> { onArchive(); true }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> PxColors.ErrorVariant
                    SwipeToDismissBoxValue.StartToEnd -> PxColors.SuccessVariant
                    else -> Color.Transparent
                },
                animationSpec = tween(200),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.Center
                },
            ) {
                Icon(
                    imageVector = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.EndToStart -> Icons.Outlined.Delete
                        SwipeToDismissBoxValue.StartToEnd -> Icons.Outlined.Archive
                        else -> Icons.Outlined.PushPin
                    },
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PxColors.Background)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PxColors.Primary, PxColors.Secondary),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (conversation.isPinned) {
                        Icon(
                            Icons.Outlined.PushPin,
                            contentDescription = null,
                            tint = PxColors.Primary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = highlightText(conversation.title ?: "New conversation", searchQuery),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PxColors.OnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!conversation.lastMessage.isNullOrBlank()) {
                    Text(
                        text = highlightText(conversation.lastMessage, searchQuery),
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurfaceDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateTimeUtils.relativeTime(LocalContext.current, conversation.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = PxColors.OnSurfaceDim,
                )
                if (conversation.unreadCount > 0) {
                    Spacer(Modifier.height(6.dp))
                    UnreadBadge(count = conversation.unreadCount)
                }
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
    )

    Box(
        modifier = Modifier
            .size(20.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(PxColors.Primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 9) "9+" else count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
        )
    }
}

@Composable
private fun highlightText(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.isBlank()) return buildAnnotatedString { append(text) }
    return buildAnnotatedString {
        var start = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        while (true) {
            val index = lowerText.indexOf(lowerQuery, start)
            if (index == -1) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, index))
            withStyle(SpanStyle(color = PxColors.Primary, background = PxColors.Primary.copy(alpha = 0.15f))) {
                append(text.substring(index, index + query.length))
            }
            start = index + query.length
        }
    }
}

@Composable
private fun FabWithOptions(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNewChat: () -> Unit,
    onQuickAction: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(animationSpec = tween(200)),
            exit = fadeOut() + shrinkVertically(animationSpec = tween(200)),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                FabOption(
                    icon = Icons.Outlined.Bolt,
                    label = "Quick Action",
                    onClick = {
                        onQuickAction()
                        onToggle()
                    },
                )
                Spacer(Modifier.height(8.dp))
                FabOption(
                    icon = Icons.Outlined.Add,
                    label = "New Chat",
                    onClick = {
                        onNewChat()
                        onToggle()
                    },
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = PxColors.Primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.Close else Icons.Outlined.Add,
                contentDescription = if (isExpanded) "Close" else "New conversation",
            )
        }
    }
}

@Composable
private fun FabOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PxColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = PxColors.OnSurface,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PxColors.Primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun EmptyConversations(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PxColors.Primary, PxColors.Secondary),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "No conversations yet",
                style = MaterialTheme.typography.titleLarge,
                color = PxColors.OnBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Start a conversation with your AI assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurfaceDim,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Plan my day", "Summarize notes", "Brainstorm ideas").forEach { text ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PxColors.Surface)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = PxColors.OnSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
