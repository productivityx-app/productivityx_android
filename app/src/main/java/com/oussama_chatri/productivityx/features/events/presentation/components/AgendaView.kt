package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d").withZone(ZoneId.systemDefault())
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())

@Composable
fun AgendaView(
    daysOffset: Int,
    today: LocalDate,
    events: List<Event>,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone = ZoneId.systemDefault()
    val endDate = remember(daysOffset) { today.plusDays(daysOffset.toLong()).plusMonths(3) }

    val groupedEvents = remember(events, today) {
        val activeEvents = events.filter { !it.isDeleted }
            .sortedBy { it.startAt }
        activeEvents.groupBy { event ->
            val startDate = event.startAt.atZone(zone).toLocalDate()
            if (startDate < today) today else startDate
        }.toSortedMap()
    }

    if (groupedEvents.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No upcoming events",
                style = MaterialTheme.typography.bodyLarge,
                color = PxColors.OnSurfaceDim
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groupedEvents.forEach { (date, dayEvents) ->
            item(key = "header_$date") {
                AgendaDateHeader(date = date, today = today)
            }
            items(dayEvents, key = { it.id }) { event ->
                AgendaEventRow(event = event, onClick = { onEventClick(event.id) })
            }
            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AgendaDateHeader(date: LocalDate, today: LocalDate) {
    val isToday = date == today
    val label = if (isToday) {
        "Today"
    } else if (date == today.plusDays(1)) {
        "Tomorrow"
    } else if (date == today.minusDays(1)) {
        "Yesterday"
    } else {
        runCatching { dateFormatter.format(date) }.getOrElse { "-" }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isToday) PxColors.Primary else PxColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isToday) Color.White else PxColors.OnSurface
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isToday) PxColors.Primary else PxColors.OnBackground
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = runCatching { DateTimeFormatter.ofPattern("MMM").format(date) }.getOrElse { "-" },
            style = MaterialTheme.typography.labelSmall,
            color = PxColors.OnSurfaceDim
        )
    }
}

@Composable
private fun AgendaEventRow(event: Event, onClick: () -> Unit) {
    val eventColor = runCatching { Color(android.graphics.Color.parseColor(event.color)) }
        .getOrDefault(PxColors.Primary)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(eventColor.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(eventColor)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PxColors.OnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            val timeStr = if (event.isAllDay) {
                "All day"
            } else {
                runCatching { "${timeFormatter.format(event.startAt)} - ${timeFormatter.format(event.endAt)}" }
                    .getOrElse { "-" }
            }
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurfaceDim
            )
            if (!event.location.isNullOrBlank()) {
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
            Text(
                text = "Repeats",
                style = MaterialTheme.typography.labelSmall,
                color = eventColor.copy(alpha = 0.7f)
            )
        }
    }
}
