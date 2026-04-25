package com.oussama_chatri.productivityx.features.events.presentation.event

import com.oussama_chatri.productivityx.core.enums.CalendarView
import java.time.LocalDate
import java.time.LocalDateTime

sealed class CalendarUiEvent {
    data class ViewChanged(val view: CalendarView)           : CalendarUiEvent()
    data class WeekChanged(val offsetWeeks: Int)             : CalendarUiEvent()
    data class DaySelected(val date: LocalDate)              : CalendarUiEvent()
    data object NavigateToToday                              : CalendarUiEvent()
    data class OpenAddEvent(val prefilledDateTime: LocalDateTime? = null) : CalendarUiEvent()
    data class OpenEditEvent(val eventId: String)            : CalendarUiEvent()
    data class DeleteEvent(val eventId: String)              : CalendarUiEvent()
    data object Refresh                                      : CalendarUiEvent()
    data object ClearError                                   : CalendarUiEvent()
}

sealed class AddEditEventUiEvent {
    data class TitleChanged(val value: String)                  : AddEditEventUiEvent()
    data class DescriptionChanged(val value: String)            : AddEditEventUiEvent()
    data class LocationChanged(val value: String)               : AddEditEventUiEvent()
    data class StartDateTimeChanged(val epochMillis: Long)      : AddEditEventUiEvent()
    data class EndDateTimeChanged(val epochMillis: Long)        : AddEditEventUiEvent()
    data class AllDayToggled(val isAllDay: Boolean)             : AddEditEventUiEvent()
    data class ColorSelected(val hex: String)                   : AddEditEventUiEvent()
    data class RecurrenceRuleChanged(val rule: String?)         : AddEditEventUiEvent()
    data class ReminderMinutesChanged(val minutes: Int?)        : AddEditEventUiEvent()
    data object Save                                            : AddEditEventUiEvent()
    data object Delete                                          : AddEditEventUiEvent()
    data object Dismiss                                         : AddEditEventUiEvent()
}
