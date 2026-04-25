package com.oussama_chatri.productivityx.features.events.presentation.state

import com.oussama_chatri.productivityx.core.enums.CalendarView
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import java.time.Instant
import java.time.LocalDate

data class CalendarUiState(
    val view: CalendarView                  = CalendarView.WEEK,
    val weekOffset: Int                     = 0,
    val selectedDay: LocalDate              = LocalDate.now(),
    val events: List<Event>                 = emptyList(),
    val isLoading: Boolean                  = false,
    val isRefreshing: Boolean               = false,
    val error: String?                      = null,
    val showAddEditSheet: Boolean           = false,
    val editingEventId: String?             = null,
    val prefilledStartMs: Long?             = null
)

data class AddEditEventUiState(
    val eventId: String?         = null,
    val title: String            = "",
    val description: String      = "",
    val location: String         = "",
    val startMs: Long            = Instant.now().toEpochMilli(),
    val endMs: Long              = Instant.now().plusSeconds(3600).toEpochMilli(),
    val isAllDay: Boolean        = false,
    val color: String            = "#6366F1",
    val recurrenceRule: String?  = null,
    val reminderMinutes: Int?    = null,
    val isLoading: Boolean       = false,
    val isSaved: Boolean         = false,
    val isDeleted: Boolean       = false,
    val titleError: String?      = null,
    val error: String?           = null
)
