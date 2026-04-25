package com.oussama_chatri.productivityx.features.events.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.oussama_chatri.productivityx.core.enums.SyncStatus

@Entity(
    tableName = "events",
    indices = [
        Index("userId"),
        Index("userId", "isDeleted"),
        Index("userId", "startAt"),
        Index("syncStatus")
    ]
)
data class EventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val recurrenceParentId: String?,
    val title: String,
    val description: String?,
    val location: String?,
    val startAt: Long,
    val endAt: Long,
    val isAllDay: Boolean,
    val color: String,
    val recurrenceRule: String?,
    val recurrenceEndAt: Long?,
    val reminderMinutes: Int?,
    val isDeleted: Boolean,
    val deletedAt: Long?,
    val version: Int,
    val syncStatus: SyncStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val pendingOperation: String?
)
