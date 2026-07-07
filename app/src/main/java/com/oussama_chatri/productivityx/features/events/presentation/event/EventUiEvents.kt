package com.oussama_chatri.productivityx.features.events.presentation.event

import com.oussama_chatri.productivityx.core.enums.CalendarView
import com.oussama_chatri.productivityx.features.events.presentation.state.EventTemplateType
import java.time.LocalDate
import java.time.LocalDateTime

sealed class CalendarUiEvent {
    data class ViewChanged(val view: CalendarView) : CalendarUiEvent()
    data class WeekChanged(val offsetWeeks: Int) : CalendarUiEvent()
    data class MonthChanged(val offsetMonths: Int) : CalendarUiEvent()
    data class DayChanged(val offsetDays: Int) : CalendarUiEvent()
    data class DaySelected(val date: LocalDate) : CalendarUiEvent()
    data object NavigateToToday : CalendarUiEvent()
    data class OpenAddEvent(val prefilledDateTime: LocalDateTime? = null) : CalendarUiEvent()
    data class OpenAddEventAtSlot(val date: LocalDate, val startHour: Int) : CalendarUiEvent()
    data class OpenAddEventWithDuration(val date: LocalDate, val startHour: Int, val endHour: Int) : CalendarUiEvent()
    data class OpenEditEvent(val eventId: String) : CalendarUiEvent()
    data class DeleteEvent(val eventId: String) : CalendarUiEvent()
    data object Refresh : CalendarUiEvent()
    data object ClearError : CalendarUiEvent()
    data object ToggleDatePicker : CalendarUiEvent()
    data class DatePicked(val date: LocalDate) : CalendarUiEvent()
    data object TriggerTodayPulse : CalendarUiEvent()
    data class WeekHourHeightChanged(val height: Float) : CalendarUiEvent()
    data class YearSelected(val year: Int) : CalendarUiEvent()
    data class OpenVoiceInput(val dateTime: LocalDateTime? = null) : CalendarUiEvent()
}

sealed class AddEditEventUiEvent {
    data class TitleChanged(val value: String) : AddEditEventUiEvent()
    data class DescriptionChanged(val value: String) : AddEditEventUiEvent()
    data class LocationChanged(val value: String) : AddEditEventUiEvent()
    data class StartDateTimeChanged(val epochMillis: Long) : AddEditEventUiEvent()
    data class EndDateTimeChanged(val epochMillis: Long) : AddEditEventUiEvent()
    data class AllDayToggled(val isAllDay: Boolean) : AddEditEventUiEvent()
    data class ColorSelected(val hex: String) : AddEditEventUiEvent()
    data class RecurrenceRuleChanged(val rule: String?) : AddEditEventUiEvent()
    data class ReminderMinutesChanged(val minutes: Int?) : AddEditEventUiEvent()
    data class ReminderTimesChanged(val times: List<Int>) : AddEditEventUiEvent()
    data class AddReminderTime(val minutes: Int) : AddEditEventUiEvent()
    data class RemoveReminderTime(val minutes: Int) : AddEditEventUiEvent()
    data object Save : AddEditEventUiEvent()
    data object Delete : AddEditEventUiEvent()
    data object Dismiss : AddEditEventUiEvent()
    data class TemplateApplied(val template: EventTemplateType) : AddEditEventUiEvent()
    data class TitleSuggestionAccepted(val suggestion: String) : AddEditEventUiEvent()
    data class TravelTimeChanged(val minutes: Int?) : AddEditEventUiEvent()
    data class MeetingUrlChanged(val url: String) : AddEditEventUiEvent()
    data class AddAttendee(val email: String) : AddEditEventUiEvent()
    data class RemoveAttendee(val email: String) : AddEditEventUiEvent()
    data object CheckConflicts : AddEditEventUiEvent()
    data class VoiceTitleResult(val title: String) : AddEditEventUiEvent()
    data object Duplicate : AddEditEventUiEvent()
}
