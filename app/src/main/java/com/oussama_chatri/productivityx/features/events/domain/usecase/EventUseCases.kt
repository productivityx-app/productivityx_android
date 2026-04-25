package com.oussama_chatri.productivityx.features.events.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import com.oussama_chatri.productivityx.features.events.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class ObserveEventsUseCase @Inject constructor(private val repo: EventRepository) {
    operator fun invoke(from: Instant, to: Instant): Flow<List<Event>> =
        repo.observeEvents(from, to)
}

class ObserveUpcomingEventsUseCase @Inject constructor(private val repo: EventRepository) {
    operator fun invoke(limit: Int = 5): Flow<List<Event>> =
        repo.observeUpcomingEvents(limit)
}

class GetEventByIdUseCase @Inject constructor(private val repo: EventRepository) {
    suspend operator fun invoke(eventId: String): Resource<Event> =
        repo.getEventById(eventId)
}

class CreateEventUseCase @Inject constructor(private val repo: EventRepository) {
    suspend operator fun invoke(
        title: String,
        description: String? = null,
        location: String? = null,
        startAt: Instant,
        endAt: Instant,
        isAllDay: Boolean = false,
        color: String = "#6366F1",
        recurrenceRule: String? = null,
        recurrenceEndAt: Instant? = null,
        reminderMinutes: Int? = null
    ): Resource<Event> = repo.createEvent(
        title, description, location, startAt, endAt,
        isAllDay, color, recurrenceRule, recurrenceEndAt, reminderMinutes
    )
}

class UpdateEventUseCase @Inject constructor(private val repo: EventRepository) {
    suspend operator fun invoke(
        eventId: String,
        title: String,
        description: String? = null,
        location: String? = null,
        startAt: Instant,
        endAt: Instant,
        isAllDay: Boolean = false,
        color: String = "#6366F1",
        recurrenceRule: String? = null,
        recurrenceEndAt: Instant? = null,
        reminderMinutes: Int? = null
    ): Resource<Event> = repo.updateEvent(
        eventId, title, description, location, startAt, endAt,
        isAllDay, color, recurrenceRule, recurrenceEndAt, reminderMinutes
    )
}

class DeleteEventUseCase @Inject constructor(private val repo: EventRepository) {
    suspend operator fun invoke(eventId: String): Resource<Unit> =
        repo.deleteEvent(eventId)
}

class RestoreEventUseCase @Inject constructor(private val repo: EventRepository) {
    suspend operator fun invoke(eventId: String): Resource<Event> =
        repo.restoreEvent(eventId)
}

class RefreshEventsUseCase @Inject constructor(private val repo: EventRepository) {
    suspend operator fun invoke(from: Instant, to: Instant): Resource<Unit> =
        repo.refreshEvents(from, to)
}
