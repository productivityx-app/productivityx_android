package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private val gutterWidth = 52.dp
private val dayHeaderHeight = 56.dp
private val timeFormatter = DateTimeFormatter.ofPattern("h a").withZone(ZoneId.systemDefault())
private val dayAbbrFormatter = DateTimeFormatter.ofPattern("EEE")
private val dayNumFormatter = DateTimeFormatter.ofPattern("d")

@Composable
fun WeekViewGrid(
    weekOffset: Int,
    selectedDay: LocalDate,
    events: List<Event>,
    onDaySelected: (LocalDate) -> Unit,
    onSlotClick: (LocalDate, Int) -> Unit,
    onSlotLongPress: (LocalDate, Int, Int) -> Unit,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val weekStart = remember(weekOffset) {
        today.plusWeeks(weekOffset.toLong())
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val days = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }
    val zone = ZoneId.systemDefault()
    var hourHeight by remember { mutableFloatStateOf(60f) }

    val allDayEvents = events.filter { it.isAllDay && !it.isDeleted }
    val timedEvents = events.filter { !it.isAllDay && !it.isDeleted }

    Column(modifier = modifier.fillMaxSize()) {
        WeekDayHeader(
            days = days,
            today = today,
            selectedDay = selectedDay,
            onDaySelected = onDaySelected
        )
        HorizontalDivider(color = Color(0xFF252533))

        if (allDayEvents.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A24))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "All day",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF888899),
                    modifier = Modifier.padding(bottom = 4.dp, start = gutterWidth)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.width(gutterWidth))
                    days.forEach { day ->
                        val dayAllDay = allDayEvents.filter { event ->
                            val eventDay = event.startAt.atZone(zone).toLocalDate()
                            eventDay <= day && event.endAt.atZone(zone).toLocalDate() >= day
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 1.dp)) {
                            dayAllDay.forEach { event ->
                                val eventColor = runCatching { Color(android.graphics.Color.parseColor(event.color)) }
                                    .getOrDefault(Color(0xFF6366F1))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(22.dp)
                                        .padding(vertical = 1.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(eventColor.copy(alpha = 0.85f))
                                        .clickable { onEventClick(event.id) }
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color(0xFF252533))
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        hourHeight = (hourHeight * zoom).coerceIn(40f, 120f)
                    }
                }
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.width(gutterWidth)) {
                Spacer(Modifier.height(8.dp))
                repeat(24) { hour ->
                    Box(
                        modifier = Modifier.height(hourHeight.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        if (hour > 0) {
                            Text(
                                text = runCatching {
                                    timeFormatter.format(
                                        java.time.Instant.ofEpochSecond((hour * 3600).toLong())
                                    )
                                }.getOrElse { "-" },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF888899),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }

            days.forEach { day ->
                val dayEvents = timedEvents.filter { event ->
                    val eventDay = event.startAt.atZone(zone).toLocalDate()
                    eventDay == day
                }

                DayColumn(
                    day = day,
                    events = dayEvents,
                    hourHeight = hourHeight,
                    zone = zone,
                    onSlotClick = { hour -> onSlotClick(day, hour) },
                    onEventClick = onEventClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WeekDayHeader(
    days: List<LocalDate>,
    today: LocalDate,
    selectedDay: LocalDate,
    onDaySelected: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dayHeaderHeight)
            .padding(start = gutterWidth)
    ) {
        days.forEach { day ->
            val isToday = day == today
            val isSelected = day == selectedDay

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onDaySelected(day) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = runCatching { dayAbbrFormatter.format(day).uppercase() }.getOrElse { "-" },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF888899)
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected && isToday -> Color(0xFF6366F1)
                                isSelected -> Color(0xFF252533)
                                else -> Color.Transparent
                            }
                        )
                ) {
                    Text(
                        text = runCatching { dayNumFormatter.format(day) }.getOrElse { "-" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = when {
                            isSelected -> Color.White
                            isToday -> Color(0xFF6366F1)
                            else -> Color(0xFFCCCCD8)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayColumn(
    day: LocalDate,
    events: List<Event>,
    hourHeight: Float,
    zone: ZoneId,
    onSlotClick: (Int) -> Unit,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxHeight()) {
        Column {
            repeat(24) { hour ->
                Box(
                    modifier = Modifier
                        .height(hourHeight.dp)
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = Color(0xFF252533).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(0.dp)
                        )
                        .clickable { onSlotClick(hour) }
                )
            }
        }

        val overlappingGroups = groupOverlappingEvents(events, zone)
        overlappingGroups.forEachIndexed { groupIndex, group ->
            val maxOverlap = group.maxOfOrNull { overlapCount(it, events, zone) } ?: 1
            val widthFraction = 1f / maxOverlap.coerceAtLeast(1)

            group.forEach { event ->
                val startZdt = event.startAt.atZone(zone)
                val endZdt = event.endAt.atZone(zone)
                val startHour = startZdt.hour + startZdt.minute / 60f
                val endHour = endZdt.hour + endZdt.minute / 60f
                val duration = (endHour - startHour).coerceAtLeast(0.5f)

                val topOffset = (startHour * hourHeight).dp
                val blockHeight = (duration * hourHeight).coerceAtLeast(30f).dp
                val hOffset = group.indexOf(event) * widthFraction

                val eventColor = runCatching { Color(android.graphics.Color.parseColor(event.color)) }
                    .getOrDefault(Color(0xFF6366F1))

                Box(
                    modifier = Modifier
                        .offset(y = topOffset)
                        .padding(start = (hOffset * 100).dp / 100f, end = 1.dp)
                        .width(((widthFraction * 100).toInt()).dp)
                        .height(blockHeight)
                        .clip(RoundedCornerShape(4.dp))
                        .background(eventColor.copy(alpha = 0.85f))
                        .clickable { onEventClick(event.id) }
                        .padding(3.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun groupOverlappingEvents(events: List<Event>, zone: ZoneId): List<List<Event>> {
    if (events.isEmpty()) return emptyList()
    val sorted = events.sortedBy { it.startAt.toEpochMilli() }
    val groups = mutableListOf<MutableList<Event>>()

    for (event in sorted) {
        val eventStart = event.startAt.atZone(zone)
        val eventEnd = event.endAt.atZone(zone)
        var placed = false

        for (group in groups) {
            val overlaps = group.any { g ->
                val gStart = g.startAt.atZone(zone)
                val gEnd = g.endAt.atZone(zone)
                eventStart.isBefore(gEnd) && eventEnd.isAfter(gStart)
            }
            if (overlaps) {
                group.add(event)
                placed = true
                break
            }
        }

        if (!placed) {
            groups.add(mutableListOf(event))
        }
    }

    return groups
}

private fun overlapCount(event: Event, allEvents: List<Event>, zone: ZoneId): Int {
    val eStart = event.startAt.atZone(zone)
    val eEnd = event.endAt.atZone(zone)
    return allEvents.count { other ->
        if (other.id == event.id) return@count false
        val oStart = other.startAt.atZone(zone)
        val oEnd = other.endAt.atZone(zone)
        eStart.isBefore(oEnd) && eEnd.isAfter(oStart)
    } + 1
}
