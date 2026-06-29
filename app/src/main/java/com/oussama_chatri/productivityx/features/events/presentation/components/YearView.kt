package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private val monthFormatter = DateTimeFormatter.ofPattern("MMM")

@Composable
fun YearView(
    selectedYear: Int,
    events: List<Event>,
    onYearChanged: (Int) -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone = ZoneId.systemDefault()

    val months = remember(selectedYear) {
        (1..12).map { YearMonth.of(selectedYear, it) }
    }

    val eventCountByDate = remember(events, selectedYear) {
        events.filter { !it.isDeleted }
            .groupBy { it.startAt.atZone(zone).toLocalDate() }
            .mapValues { it.value.size }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { onYearChanged(selectedYear - 1) }) {
                Icon(Icons.Outlined.ChevronLeft, "Previous year", tint = Color(0xFFCCCCD8))
            }
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFEEEEF5),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { onYearChanged(selectedYear + 1) }) {
                Icon(Icons.Outlined.ChevronRight, "Next year", tint = Color(0xFFCCCCD8))
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(months, key = { it.monthValue }) { month ->
                MonthHeatMap(
                    yearMonth = month,
                    eventCountByDate = eventCountByDate,
                    onDaySelected = onDaySelected,
                )
            }
        }
    }
}

@Composable
private fun MonthHeatMap(
    yearMonth: YearMonth,
    eventCountByDate: Map<LocalDate, Int>,
    onDaySelected: (LocalDate) -> Unit,
) {
    val firstDay = yearMonth.atDay(1)
    val startPad = ((firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value) + 7) % 7
    val totalCells = startPad + yearMonth.lengthOfMonth()
    val rows = (totalCells + 6) / 7
    val maxEvents = eventCountByDate.values.maxOrNull() ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A24))
            .padding(6.dp)
    ) {
        Text(
            text = runCatching { monthFormatter.format(firstDay) }.getOrElse { "-" },
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFEEEEF5),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        repeat(rows) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { colIndex ->
                    val cellIndex = rowIndex * 7 + colIndex
                    val dayOffset = cellIndex - startPad
                    val day = if (dayOffset in 0 until yearMonth.lengthOfMonth())
                        firstDay.plusDays(dayOffset.toLong()) else null

                    val eventCount = day?.let { eventCountByDate[it] } ?: 0
                    val intensity = if (maxEvents > 0) eventCount.toFloat() / maxEvents else 0f

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(heatMapColor(intensity))
                            .clickable(enabled = day != null) {
                                day?.let { onDaySelected(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = Color(0xFFCCCCD8).copy(alpha = if (intensity > 0.3f) 0.9f else 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun heatMapColor(intensity: Float): Color {
    return when {
        intensity <= 0f -> Color(0xFF1A1A24)
        intensity <= 0.25f -> Color(0xFF6366F1).copy(alpha = 0.2f)
        intensity <= 0.5f -> Color(0xFF6366F1).copy(alpha = 0.4f)
        intensity <= 0.75f -> Color(0xFF6366F1).copy(alpha = 0.65f)
        else -> Color(0xFF6366F1).copy(alpha = 0.9f)
    }
}
