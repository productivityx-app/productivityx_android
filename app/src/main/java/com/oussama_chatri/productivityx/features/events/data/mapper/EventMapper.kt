package com.oussama_chatri.productivityx.features.events.data.mapper

import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.features.events.data.local.EventEntity
import com.oussama_chatri.productivityx.features.events.data.remote.dto.EventResponseDto
import com.oussama_chatri.productivityx.features.events.domain.model.Event
import java.time.Instant

private fun parseInstantOrNow(value: String?): Instant =
    if (value.isNullOrBlank()) Instant.now()
    else runCatching { Instant.parse(value) }.getOrDefault(Instant.now())

private fun parseInstantOrNull(value: String?): Instant? =
    if (value.isNullOrBlank()) null
    else runCatching { Instant.parse(value) }.getOrNull()

fun EventResponseDto.toEntity(fallbackUserId: String = "") = EventEntity(
    id                  = id,
    userId              = userId.orEmpty().ifBlank { fallbackUserId },
    recurrenceParentId  = recurrenceParentId,
    title               = title,
    description         = description,
    location            = location,
    startAt             = parseInstantOrNow(startAt).toEpochMilli(),
    endAt               = parseInstantOrNow(endAt).toEpochMilli(),
    isAllDay            = isAllDay,
    color               = color,
    recurrenceRule      = recurrenceRule,
    recurrenceEndAt     = parseInstantOrNull(recurrenceEndAt)?.toEpochMilli(),
    reminderMinutes     = reminderMinutes,
    isDeleted           = deleted,
    deletedAt           = parseInstantOrNull(deletedAt)?.toEpochMilli(),
    version             = version,
    syncStatus          = SyncStatus.SYNCED,
    createdAt           = parseInstantOrNow(createdAt).toEpochMilli(),
    updatedAt           = parseInstantOrNow(updatedAt).toEpochMilli(),
    pendingOperation    = null
)

fun EventResponseDto.toDomain(fallbackUserId: String = "") = Event(
    id                  = id,
    userId              = userId.orEmpty().ifBlank { fallbackUserId },
    recurrenceParentId  = recurrenceParentId,
    title               = title,
    description         = description,
    location            = location,
    startAt             = parseInstantOrNow(startAt),
    endAt               = parseInstantOrNow(endAt),
    isAllDay            = isAllDay,
    color               = color,
    recurrenceRule      = recurrenceRule,
    recurrenceEndAt     = parseInstantOrNull(recurrenceEndAt),
    reminderMinutes     = reminderMinutes,
    isDeleted           = deleted,
    deletedAt           = parseInstantOrNull(deletedAt),
    version             = version,
    syncStatus          = SyncStatus.SYNCED,
    createdAt           = parseInstantOrNow(createdAt),
    updatedAt           = parseInstantOrNow(updatedAt)
)

fun EventEntity.toDomain() = Event(
    id                  = id,
    userId              = userId,
    recurrenceParentId  = recurrenceParentId,
    title               = title,
    description         = description,
    location            = location,
    startAt             = Instant.ofEpochMilli(startAt),
    endAt               = Instant.ofEpochMilli(endAt),
    isAllDay            = isAllDay,
    color               = color,
    recurrenceRule      = recurrenceRule,
    recurrenceEndAt     = recurrenceEndAt?.let { Instant.ofEpochMilli(it) },
    reminderMinutes     = reminderMinutes,
    isDeleted           = isDeleted,
    deletedAt           = deletedAt?.let { Instant.ofEpochMilli(it) },
    version             = version,
    syncStatus          = syncStatus,
    createdAt           = Instant.ofEpochMilli(createdAt),
    updatedAt           = Instant.ofEpochMilli(updatedAt)
)
