package com.oussama_chatri.productivityx.features.events.domain.repository

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface EventRepository {

    fun observeEvents(from: Instant, to: Instant): Flow<List<Event>>

    fun observeUpcomingEvents(limit: Int = 5): Flow<List<Event>>

    suspend fun getEventById(eventId: String): Resource<Event>

    suspend fun createEvent(
        title: String,
        description: String?,
        location: String?,
        startAt: Instant,
        endAt: Instant,
        isAllDay: Boolean,
        color: String,
        recurrenceRule: String?,
        recurrenceEndAt: Instant?,
        reminderMinutes: Int?
    ): Resource<Event>

    suspend fun updateEvent(
        eventId: String,
        title: String,
        description: String?,
        location: String?,
        startAt: Instant,
        endAt: Instant,
        isAllDay: Boolean,
        color: String,
        recurrenceRule: String?,
        recurrenceEndAt: Instant?,
        reminderMinutes: Int?
    ): Resource<Event>

    suspend fun deleteEvent(eventId: String): Resource<Unit>

    suspend fun restoreEvent(eventId: String): Resource<Event>

    suspend fun permanentDeleteEvent(eventId: String): Resource<Unit>

    suspend fun deleteSeries(eventId: String): Resource<Unit>

    suspend fun listTrashEvents(page: Int = 0, size: Int = 50): Resource<List<Event>>

    suspend fun refreshEvents(from: Instant, to: Instant): Resource<Unit>
}
