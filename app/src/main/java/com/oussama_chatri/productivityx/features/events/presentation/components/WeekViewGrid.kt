package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

private val hourHeight = 60.dp
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
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val today     = LocalDate.now()
    val weekStart = remember(weekOffset) {
        today.plusWeeks(weekOffset.toLong())
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val days = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }

    Column(modifier = modifier.fillMaxSize()) {
        WeekDayHeader(
            days        = days,
            today       = today,
            selectedDay = selectedDay,
            onDaySelected = onDaySelected
        )
        HorizontalDivider(color = Color(0xFF252533))

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Time gutter
            Column(modifier = Modifier.width(gutterWidth)) {
                Spacer(Modifier.height(8.dp))
                repeat(24) { hour ->
                    Box(
                        modifier          = Modifier.height(hourHeight),
                        contentAlignment  = Alignment.TopEnd
                    ) {
                        if (hour > 0) {
                            Text(
                                text     = timeFormatter.format(
                                    java.time.Instant.ofEpochSecond((hour * 3600).toLong())
                                ),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Color(0xFF888899),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }

            // Day columns
            days.forEach { day ->
                val zone       = ZoneId.systemDefault()
                val dayEvents  = events.filter { event ->
                    val eventDay = event.startAt.atZone(zone).toLocalDate()
                    eventDay == day && !event.isDeleted
                }

                DayColumn(
                    day         = day,
                    events      = dayEvents,
                    onSlotClick = { hour -> onSlotClick(day, hour) },
                    onEventClick = onEventClick,
                    modifier    = Modifier.weight(1f)
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
            val isToday    = day == today
            val isSelected = day == selectedDay

            Column(
                modifier              = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onDaySelected(day) },
                horizontalAlignment   = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.Center
            ) {
                Text(
                    text  = dayAbbrFormatter.format(day).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF888899)
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected && isToday -> Color(0xFF6366F1)
                                isSelected            -> Color(0xFF252533)
                                else                  -> Color.Transparent
                            }
                        )
                ) {
                    Text(
                        text       = dayNumFormatter.format(day),
                        style      = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        ),
                        color      = when {
                            isSelected -> Color.White
                            isToday    -> Color(0xFF6366F1)
                            else       -> Color(0xFFCCCCD8)
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
    onSlotClick: (Int) -> Unit,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()

    Box(modifier = modifier.fillMaxHeight()) {
        Column {
            repeat(24) { hour ->
                Box(
                    modifier = Modifier
                        .height(hourHeight)
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

        events.forEach { event ->
            val startZdt  = event.startAt.atZone(zone)
            val endZdt    = event.endAt.atZone(zone)
            val startHour = startZdt.hour + startZdt.minute / 60f
            val endHour   = endZdt.hour + endZdt.minute / 60f
            val duration  = (endHour - startHour).coerceAtLeast(0.5f)

            val topOffset   = (startHour * hourHeight.value).dp
            val blockHeight = (duration * hourHeight.value).coerceAtLeast(30f).dp

            val eventColor = runCatching {
                Color(android.graphics.Color.parseColor(event.color))
            }.getOrDefault(Color(0xFF6366F1))

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
                Text(
                    text     = event.title,
                    style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color    = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
