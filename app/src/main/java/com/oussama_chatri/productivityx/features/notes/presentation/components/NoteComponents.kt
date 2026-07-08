package com.oussama_chatri.productivityx.features.notes.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import com.oussama_chatri.productivityx.features.notes.presentation.state.NoteViewMode
import java.time.Instant
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: Note,
    viewMode: NoteViewMode,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    searchQuery: String = "",
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (viewMode) {
        NoteViewMode.GRID -> NoteGridCard(note, isSelected, isSelectionMode, searchQuery, onClick, onLongClick, onSwipeLeft, onSwipeRight, modifier)
        NoteViewMode.LIST -> NoteListCard(note, isSelected, isSelectionMode, searchQuery, onClick, onLongClick, onSwipeLeft, onSwipeRight, modifier)
        NoteViewMode.COMPACT -> NoteCompactCard(note, isSelected, isSelectionMode, searchQuery, onClick, onLongClick, onSwipeLeft, onSwipeRight, modifier)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteGridCard(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    searchQuery: String = "",
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            offsetX < -100 -> onSwipeLeft()
                            offsetX > 100 -> onSwipeRight()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount -> offsetX += dragAmount }
                )
            },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PxColors.Primary.copy(alpha = 0.12f) else PxColors.SurfaceVariant,
        tonalElevation = 0.dp
    ) {
        Box {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = "Pinned",
                            tint = PxColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(14.dp))
                    }
                    AnimatedVisibility(
                        visible = isSelectionMode,
                        enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)),
                        exit = scaleOut(animationSpec = tween(100))
                    ) {
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.8f,
                            label = "checkScale"
                        )
                        Icon(
                            imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = if (isSelected) "Selected" else "Not selected",
                            tint = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                            modifier = Modifier.size(20.dp).scale(scale)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))

                if (note.title.isNotBlank()) {
                    HighlightedText(
                        text = note.title,
                        query = if (isSelectionMode) "" else searchQuery,
                        style = MaterialTheme.typography.titleMedium,
                        color = PxColors.OnBackground,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (note.firstThreeLines.isNotBlank()) {
                    HighlightedText(
                        text = note.firstThreeLines,
                        query = if (isSelectionMode) "" else searchQuery,
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurfaceDim,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (note.imageUrls.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${note.imageUrls.size}", style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (note.hasVoiceMemo) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Mic, contentDescription = null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voice memo", style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (note.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        note.tags.forEach { tag -> NoteTagChip(tag = tag) }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = relativeTime(note.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = PxColors.OnSurfaceDim
                    )
                    SyncDot(syncStatus = note.syncStatus)
                }
            }

            if (offsetX < -50) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(PxColors.Error.copy(alpha = 0.9f))
                        .clickable { onSwipeLeft() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Archive, contentDescription = "Archive", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            if (offsetX > 50) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(PxColors.Primary.copy(alpha = 0.9f))
                        .clickable { onSwipeRight() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.PushPin, contentDescription = "Pin", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun NoteListCard(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    searchQuery: String = "",
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PxColors.Primary.copy(alpha = 0.12f) else PxColors.Surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)),
                exit = scaleOut(animationSpec = tween(100))
            ) {
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.8f,
                    label = "checkScale"
                )
                Icon(
                    imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                    modifier = Modifier.size(20.dp).scale(scale).padding(end = 8.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (note.isPinned) {
                        Icon(Icons.Outlined.PushPin, contentDescription = "Pinned", tint = PxColors.Primary, modifier = Modifier.size(14.dp))
                    }
                    HighlightedText(
                        text = note.title.ifBlank { "Untitled" },
                        query = if (isSelectionMode) "" else searchQuery,
                        style = MaterialTheme.typography.titleMedium,
                        color = PxColors.OnBackground,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))

                if (note.firstThreeLines.isNotBlank()) {
                    HighlightedText(
                        text = note.firstThreeLines,
                        query = if (isSelectionMode) "" else searchQuery,
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurfaceDim,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(relativeTime(note.updatedAt), style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
                    SyncDot(syncStatus = note.syncStatus)
                    if (note.imageUrls.isNotEmpty()) {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(12.dp))
                    }
                    if (note.hasVoiceMemo) {
                        Icon(Icons.Outlined.Mic, contentDescription = null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCompactCard(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    searchQuery: String = "",
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PxColors.Primary.copy(alpha = 0.12f) else Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)),
                exit = scaleOut(animationSpec = tween(100))
            ) {
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.8f,
                    label = "checkScale"
                )
                Icon(
                    imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                    modifier = Modifier.size(18.dp).scale(scale).padding(end = 8.dp)
                )
            }

            if (note.isPinned) {
                Icon(Icons.Outlined.PushPin, contentDescription = "Pinned", tint = PxColors.Primary, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }

            HighlightedText(
                text = note.title.ifBlank { "Untitled" },
                query = if (isSelectionMode) "" else searchQuery,
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnBackground,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = relativeTime(note.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurfaceDim
            )
        }
    }
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    maxLines: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier
) {
    val annotated = remember(text, query, color) {
        if (query.isBlank() || !text.contains(query, ignoreCase = true)) {
            buildAnnotatedString { append(text) }
        } else {
            buildAnnotatedString {
                var currentIndex = 0
                val lowerText = text.lowercase()
                val lowerQuery = query.lowercase()
                while (currentIndex < text.length) {
                    val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
                    if (matchIndex == -1) {
                        append(text.substring(currentIndex))
                        break
                    }
                    if (matchIndex > currentIndex) {
                        append(text.substring(currentIndex, matchIndex))
                    }
                    withStyle(SpanStyle(color = PxColors.Primary, background = PxColors.Primary.copy(alpha = 0.2f))) {
                        append(text.substring(matchIndex, matchIndex + query.length))
                    }
                    currentIndex = matchIndex + query.length
                }
            }
        }
    }

    Text(
        text = annotated,
        style = style,
        color = if (query.isBlank() || !text.contains(query, ignoreCase = true)) color else Color.Unspecified,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
fun NoteTagChip(
    tag: Tag,
    modifier: Modifier = Modifier
) {
    val tagColor = runCatching { Color(android.graphics.Color.parseColor(tag.color)) }
        .getOrDefault(PxColors.Primary)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(tagColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelSmall,
            color = tagColor
        )
    }
}

@Composable
fun FilterTagChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PxColors.Primary else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else PxColors.OnSurfaceDim,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipContent"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PxColors.Primary else PxColors.SurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
fun SyncDot(syncStatus: SyncStatus, modifier: Modifier = Modifier) {
    val color = when (syncStatus) {
        SyncStatus.SYNCED -> Color(0xFF22C55E)
        SyncStatus.PENDING -> Color(0xFFF59E0B)
        SyncStatus.SYNCING -> Color(0xFF3B82F6)
        SyncStatus.CONFLICT -> Color(0xFFEF4444)
    }
    Box(
        modifier = modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(color)
    )
}

fun relativeTime(instant: Instant): String {
    val now = Instant.now()
    val seconds = now.epochSecond - instant.epochSecond
    return when {
        seconds < 60 -> "Just now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        seconds < 604800 -> "${seconds / 86400}d ago"
        else -> {
            val formatter = java.time.format.DateTimeFormatter
                .ofPattern("MMM d")
                .withZone(java.time.ZoneId.systemDefault())
            runCatching { formatter.format(instant) }.getOrElse { "\u2014" }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun NoteCardPreview() {
    ProductivityXTheme {
        NoteGridCard(
            note = Note(
                id = "1",
                userId = "u1",
                title = "Meeting Notes",
                content = "# Agenda\n- Review Q2 targets\n- Discuss roadmap",
                plainTextContent = "Agenda Review Q2 targets Discuss roadmap",
                wordCount = 6,
                readingTimeSeconds = 3,
                isPinned = true,
                isDeleted = false,
                deletedAt = null,
                version = 1,
                syncStatus = SyncStatus.SYNCED,
                tags = setOf(Tag("t1", "u1", "Work", "#6366F1", Instant.now())),
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                folderId = null,
                imageUrls = emptyList(),
                hasVoiceMemo = false,
                hasFileAttachment = false,
                linkedNoteIds = emptyList()
            ),
            isSelected = false,
            isSelectionMode = false,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
