package com.oussama_chatri.productivityx.features.events.presentation.state

import com.oussama_chatri.productivityx.core.enums.CalendarView
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class CalendarUiState(
    val view: CalendarView = CalendarView.WEEK,
    val weekOffset: Int = 0,
    val selectedDay: LocalDate = LocalDate.now(),
    val selectedDayEvents: List<Event> = emptyList(),
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val showAddEditSheet: Boolean = false,
    val editingEventId: String? = null,
    val prefilledStartMs: Long? = null,
    val prefilledEndMs: Long? = null,
    val showDatePickerDialog: Boolean = false,
    val isTodayPulsing: Boolean = false,
    val weekHourHeight: Float = 60f,
    val selectedYear: Int = LocalDate.now().year,
    val weatherData: WeatherData? = null,
)

data class WeatherData(
    val temperature: Int,
    val condition: String,
    val icon: String,
)

data class AddEditEventUiState(
    val eventId: String? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startMs: Long = Instant.now().toEpochMilli(),
    val endMs: Long = Instant.now().plusSeconds(3600).toEpochMilli(),
    val isAllDay: Boolean = false,
    val color: String = "#6366F1",
    val recurrenceRule: String? = null,
    val reminderMinutes: Int? = null,
    val reminderTimes: List<Int> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val titleError: String? = null,
    val error: String? = null,
    val eventTemplate: EventTemplateType? = null,
    val showConflictWarning: Boolean = false,
    val conflictingEvents: List<Event> = emptyList(),
    val titleSuggestions: List<String> = emptyList(),
    val travelTimeMinutes: Int? = null,
    val meetingUrl: String? = null,
    val attendees: List<String> = emptyList(),
)

enum class EventTemplateType(val label: String) {
    MEETING("Meeting"),
    FOCUS_TIME("Focus Time"),
    BREAK("Break"),
    TRAVEL("Travel"),
}
