package com.oussama_chatri.productivityx.features.notes.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import java.time.Instant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteStaggeredCard(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick      = onClick,
        modifier     = modifier.fillMaxWidth(),
        shape        = RoundedCornerShape(12.dp),
        color        = Color(0xFF1A1A24),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (note.isPinned) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.PushPin,
                        contentDescription = "Pinned",
                        tint               = Color(0xFF6366F1),
                        modifier           = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            if (note.title.isNotBlank()) {
                Text(
                    text      = note.title,
                    style     = MaterialTheme.typography.titleMedium,
                    color     = Color(0xFFEEEEF5),
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (note.preview.isNotBlank()) {
                Text(
                    text      = note.preview,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = Color(0xFF888899),
                    maxLines  = 4,
                    overflow  = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (note.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    note.tags.forEach { tag ->
                        NoteTagChip(tag = tag)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text  = relativeTime(note.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF888899)
            )
        }
    }
}

@Composable
fun NoteTagChip(
    tag: Tag,
    modifier: Modifier = Modifier
) {
    val tagColor = runCatching { Color(android.graphics.Color.parseColor(tag.color)) }
        .getOrDefault(Color(0xFF6366F1))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(tagColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text  = tag.name,
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
        targetValue = if (isSelected) Color(0xFF6366F1) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF888899),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipContent"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF6366F1) else Color(0xFF252533),
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
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
fun SyncDot(syncStatus: SyncStatus, modifier: Modifier = Modifier) {
    val color = when (syncStatus) {
        SyncStatus.SYNCED   -> Color(0xFF22C55E)
        SyncStatus.PENDING  -> Color(0xFFF59E0B)
        SyncStatus.SYNCING  -> Color(0xFF3B82F6)
        SyncStatus.CONFLICT -> Color(0xFFEF4444)
    }
    Box(
        modifier = modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private fun relativeTime(instant: Instant): String {
    val now     = Instant.now()
    val seconds = now.epochSecond - instant.epochSecond
    return when {
        seconds < 60        -> "Just now"
        seconds < 3600      -> "${seconds / 60}m ago"
        seconds < 86400     -> "${seconds / 3600}h ago"
        seconds < 604800    -> "${seconds / 86400}d ago"
        else                -> {
            val formatter = java.time.format.DateTimeFormatter
                .ofPattern("MMM d")
                .withZone(java.time.ZoneId.systemDefault())
            formatter.format(instant)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun NoteCardPreview() {
    ProductivityXTheme {
        NoteStaggeredCard(
            note = Note(
                id                 = "1",
                userId             = "u1",
                title              = "Meeting Notes",
                content            = "# Agenda\n- Review Q2 targets\n- Discuss roadmap",
                plainTextContent   = "Agenda Review Q2 targets Discuss roadmap",
                wordCount          = 6,
                readingTimeSeconds = 3,
                isPinned           = true,
                isDeleted          = false,
                deletedAt          = null,
                version            = 1,
                syncStatus         = SyncStatus.SYNCED,
                tags               = setOf(Tag("t1", "u1", "Work", "#6366F1", Instant.now())),
                createdAt          = Instant.now(),
                updatedAt          = Instant.now()
            ),
            onClick  = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
