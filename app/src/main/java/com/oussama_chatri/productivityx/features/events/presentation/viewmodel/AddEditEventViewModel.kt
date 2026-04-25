package com.oussama_chatri.productivityx.features.events.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.events.domain.usecase.CreateEventUseCase
import com.oussama_chatri.productivityx.features.events.domain.usecase.DeleteEventUseCase
import com.oussama_chatri.productivityx.features.events.domain.usecase.GetEventByIdUseCase
import com.oussama_chatri.productivityx.features.events.domain.usecase.UpdateEventUseCase
import com.oussama_chatri.productivityx.features.events.presentation.event.AddEditEventUiEvent
import com.oussama_chatri.productivityx.features.events.presentation.state.AddEditEventUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class AddEditEventViewModel @Inject constructor(
    private val getEventById: GetEventByIdUseCase,
    private val createEvent: CreateEventUseCase,
    private val updateEvent: UpdateEventUseCase,
    private val deleteEvent: DeleteEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditEventUiState())
    val uiState: StateFlow<AddEditEventUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun init(eventId: String?, prefilledStartMs: Long?) {
        if (eventId != null) {
            loadEvent(eventId)
        } else if (prefilledStartMs != null) {
            _uiState.update {
                it.copy(
                    startMs = prefilledStartMs,
                    endMs   = prefilledStartMs + 3_600_000L
                )
            }
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
                            eventId         = e.id,
                            title           = e.title,
                            description     = e.description ?: "",
                            location        = e.location ?: "",
                            startMs         = e.startAt.toEpochMilli(),
                            endMs           = e.endAt.toEpochMilli(),
                            isAllDay        = e.isAllDay,
                            color           = e.color,
                            recurrenceRule  = e.recurrenceRule,
                            reminderMinutes = e.reminderMinutes,
                            isLoading       = false
                        )
                    }
                }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun onEvent(event: AddEditEventUiEvent) {
        when (event) {
            is AddEditEventUiEvent.TitleChanged          -> _uiState.update { it.copy(title = event.value, titleError = null) }
            is AddEditEventUiEvent.DescriptionChanged    -> _uiState.update { it.copy(description = event.value) }
            is AddEditEventUiEvent.LocationChanged       -> _uiState.update { it.copy(location = event.value) }
            is AddEditEventUiEvent.StartDateTimeChanged  -> _uiState.update {
                val endMs = if (event.epochMillis >= it.endMs) event.epochMillis + 3_600_000L else it.endMs
                it.copy(startMs = event.epochMillis, endMs = endMs)
            }
            is AddEditEventUiEvent.EndDateTimeChanged    -> _uiState.update { it.copy(endMs = event.epochMillis) }
            is AddEditEventUiEvent.AllDayToggled         -> _uiState.update { it.copy(isAllDay = event.isAllDay) }
            is AddEditEventUiEvent.ColorSelected         -> _uiState.update { it.copy(color = event.hex) }
            is AddEditEventUiEvent.RecurrenceRuleChanged -> _uiState.update { it.copy(recurrenceRule = event.rule) }
            is AddEditEventUiEvent.ReminderMinutesChanged -> _uiState.update { it.copy(reminderMinutes = event.minutes) }
            AddEditEventUiEvent.Save                     -> save()
            AddEditEventUiEvent.Delete                   -> delete()
            AddEditEventUiEvent.Dismiss                  -> viewModelScope.launch { _events.send(UiEvent.NavigateBack) }
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
            val endAt   = Instant.ofEpochMilli(state.endMs)

            val result = if (state.eventId == null) {
                createEvent(
                    title           = state.title.trim(),
                    description     = state.description.ifBlank { null },
                    location        = state.location.ifBlank { null },
                    startAt         = startAt,
                    endAt           = endAt,
                    isAllDay        = state.isAllDay,
                    color           = state.color,
                    recurrenceRule  = state.recurrenceRule,
                    reminderMinutes = state.reminderMinutes
                )
            } else {
                updateEvent(
                    eventId         = state.eventId,
                    title           = state.title.trim(),
                    description     = state.description.ifBlank { null },
                    location        = state.location.ifBlank { null },
                    startAt         = startAt,
                    endAt           = endAt,
                    isAllDay        = state.isAllDay,
                    color           = state.color,
                    recurrenceRule  = state.recurrenceRule,
                    reminderMinutes = state.reminderMinutes
                )
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    _events.send(UiEvent.NavigateBack)
                }
                is Resource.Error   -> _uiState.update { it.copy(isLoading = false, error = result.message) }
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
