package com.oussama_chatri.productivityx.features.pomodoro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import java.time.Instant

@Entity(tableName = "pomodoro_sessions_local")
data class PomodoroSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "task_id")
    val taskId: String?,

    @ColumnInfo(name = "task_title")
    val taskTitle: String?,

    @ColumnInfo(name = "type")
    val type: PomodoroType,

    @ColumnInfo(name = "planned_duration_seconds")
    val plannedDurationSeconds: Int,

    @ColumnInfo(name = "actual_duration_seconds")
    val actualDurationSeconds: Int?,

    @ColumnInfo(name = "completed")
    val completed: Boolean = false,

    @ColumnInfo(name = "interrupted")
    val interrupted: Boolean = false,

    @ColumnInfo(name = "interrupt_reason")
    val interruptReason: String? = null,

    // epoch millis
    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @ColumnInfo(name = "ended_at")
    val endedAt: Long?,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    @ColumnInfo(name = "pending_operation")
    val pendingOperation: String? = null,
) {
    fun toDomain(): PomodoroSession = PomodoroSession(
        id = id,
        userId = userId,
        taskId = taskId,
        taskTitle = taskTitle,
        type = type,
        plannedDurationSeconds = plannedDurationSeconds,
        actualDurationSeconds = actualDurationSeconds,
        interrupted = interrupted,
        interruptReason = interruptReason,
        focusMinutesSetting = if (type == PomodoroType.FOCUS) plannedDurationSeconds / 60 else 25,
        shortBreakMinutesSetting = 5,
        longBreakMinutesSetting = 15,
        startedAt = Instant.ofEpochMilli(startedAt),
        endedAt = endedAt?.let { Instant.ofEpochMilli(it) },
        completed = completed,
        actualMinutes = actualDurationSeconds?.div(60),
        createdAt = Instant.ofEpochMilli(startedAt),
    )
}
