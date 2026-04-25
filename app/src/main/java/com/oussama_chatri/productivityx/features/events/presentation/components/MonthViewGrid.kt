package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

@Composable
fun MonthViewGrid(
    monthOffset: Int,
    selectedDay: LocalDate,
    events: List<Event>,
    onDaySelected: (LocalDate) -> Unit,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val today      = LocalDate.now()
    val month      = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }
    val firstDay   = remember(month) { month.atDay(1) }
    val startPad   = remember(firstDay) {
        ((firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value) + 7) % 7
    }
    val totalCells = remember(month, startPad) { startPad + month.lengthOfMonth() }
    val rows       = remember(totalCells) { (totalCells + 6) / 7 }
    val zone       = ZoneId.systemDefault()

    Column(modifier = modifier) {
        // Day-of-week header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { label ->
                Text(
                    text      = label,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = Color(0xFF888899),
                    modifier  = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Calendar grid
        repeat(rows) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { colIndex ->
                    val cellIndex = rowIndex * 7 + colIndex
                    val dayOffset = cellIndex - startPad
                    val day       = if (dayOffset in 0 until month.lengthOfMonth())
                        firstDay.plusDays(dayOffset.toLong()) else null

                    val dayEvents = if (day != null) {
                        events.filter { event ->
                            val eventDay = event.startAt.atZone(zone).toLocalDate()
                            eventDay == day && !event.isDeleted
                        }
                    } else emptyList()

                    MonthDayCell(
                        day       = day,
                        today     = today,
                        selected  = day == selectedDay,
                        events    = dayEvents,
                        onClick   = { day?.let { onDaySelected(it) } },
                        modifier  = Modifier.weight(1f)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF252533), modifier = Modifier.padding(vertical = 8.dp))

        // Selected day events list
        val selectedDayEvents = events.filter { event ->
            event.startAt.atZone(zone).toLocalDate() == selectedDay && !event.isDeleted
        }.sortedBy { it.startAt }

        AnimatedVisibility(
            visible = selectedDayEvents.isNotEmpty(),
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(selectedDayEvents, key = { it.id }) { event ->
                    EventBlock(
                        event    = event,
                        onClick  = { onEventClick(event.id) },
                        showDate = false
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    day: LocalDate?,
    today: LocalDate,
    selected: Boolean,
    events: List<Event>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier             = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = day != null, onClick = onClick)
            .background(
                if (selected) Color(0xFF6366F1).copy(alpha = 0.15f) else Color.Transparent
            )
            .padding(4.dp),
        horizontalAlignment  = Alignment.CenterHorizontally
    ) {
        if (day != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            selected && day == today -> Color(0xFF6366F1)
                            day == today             -> Color(0xFF6366F1).copy(alpha = 0.2f)
                            else                     -> Color.Transparent
                        }
                    )
            ) {
                Text(
                    text  = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = when {
                        selected && day == today -> Color.White
                        day == today             -> Color(0xFF6366F1)
                        selected                 -> Color(0xFFEEEEF5)
                        else                     -> Color(0xFFCCCCD8)
                    }
                )
            }

            // Up to 3 event dots
            val dotsToShow = events.take(3)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier              = Modifier.padding(top = 3.dp)
            ) {
                dotsToShow.forEach { event ->
                    val dotColor = runCatching {
                        Color(android.graphics.Color.parseColor(event.color))
                    }.getOrDefault(Color(0xFF6366F1))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            if (events.size > 3) {
                Text(
                    text  = "+${events.size - 3}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF888899),
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
            }
        }
    }
}
