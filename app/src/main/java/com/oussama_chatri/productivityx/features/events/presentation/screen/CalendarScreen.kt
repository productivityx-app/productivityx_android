package com.oussama_chatri.productivityx.features.events.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.core.enums.CalendarView
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.events.presentation.components.AddEditEventSheet
import com.oussama_chatri.productivityx.features.events.presentation.components.MonthViewGrid
import com.oussama_chatri.productivityx.features.events.presentation.components.WeekViewGrid
import com.oussama_chatri.productivityx.features.events.presentation.event.CalendarUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.viewmodel.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private val weekHeaderFormatter  = DateTimeFormatter.ofPattern("MMMM yyyy")
private val monthHeaderFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onNavigateToEventDetail: (String) -> Unit,
    showAddEvent: Boolean = false,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state           by viewModel.uiState.collectAsState()
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
                else                    -> Unit
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CalendarTopBar(
                view        = state.view,
                weekOffset  = state.weekOffset,
                onToggleView = {
                    viewModel.onEvent(
                        CalendarUiEvent.ViewChanged(
                            if (state.view == CalendarView.WEEK) CalendarView.MONTH else CalendarView.WEEK
                        )
                    )
                },
                onPrevious   = { viewModel.onEvent(CalendarUiEvent.WeekChanged(-1)) },
                onNext       = { viewModel.onEvent(CalendarUiEvent.WeekChanged(1)) },
                onToday      = { viewModel.onEvent(CalendarUiEvent.NavigateToToday) }
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh    = { viewModel.onEvent(CalendarUiEvent.Refresh) },
                modifier     = Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = state.view,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label       = "calendar_view_switch"
                ) { view ->
                    when (view) {
                        CalendarView.WEEK -> WeekViewGrid(
                            weekOffset   = state.weekOffset,
                            selectedDay  = state.selectedDay,
                            events       = state.events,
                            onDaySelected = { viewModel.onEvent(CalendarUiEvent.DaySelected(it)) },
                            onSlotClick  = { day, hour ->
                                val dateTime = day.atTime(hour, 0)
                                viewModel.onEvent(CalendarUiEvent.OpenAddEvent(dateTime))
                            },
                            onEventClick = { id ->
                                if (state.showAddEditSheet) return@WeekViewGrid
                                onNavigateToEventDetail(id)
                            },
                            modifier     = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        )
                        CalendarView.MONTH -> MonthViewGrid(
                            monthOffset  = state.weekOffset,
                            selectedDay  = state.selectedDay,
                            events       = state.events,
                            onDaySelected = { viewModel.onEvent(CalendarUiEvent.DaySelected(it)) },
                            onEventClick = { id -> onNavigateToEventDetail(id) },
                            modifier     = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter),
        )
    }

    // Bottom sheet
    if (state.showAddEditSheet) {
        AddEditEventSheet(
            eventId          = state.editingEventId,
            prefilledStartMs = state.prefilledStartMs,
            onDismiss        = { viewModel.dismissSheet() }
        )
    }
}

@Composable
private fun CalendarTopBar(
    view: CalendarView,
    weekOffset: Int,
    onToggleView: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val today = LocalDate.now()
    val headerText = if (view == CalendarView.WEEK) {
        val weekStart = today.plusWeeks(weekOffset.toLong())
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        weekHeaderFormatter.format(weekStart)
    } else {
        val month = YearMonth.now().plusMonths(weekOffset.toLong())
        monthHeaderFormatter.format(month.atDay(1))
    }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = headerText,
            style    = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color    = Color(0xFFEEEEF5),
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )

        TextButton(onClick = onToday) {
            Icon(Icons.Outlined.Today, null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Today", color = Color(0xFF6366F1), style = MaterialTheme.typography.labelMedium)
        }

        IconButton(onClick = onPrevious) {
            Icon(Icons.Outlined.ChevronLeft, "Previous", tint = Color(0xFFCCCCD8))
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Outlined.ChevronRight, "Next", tint = Color(0xFFCCCCD8))
        }
        IconButton(onClick = onToggleView) {
            Icon(
                imageVector        = if (view == CalendarView.WEEK) Icons.Outlined.CalendarViewMonth else Icons.Outlined.CalendarViewDay,
                contentDescription = "Toggle view",
                tint               = Color(0xFFCCCCD8)
            )
        }
    }
}
