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
import kotlinx.coroutines.delay
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
import java.time.YearMonth
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
            is CalendarUiEvent.ViewChanged -> {
                _uiState.update { it.copy(view = event.view) }
                observeCurrentRange()
            }
            is CalendarUiEvent.WeekChanged -> {
                _uiState.update { it.copy(weekOffset = it.weekOffset + event.offsetWeeks) }
                observeCurrentRange()
            }
            is CalendarUiEvent.MonthChanged -> {
                _uiState.update { it.copy(weekOffset = it.weekOffset + event.offsetMonths) }
                observeCurrentRange()
            }
            is CalendarUiEvent.DayChanged -> {
                _uiState.update { it.copy(weekOffset = it.weekOffset + event.offsetDays) }
                observeCurrentRange()
            }
            is CalendarUiEvent.DaySelected -> {
                val dayEvents = _uiState.value.events.filter { e ->
                    val eventDay = e.startAt.atZone(ZoneId.systemDefault()).toLocalDate()
                    eventDay == event.date && !e.isDeleted
                }.sortedBy { it.startAt }
                _uiState.update { it.copy(selectedDay = event.date, selectedDayEvents = dayEvents) }
            }
            CalendarUiEvent.NavigateToToday -> {
                _uiState.update {
                    it.copy(
                        weekOffset = 0,
                        selectedDay = LocalDate.now(),
                        selectedYear = LocalDate.now().year
                    )
                }
                observeCurrentRange()
                triggerTodayPulse()
            }
            is CalendarUiEvent.OpenAddEvent -> _uiState.update {
                it.copy(
                    showAddEditSheet = true,
                    editingEventId = null,
                    prefilledStartMs = event.prefilledDateTime
                        ?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                    prefilledEndMs = null
                )
            }
            is CalendarUiEvent.OpenAddEventAtSlot -> {
                val dateTime = event.date.atTime(event.startHour, 0)
                _uiState.update {
                    it.copy(
                        showAddEditSheet = true,
                        editingEventId = null,
                        prefilledStartMs = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        prefilledEndMs = null
                    )
                }
            }
            is CalendarUiEvent.OpenAddEventWithDuration -> {
                val startDt = event.date.atTime(event.startHour, 0)
                val endDt = event.date.atTime(event.endHour, 0)
                val startMs = startDt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endMs = endDt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                _uiState.update {
                    it.copy(
                        showAddEditSheet = true,
                        editingEventId = null,
                        prefilledStartMs = startMs,
                        prefilledEndMs = endMs
                    )
                }
            }
            is CalendarUiEvent.OpenEditEvent -> _uiState.update {
                it.copy(
                    showAddEditSheet = true,
                    editingEventId = event.eventId,
                    prefilledStartMs = null,
                    prefilledEndMs = null
                )
            }
            is CalendarUiEvent.DeleteEvent -> viewModelScope.launch {
                deleteEvent(event.eventId)
                _events.send(UiEvent.ShowSnackbar("Event deleted"))
                _uiState.update { it.copy(showAddEditSheet = false, editingEventId = null, prefilledEndMs = null) }
            }
            CalendarUiEvent.Refresh -> refresh()
            CalendarUiEvent.ClearError -> _uiState.update { it.copy(error = null) }
            CalendarUiEvent.ToggleDatePicker -> _uiState.update { it.copy(showDatePickerDialog = !it.showDatePickerDialog) }
            is CalendarUiEvent.DatePicked -> {
                val dayEvents = _uiState.value.events.filter { e ->
                    val eventDay = e.startAt.atZone(ZoneId.systemDefault()).toLocalDate()
                    eventDay == event.date && !e.isDeleted
                }.sortedBy { it.startAt }
                _uiState.update {
                    it.copy(
                        selectedDay = event.date,
                        showDatePickerDialog = false,
                        selectedDayEvents = dayEvents,
                        weekOffset = 0
                    )
                }
                observeCurrentRange()
            }
            CalendarUiEvent.TriggerTodayPulse -> triggerTodayPulse()
            is CalendarUiEvent.WeekHourHeightChanged -> _uiState.update { it.copy(weekHourHeight = event.height) }
            is CalendarUiEvent.YearSelected -> {
                _uiState.update { it.copy(selectedYear = event.year) }
                observeCurrentRange()
            }
            is CalendarUiEvent.OpenVoiceInput -> {
                _uiState.update {
                    it.copy(
                        showAddEditSheet = true,
                        editingEventId = null,
                        prefilledStartMs = event.dateTime
                            ?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                    )
                }
            }
        }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(showAddEditSheet = false, editingEventId = null, prefilledStartMs = null, prefilledEndMs = null) }
    }

    private fun triggerTodayPulse() {
        _uiState.update { it.copy(isTodayPulsing = true) }
        viewModelScope.launch {
            delay(1200)
            _uiState.update { it.copy(isTodayPulsing = false) }
        }
    }

    private fun observeCurrentRange() {
        observeJob?.cancel()
        val (from, to) = currentRange()
        observeJob = observeEvents(from, to)
            .onEach { list ->
                val state = _uiState.value
                val dayEvents = list.filter { e ->
                    val eventDay = e.startAt.atZone(ZoneId.systemDefault()).toLocalDate()
                    eventDay == state.selectedDay && !e.isDeleted
                }.sortedBy { it.startAt }
                _uiState.update { it.copy(events = list, selectedDayEvents = dayEvents, isLoading = false) }
            }
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
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()

        return when (state.view) {
            CalendarView.WEEK -> {
                val weekStart = today.plusWeeks(state.weekOffset.toLong())
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val from = weekStart.atStartOfDay(zone).toInstant()
                val to = weekStart.plusDays(7).atStartOfDay(zone).toInstant()
                from to to
            }
            CalendarView.MONTH -> {
                val monthStart = today.plusMonths(state.weekOffset.toLong()).withDayOfMonth(1)
                val from = monthStart.atStartOfDay(zone).toInstant()
                val to = monthStart.plusMonths(1).atStartOfDay(zone).toInstant()
                from to to
            }
            CalendarView.DAY -> {
                val day = today.plusDays(state.weekOffset.toLong())
                val from = day.atStartOfDay(zone).toInstant()
                val to = day.plusDays(1).atStartOfDay(zone).toInstant()
                from to to
            }
            CalendarView.AGENDA -> {
                val from = today.atStartOfDay(zone).toInstant()
                val to = today.plusDays(state.weekOffset.toLong()).plusMonths(3).atStartOfDay(zone).toInstant()
                from to to
            }
            CalendarView.YEAR -> {
                val yearStart = YearMonth.of(state.selectedYear, 1).atDay(1).atStartOfDay(zone).toInstant()
                val yearEnd = YearMonth.of(state.selectedYear, 12).atEndOfMonth().plusDays(1).atStartOfDay(zone).toInstant()
                yearStart to yearEnd
            }
        }
    }
}
