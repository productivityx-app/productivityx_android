package com.oussama_chatri.productivityx.features.events.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.enums.CalendarView
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.events.domain.usecase.DeleteEventUseCase
import com.oussama_chatri.productivityx.features.events.domain.usecase.ObserveEventsUseCase
import com.oussama_chatri.productivityx.features.events.domain.usecase.RefreshEventsUseCase
import com.oussama_chatri.productivityx.features.events.presentation.event.CalendarUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.state.CalendarUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val observeEvents: ObserveEventsUseCase,
    private val deleteEvent: DeleteEventUseCase,
    private val refreshEvents: RefreshEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var observeJob: Job? = null

    init {
        observeCurrentRange()
    }

    fun onEvent(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.ViewChanged    -> {
                _uiState.update { it.copy(view = event.view) }
                observeCurrentRange()
            }
            is CalendarUiEvent.WeekChanged    -> {
                _uiState.update { it.copy(weekOffset = it.weekOffset + event.offsetWeeks) }
                observeCurrentRange()
            }
            is CalendarUiEvent.DaySelected    -> _uiState.update { it.copy(selectedDay = event.date) }
            CalendarUiEvent.NavigateToToday   -> {
                _uiState.update { it.copy(weekOffset = 0, selectedDay = LocalDate.now()) }
                observeCurrentRange()
            }
            is CalendarUiEvent.OpenAddEvent   -> _uiState.update {
                it.copy(
                    showAddEditSheet  = true,
                    editingEventId    = null,
                    prefilledStartMs  = event.prefilledDateTime
                        ?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                )
            }
            is CalendarUiEvent.OpenEditEvent  -> _uiState.update {
                it.copy(
                    showAddEditSheet = true,
                    editingEventId   = event.eventId,
                    prefilledStartMs = null
                )
            }
            is CalendarUiEvent.DeleteEvent    -> viewModelScope.launch {
                deleteEvent(event.eventId)
                _events.send(UiEvent.ShowSnackbar("Event deleted"))
                _uiState.update { it.copy(showAddEditSheet = false, editingEventId = null) }
            }
            CalendarUiEvent.Refresh           -> refresh()
            CalendarUiEvent.ClearError        -> _uiState.update { it.copy(error = null) }
        }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(showAddEditSheet = false, editingEventId = null, prefilledStartMs = null) }
    }

    private fun observeCurrentRange() {
        observeJob?.cancel()
        val (from, to) = currentRange()
        observeJob = observeEvents(from, to)
            .onEach { list -> _uiState.update { it.copy(events = list, isLoading = false) } }
            .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val (from, to) = currentRange()
            refreshEvents(from, to)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun currentRange(): Pair<Instant, Instant> {
        val state = _uiState.value
        val zone  = ZoneId.systemDefault()

        return if (state.view == CalendarView.WEEK) {
            val weekStart = LocalDate.now()
                .plusWeeks(state.weekOffset.toLong())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val from = weekStart.atStartOfDay(zone).toInstant()
            val to   = weekStart.plusDays(7).atStartOfDay(zone).toInstant()
            from to to
        } else {
            val monthStart = LocalDate.now()
                .plusMonths(state.weekOffset.toLong())
                .withDayOfMonth(1)
            val from = monthStart.atStartOfDay(zone).toInstant()
            val to   = monthStart.plusMonths(1).atStartOfDay(zone).toInstant()
            from to to
        }
    }
}
