package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

private val hourHeight = 60.dp
private val gutterWidth = 52.dp
private val timeFormatter = DateTimeFormatter.ofPattern("h a").withZone(ZoneId.systemDefault())

@Composable
fun DayView(
    dayOffset: Int,
    selectedDay: LocalDate,
    events: List<Event>,
    onSlotClick: (LocalDate, Int) -> Unit,
    onSlotLongPress: (LocalDate, Int, Int) -> Unit,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val day = remember(dayOffset) { today.plusDays(dayOffset.toLong()) }
    val zone = ZoneId.systemDefault()

    val allDayEvents = events.filter {
        it.isAllDay && !it.isDeleted
    }
    val timedEvents = events.filter {
        !it.isAllDay && !it.isDeleted
    }

    Column(modifier = modifier.fillMaxSize()) {
        DayHeader(day = day, today = today, isSelected = day == selectedDay)

        if (allDayEvents.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PxColors.SurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "All day",
                    style = MaterialTheme.typography.labelSmall,
                    color = PxColors.OnSurfaceDim,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                allDayEvents.forEach { event ->
                    val eventColor = runCatching { Color(android.graphics.Color.parseColor(event.color)) }
                        .getOrDefault(PxColors.Primary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(eventColor.copy(alpha = 0.85f))
                            .clickable { onEventClick(event.id) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            HorizontalDivider(color = PxColors.Outline)
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.width(gutterWidth)) {
                Spacer(Modifier.height(8.dp))
                repeat(24) { hour ->
                    Box(
                        modifier = Modifier.height(hourHeight),
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
                                color = PxColors.OnSurfaceDim,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                Column {
                    repeat(24) { hour ->
                        Box(
                            modifier = Modifier
                                .height(hourHeight)
                                .fillMaxWidth()
                                .border(
                                    width = 0.5.dp,
                                    color = PxColors.SurfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .clickable { onSlotClick(day, hour) }
                        )
                    }
                }

                timedEvents.forEach { event ->
                    val startZdt = event.startAt.atZone(zone)
                    val endZdt = event.endAt.atZone(zone)
                    val startHour = startZdt.hour + startZdt.minute / 60f
                    val endHour = endZdt.hour + endZdt.minute / 60f
                    val duration = (endHour - startHour).coerceAtLeast(0.5f)

                    val topOffset = (startHour * hourHeight.value).dp
                    val blockHeight = (duration * hourHeight.value).coerceAtLeast(30f).dp

                    val eventColor = runCatching { Color(android.graphics.Color.parseColor(event.color)) }
                        .getOrDefault(PxColors.Primary)

                    Box(
                        modifier = Modifier
                            .offset(y = topOffset)
                            .padding(horizontal = 2.dp)
                            .height(blockHeight)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(eventColor.copy(alpha = 0.85f))
                            .clickable { onEventClick(event.id) }
                            .padding(4.dp)
                    ) {
                        Column {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val timeStr = runCatching {
                                val fmt = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
                                "${fmt.format(event.startAt)} - ${fmt.format(event.endAt)}"
                            }.getOrElse { "" }
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(
    day: LocalDate,
    today: LocalDate,
    isSelected: Boolean,
) {
    val isToday = day == today
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    val abbrFormatter = DateTimeFormatter.ofPattern("EEE")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) PxColors.Primary.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = runCatching { formatter.format(day) }.getOrElse { "-" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PxColors.OnBackground
            )
            Text(
                text = if (isToday) "Today" else runCatching { abbrFormatter.format(day) }.getOrElse { "-" },
                style = MaterialTheme.typography.labelSmall,
                color = if (isToday) PxColors.Primary else PxColors.OnSurfaceDim
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isToday) PxColors.Primary else Color.Transparent)
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isToday) Color.White else PxColors.OnSurface
            )
        }
    }
    HorizontalDivider(color = PxColors.Outline)
}


