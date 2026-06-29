package com.oussama_chatri.productivityx.features.events.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import com.oussama_chatri.productivityx.features.events.domain.usecase.CreateEventUseCase
import com.oussama_chatri.productivityx.features.events.domain.usecase.DeleteEventUseCase
import com.oussama_chatri.productivityx.features.events.domain.usecase.GetEventByIdUseCase
import com.oussama_chatri.productivityx.features.events.domain.usecase.ObserveEventsUseCase
import com.oussama_chatri.productivityx.features.events.domain.usecase.UpdateEventUseCase
import com.oussama_chatri.productivityx.features.events.presentation.event.AddEditEventUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.state.AddEditEventUiState
import com.oussama_chatri.productivityx.features.events.presentation.state.EventTemplateType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class AddEditEventViewModel @Inject constructor(
    private val getEventById: GetEventByIdUseCase,
    private val createEvent: CreateEventUseCase,
    private val updateEvent: UpdateEventUseCase,
    private val deleteEvent: DeleteEventUseCase,
    private val observeEvents: ObserveEventsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditEventUiState())
    val uiState: StateFlow<AddEditEventUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var prefilledEndMs: Long? = null

    fun init(eventId: String?, prefilledStartMs: Long?, prefilledEndMs: Long? = null) {
        this.prefilledEndMs = prefilledEndMs
        if (eventId != null) {
            loadEvent(eventId)
        } else if (prefilledStartMs != null) {
            val end = prefilledEndMs ?: (prefilledStartMs + 3_600_000L)
            _uiState.update {
                it.copy(startMs = prefilledStartMs, endMs = end)
            }
            generateTitleSuggestions()
        } else {
            generateTitleSuggestions()
        }
    }

    private fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getEventById(eventId)) {
                is Resource.Success -> {
                    val e = result.data
                    _uiState.update {
                        it.copy(
                            eventId = e.id,
                            title = e.title,
                            description = e.description ?: "",
                            location = e.location ?: "",
                            startMs = e.startAt.toEpochMilli(),
                            endMs = e.endAt.toEpochMilli(),
                            isAllDay = e.isAllDay,
                            color = e.color,
                            recurrenceRule = e.recurrenceRule,
                            reminderMinutes = e.reminderMinutes,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun onEvent(event: AddEditEventUiEvent) {
        when (event) {
            is AddEditEventUiEvent.TitleChanged -> _uiState.update { it.copy(title = event.value, titleError = null) }
            is AddEditEventUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value) }
            is AddEditEventUiEvent.LocationChanged -> _uiState.update { it.copy(location = event.value) }
            is AddEditEventUiEvent.StartDateTimeChanged -> _uiState.update {
                val endMs = if (event.epochMillis >= it.endMs) event.epochMillis + 3_600_000L else it.endMs
                it.copy(startMs = event.epochMillis, endMs = endMs)
            }
            is AddEditEventUiEvent.EndDateTimeChanged -> _uiState.update { it.copy(endMs = event.epochMillis) }
            is AddEditEventUiEvent.AllDayToggled -> _uiState.update { it.copy(isAllDay = event.isAllDay) }
            is AddEditEventUiEvent.ColorSelected -> _uiState.update { it.copy(color = event.hex) }
            is AddEditEventUiEvent.RecurrenceRuleChanged -> _uiState.update { it.copy(recurrenceRule = event.rule) }
            is AddEditEventUiEvent.ReminderMinutesChanged -> _uiState.update { it.copy(reminderMinutes = event.minutes) }
            is AddEditEventUiEvent.ReminderTimesChanged -> _uiState.update { it.copy(reminderTimes = event.times) }
            is AddEditEventUiEvent.AddReminderTime -> _uiState.update {
                val times = it.reminderTimes + event.minutes
                it.copy(reminderTimes = times.distinct().sorted())
            }
            is AddEditEventUiEvent.RemoveReminderTime -> _uiState.update {
                it.copy(reminderTimes = it.reminderTimes - event.minutes)
            }
            AddEditEventUiEvent.Save -> save()
            AddEditEventUiEvent.Delete -> delete()
            AddEditEventUiEvent.Dismiss -> viewModelScope.launch { _events.send(UiEvent.NavigateBack) }
            is AddEditEventUiEvent.TemplateApplied -> applyTemplate(event.template)
            is AddEditEventUiEvent.TitleSuggestionAccepted -> _uiState.update { it.copy(title = event.suggestion) }
            is AddEditEventUiEvent.TravelTimeChanged -> _uiState.update { it.copy(travelTimeMinutes = event.minutes) }
            is AddEditEventUiEvent.MeetingUrlChanged -> _uiState.update { it.copy(meetingUrl = event.url) }
            is AddEditEventUiEvent.AddAttendee -> _uiState.update { it.copy(attendees = it.attendees + event.email) }
            is AddEditEventUiEvent.RemoveAttendee -> _uiState.update { it.copy(attendees = it.attendees - event.email) }
            AddEditEventUiEvent.CheckConflicts -> checkConflicts()
            is AddEditEventUiEvent.VoiceTitleResult -> _uiState.update { it.copy(title = event.title) }
        }
    }

    private fun applyTemplate(template: EventTemplateType) {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startMs: Long
        val endMs: Long
        val title: String
        val color: String
        val reminderMinutes: Int?

        when (template) {
            EventTemplateType.MEETING -> {
                val nextHour = LocalTime.now(zone).hour + 1
                val start = today.atTime(nextHour.coerceAtMost(23), 0)
                startMs = start.atZone(zone).toInstant().toEpochMilli()
                endMs = start.plusHours(1).atZone(zone).toInstant().toEpochMilli()
                title = "Meeting"
                color = "#6366F1"
                reminderMinutes = 10
            }
            EventTemplateType.FOCUS_TIME -> {
                val nextHour = LocalTime.now(zone).hour + 1
                val start = today.atTime(nextHour.coerceAtMost(23), 0)
                startMs = start.atZone(zone).toInstant().toEpochMilli()
                endMs = start.plusMinutes(25).atZone(zone).toInstant().toEpochMilli()
                title = "Focus Time"
                color = "#22C55E"
                reminderMinutes = 5
            }
            EventTemplateType.BREAK -> {
                val nextHour = LocalTime.now(zone).hour + 1
                val start = today.atTime(nextHour.coerceAtMost(23), 0)
                startMs = start.atZone(zone).toInstant().toEpochMilli()
                endMs = start.plusMinutes(15).atZone(zone).toInstant().toEpochMilli()
                title = "Break"
                color = "#F59E0B"
                reminderMinutes = null
            }
            EventTemplateType.TRAVEL -> {
                val nextHour = LocalTime.now(zone).hour + 1
                val start = today.atTime(nextHour.coerceAtMost(23), 0)
                startMs = start.atZone(zone).toInstant().toEpochMilli()
                endMs = start.plusMinutes(30).atZone(zone).toInstant().toEpochMilli()
                title = "Travel"
                color = "#8B5CF6"
                reminderMinutes = 15
            }
        }
        _uiState.update {
            it.copy(
                eventTemplate = template,
                title = title,
                startMs = startMs,
                endMs = endMs,
                color = color,
                reminderMinutes = reminderMinutes,
            )
        }
    }

    private fun generateTitleSuggestions() {
        val today = LocalDate.now()
        val dayName = today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val suggestions = listOf(
            "Meeting with team",
            "$dayName standup",
            "Focus session",
            "Lunch break",
            "Review PRs",
            "Client call",
            "1:1 with manager",
        )
        _uiState.update { it.copy(titleSuggestions = suggestions) }
    }

    private fun checkConflicts() {
        val state = _uiState.value
        if (state.eventId != null) return
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val from = Instant.ofEpochMilli(state.startMs).minusSeconds(3600)
            val to = Instant.ofEpochMilli(state.endMs).plusSeconds(3600)
            val existingEvents: List<Event> = observeEvents(from, to).first()
            val conflicts = existingEvents.filter { e ->
                e.id != state.eventId && !e.isDeleted &&
                    e.startAt.toEpochMilli() < state.endMs &&
                    e.endAt.toEpochMilli() > state.startMs
            }
            _uiState.update {
                it.copy(
                    showConflictWarning = conflicts.isNotEmpty(),
                    conflictingEvents = conflicts
                )
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val startAt = Instant.ofEpochMilli(state.startMs)
            val endAt = Instant.ofEpochMilli(state.endMs)

            val result = if (state.eventId == null) {
                createEvent(
                    title = state.title.trim(),
                    description = state.description.ifBlank { null },
                    location = state.location.ifBlank { null },
                    startAt = startAt,
                    endAt = endAt,
                    isAllDay = state.isAllDay,
                    color = state.color,
                    recurrenceRule = state.recurrenceRule,
                    reminderMinutes = state.reminderMinutes
                )
            } else {
                updateEvent(
                    eventId = state.eventId,
                    title = state.title.trim(),
                    description = state.description.ifBlank { null },
                    location = state.location.ifBlank { null },
                    startAt = startAt,
                    endAt = endAt,
                    isAllDay = state.isAllDay,
                    color = state.color,
                    recurrenceRule = state.recurrenceRule,
                    reminderMinutes = state.reminderMinutes
                )
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    _events.send(UiEvent.NavigateBack)
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    private fun delete() {
        val eventId = _uiState.value.eventId ?: return
        viewModelScope.launch {
            deleteEvent(eventId)
            _uiState.update { it.copy(isDeleted = true) }
            _events.send(UiEvent.NavigateBack)
        }
    }
}
