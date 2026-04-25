package com.oussama_chatri.productivityx.features.events.domain.model

import com.oussama_chatri.productivityx.core.enums.SyncStatus
import java.time.Instant

data class Event(
    val id: String,
    val userId: String,
    val recurrenceParentId: String?,
    val title: String,
    val description: String?,
    val location: String?,
    val startAt: Instant,
    val endAt: Instant,
    val isAllDay: Boolean,
    val color: String,
    val recurrenceRule: String?,
    val recurrenceEndAt: Instant?,
    val reminderMinutes: Int?,
    val isDeleted: Boolean,
    val deletedAt: Instant?,
    val version: Int,
    val syncStatus: SyncStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val isRecurring: Boolean get() = recurrenceRule != null
    val isRecurrenceInstance: Boolean get() = recurrenceParentId != null
    val durationMinutes: Long get() {
        val diff = endAt.epochSecond - startAt.epochSecond
        return diff / 60
    }
}
