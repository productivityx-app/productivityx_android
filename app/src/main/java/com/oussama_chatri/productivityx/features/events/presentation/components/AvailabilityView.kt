package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

data class TimeSlot(
    val startHour: Int,
    val endHour: Int,
    val isFree: Boolean,
)

@Composable
fun AvailabilityView(
    events: List<Event>,
    selectedDay: LocalDate,
    modifier: Modifier = Modifier,
) {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()
    val weekStart = remember(selectedDay) {
        selectedDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val days = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }

    val freeSlots = remember(events, weekStart) {
        days.associate { day ->
            val dayEvents = events.filter { e ->
                !e.isDeleted && e.startAt.atZone(zone).toLocalDate() == day && !e.isAllDay
            }.sortedBy { it.startAt }

            val busyHours = dayEvents.map { e ->
                val startH = e.startAt.atZone(zone).hour.toFloat() + e.startAt.atZone(zone).minute / 60f
                val endH = e.endAt.atZone(zone).hour.toFloat() + e.endAt.atZone(zone).minute / 60f
                startH..endH
            }

            val slots = mutableListOf<TimeSlot>()
            var cursor = 8f
            while (cursor < 18f) {
                val nextBusy = busyHours.firstOrNull { cursor in it }
                if (nextBusy != null) {
                    val freeEnd = nextBusy.start
                    if (freeEnd - cursor >= 1f) {
                        slots.add(TimeSlot(cursor.toInt(), freeEnd.toInt(), isFree = true))
                    }
                    cursor = nextBusy.endInclusive
                } else {
                    slots.add(TimeSlot(cursor.toInt(), (cursor + 1f).toInt().coerceAtMost(18), isFree = true))
                    cursor += 1f
                }
            }
            day to slots
        }
    }

    val suggestedSlots = freeSlots.flatMap { (day, slots) ->
        slots.filter { it.isFree && (it.endHour - it.startHour) >= 1 }
            .map { day to it }
    }.take(5).toList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.SurfaceVariant)
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(R.string.calendar_free_busy_suggested),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = PxColors.OnBackground
        )
        Spacer(Modifier.height(8.dp))

        if (suggestedSlots.isEmpty()) {
            Text(
                text = stringResource(R.string.calendar_no_available_slots),
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim
            )
        } else {
            suggestedSlots.forEach { (day, slot) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(16.dp)
                    )
                    val dayAbbr = java.time.format.DateTimeFormatter.ofPattern("EEE").format(day)
                    Text(
                        text = "$dayAbbr ${slot.startHour}:00 - ${slot.endHour}:00",
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurface
                    )
                    Text(
                        text = stringResource(R.string.calendar_free),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF22C55E)
                    )
                }
            }
        }
    }
}
