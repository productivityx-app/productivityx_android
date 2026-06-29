package com.oussama_chatri.productivityx.features.events.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.CalendarView
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.events.presentation.components.AddEditEventSheet
import com.oussama_chatri.productivityx.features.events.presentation.components.AgendaView
import com.oussama_chatri.productivityx.features.events.presentation.components.AvailabilityView
import com.oussama_chatri.productivityx.features.events.presentation.components.DayView
import com.oussama_chatri.productivityx.features.events.presentation.components.MonthViewGrid
import com.oussama_chatri.productivityx.features.events.presentation.components.WeatherWidget
import com.oussama_chatri.productivityx.features.events.presentation.components.WeekViewGrid
import com.oussama_chatri.productivityx.features.events.presentation.components.YearView
import com.oussama_chatri.productivityx.features.events.presentation.event.CalendarUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.viewmodel.CalendarViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val weekHeaderFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val monthHeaderFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
private val yearHeaderFormatter = DateTimeFormatter.ofPattern("yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onNavigateToEventDetail: (String) -> Unit,
    showAddEvent: Boolean = false,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showAddEvent) {
        if (showAddEvent) {
            viewModel.onEvent(CalendarUiEvent.OpenAddEvent())
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CalendarModernTopBar(
                view = state.view,
                weekOffset = state.weekOffset,
                headerDate = state.selectedDay,
                isTodayPulsing = state.isTodayPulsing,
                onViewChanged = { viewModel.onEvent(CalendarUiEvent.ViewChanged(it)) },
                onPrevious = {
                    when (state.view) {
                        CalendarView.WEEK -> viewModel.onEvent(CalendarUiEvent.WeekChanged(-1))
                        CalendarView.MONTH -> viewModel.onEvent(CalendarUiEvent.MonthChanged(-1))
                        CalendarView.DAY -> viewModel.onEvent(CalendarUiEvent.DayChanged(-1))
                        CalendarView.AGENDA -> viewModel.onEvent(CalendarUiEvent.WeekChanged(1))
                        CalendarView.YEAR -> viewModel.onEvent(CalendarUiEvent.YearSelected(state.selectedYear - 1))
                    }
                },
                onNext = {
                    when (state.view) {
                        CalendarView.WEEK -> viewModel.onEvent(CalendarUiEvent.WeekChanged(1))
                        CalendarView.MONTH -> viewModel.onEvent(CalendarUiEvent.MonthChanged(1))
                        CalendarView.DAY -> viewModel.onEvent(CalendarUiEvent.DayChanged(1))
                        CalendarView.AGENDA -> viewModel.onEvent(CalendarUiEvent.WeekChanged(1))
                        CalendarView.YEAR -> viewModel.onEvent(CalendarUiEvent.YearSelected(state.selectedYear + 1))
                    }
                },
                onToday = { viewModel.onEvent(CalendarUiEvent.NavigateToToday) },
                onDatePicker = { viewModel.onEvent(CalendarUiEvent.ToggleDatePicker) },
                onVoiceInput = { viewModel.onEvent(CalendarUiEvent.OpenVoiceInput()) }
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.onEvent(CalendarUiEvent.Refresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                val swipeThreshold = 50f
                var totalDrag by remember { mutableStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(state.view) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (totalDrag < -swipeThreshold) {
                                        when (state.view) {
                                            CalendarView.WEEK -> viewModel.onEvent(CalendarUiEvent.WeekChanged(1))
                                            CalendarView.MONTH -> viewModel.onEvent(CalendarUiEvent.MonthChanged(1))
                                            CalendarView.DAY -> viewModel.onEvent(CalendarUiEvent.DayChanged(1))
                                            else -> {}
                                        }
                                    } else if (totalDrag > swipeThreshold) {
                                        when (state.view) {
                                            CalendarView.WEEK -> viewModel.onEvent(CalendarUiEvent.WeekChanged(-1))
                                            CalendarView.MONTH -> viewModel.onEvent(CalendarUiEvent.MonthChanged(-1))
                                            CalendarView.DAY -> viewModel.onEvent(CalendarUiEvent.DayChanged(-1))
                                            else -> {}
                                        }
                                    }
                                    totalDrag = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    totalDrag += dragAmount
                                }
                            )
                        }
                ) {
                    AnimatedContent(
                        targetState = state.view,
                        transitionSpec = {
                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                            fadeIn(tween(200)) + scaleIn(tween(200)) togetherWith
                                fadeOut(tween(200)) + scaleOut(tween(200))
                        },
                        label = "calendar_view_switch"
                    ) { view ->
                        when (view) {
                            CalendarView.WEEK -> WeekViewGrid(
                                weekOffset = state.weekOffset,
                                selectedDay = state.selectedDay,
                                events = state.events,
                                onDaySelected = { viewModel.onEvent(CalendarUiEvent.DaySelected(it)) },
                                onSlotClick = { day, hour ->
                                    viewModel.onEvent(CalendarUiEvent.OpenAddEventAtSlot(day, hour))
                                },
                                onSlotLongPress = { day, startHour, endHour ->
                                    viewModel.onEvent(CalendarUiEvent.OpenAddEventWithDuration(day, startHour, endHour))
                                },
                                onEventClick = { id ->
                                    if (state.showAddEditSheet) return@WeekViewGrid
                                    onNavigateToEventDetail(id)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp)
                            )
                            CalendarView.MONTH -> MonthViewGrid(
                                monthOffset = state.weekOffset,
                                selectedDay = state.selectedDay,
                                events = state.events,
                                onDaySelected = { viewModel.onEvent(CalendarUiEvent.DaySelected(it)) },
                                onEventClick = { id -> onNavigateToEventDetail(id) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            )
                            CalendarView.DAY -> Column {
                                if (state.weatherData != null) {
                                    WeatherWidget(
                                        weather = state.weatherData,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                }
                                DayView(
                                    dayOffset = state.weekOffset,
                                    selectedDay = state.selectedDay,
                                    events = state.events,
                                    onSlotClick = { day, hour ->
                                        viewModel.onEvent(CalendarUiEvent.OpenAddEventAtSlot(day, hour))
                                    },
                                    onSlotLongPress = { day, startHour, endHour ->
                                        viewModel.onEvent(CalendarUiEvent.OpenAddEventWithDuration(day, startHour, endHour))
                                    },
                                    onEventClick = { id -> onNavigateToEventDetail(id) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            CalendarView.AGENDA -> Column {
                                AvailabilityView(
                                    events = state.events,
                                    selectedDay = state.selectedDay,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                AgendaView(
                                    daysOffset = state.weekOffset,
                                    today = state.selectedDay,
                                    events = state.events,
                                    onEventClick = { id -> onNavigateToEventDetail(id) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            CalendarView.YEAR -> YearView(
                                selectedYear = state.selectedYear,
                                events = state.events,
                                onYearChanged = { viewModel.onEvent(CalendarUiEvent.YearSelected(it)) },
                                onDaySelected = { viewModel.onEvent(CalendarUiEvent.DaySelected(it)) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (state.showAddEditSheet) {
        AddEditEventSheet(
            eventId = state.editingEventId,
            prefilledStartMs = state.prefilledStartMs,
            prefilledEndMs = state.prefilledEndMs,
            onDismiss = { viewModel.dismissSheet() }
        )
    }

    if (state.showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { viewModel.onEvent(CalendarUiEvent.ToggleDatePicker) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(CalendarUiEvent.ToggleDatePicker) }) {
                    Text("OK", color = PxColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(CalendarUiEvent.ToggleDatePicker) }) {
                    Text("Cancel", color = PxColors.OnSurfaceDim)
                }
            }
        ) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.selectedDay
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
            DatePicker(state = datePickerState)
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    viewModel.onEvent(CalendarUiEvent.DatePicked(date))
                }
            }
        }
    }
}

@Composable
private fun CalendarModernTopBar(
    view: CalendarView,
    weekOffset: Int,
    headerDate: LocalDate,
    isTodayPulsing: Boolean,
    onViewChanged: (CalendarView) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onDatePicker: () -> Unit,
    onVoiceInput: () -> Unit,
) {
    val today = LocalDate.now()
    val headerText = when (view) {
        CalendarView.WEEK -> {
            val weekStart = today.plusWeeks(weekOffset.toLong())
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            runCatching { weekHeaderFormatter.format(weekStart) }.getOrElse { "-" }
        }
        CalendarView.MONTH -> {
            val month = YearMonth.now().plusMonths(weekOffset.toLong())
            runCatching { monthHeaderFormatter.format(month.atDay(1)) }.getOrElse { "-" }
        }
        CalendarView.DAY -> {
            val day = today.plusDays(weekOffset.toLong())
            runCatching { dayHeaderFormatter.format(day) }.getOrElse { "-" }
        }
        CalendarView.AGENDA -> "Upcoming Events"
        CalendarView.YEAR -> runCatching { yearHeaderFormatter.format(today) }.getOrElse { "-" }
    }

    val pulseScale by rememberInfiniteTransition(label = "todayPulse")
        .animateFloat(
            initialValue = 1f,
            targetValue = if (isTodayPulsing) 1.2f else 1f,
            animationSpec = if (isTodayPulsing)
                infiniteRepeatable<Float>(tween(400, easing = EaseInOutCubic), RepeatMode.Reverse)
            else
                infiniteRepeatable<Float>(tween(durationMillis = 0), RepeatMode.Restart),
            label = "pulse"
        )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFFEEEEF5),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )

            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        if (isTodayPulsing) Color(0xFF6366F1).copy(alpha = 0.15f)
                        else Color.Transparent
                    )
            ) {
                IconButton(onClick = onToday) {
                    Icon(Icons.Outlined.Today, "Today", tint = Color(0xFF6366F1))
                }
            }

            IconButton(onClick = onVoiceInput) {
                Icon(Icons.Outlined.Mic, "Voice input", tint = Color(0xFFCCCCD8))
            }
            IconButton(onClick = onDatePicker) {
                Icon(Icons.Outlined.DateRange, "Date picker", tint = Color(0xFFCCCCD8))
            }
            IconButton(onClick = onPrevious) {
                Icon(Icons.Outlined.ChevronLeft, "Previous", tint = Color(0xFFCCCCD8))
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Outlined.ChevronRight, "Next", tint = Color(0xFFCCCCD8))
            }
        }

        ViewSwitcherRow(
            currentView = view,
            onViewChanged = onViewChanged,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ViewSwitcherRow(
    currentView: CalendarView,
    onViewChanged: (CalendarView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val views = listOf(
        Triple(CalendarView.DAY, Icons.Outlined.ViewDay, "Day"),
        Triple(CalendarView.WEEK, Icons.Outlined.DateRange, "Week"),
        Triple(CalendarView.MONTH, Icons.Outlined.DateRange, "Month"),
        Triple(CalendarView.AGENDA, Icons.Outlined.ViewAgenda, "Agenda"),
        Triple(CalendarView.YEAR, Icons.Outlined.DateRange, "Year"),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A24))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        views.forEach { (view, icon, label) ->
            val isSelected = currentView == view

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF6366F1) else Color.Transparent,
                animationSpec = tween(200),
                label = "viewBg"
            )

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onViewChanged(view) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else Color(0xFF888899),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else Color(0xFF888899)
                )
            }
        }
    }
}
