package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

val eventColorPalette = listOf(
    "#6366F1", "#8B5CF6", "#EC4899", "#EF4444",
    "#F59E0B", "#22C55E", "#3B82F6", "#F97316"
)

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d · h:mm a").withZone(ZoneId.systemDefault())

@Composable
fun EventBlock(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
    showLocation: Boolean = false,
) {
    val eventColor = runCatching { Color(android.graphics.Color.parseColor(event.color)) }
        .getOrDefault(PxColors.Primary)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(eventColor.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(eventColor)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PxColors.OnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val timeLabel = if (showDate) {
                runCatching { dateTimeFormatter.format(event.startAt) }.getOrElse { "-" }
            } else {
                if (event.isAllDay) "All day"
                else runCatching { "${timeFormatter.format(event.startAt)} - ${timeFormatter.format(event.endAt)}" }
                    .getOrElse { "-" }
            }
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurfaceDim
            )
            if (showLocation && !event.location.isNullOrBlank()) {
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = PxColors.OnSurfaceDim.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (event.isRecurring) {
            Icon(
                imageVector = Icons.Outlined.Repeat,
                contentDescription = "Recurring",
                tint = eventColor.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun EventColorPicker(
    selectedHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        eventColorPalette.forEach { hex ->
            val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
            val isSelected = hex == selectedHex
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(c)
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(hex) }
            )
        }
    }
}

@Composable
fun RecurrenceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (isSelected) PxColors.Primary else PxColors.SurfaceVariant
    val content = if (isSelected) Color.White else PxColors.OnSurfaceDim
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
    }
}
