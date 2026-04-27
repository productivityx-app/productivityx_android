package com.oussama_chatri.productivityx.features.pomodoro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oussama_chatri.productivityx.core.enums.PomodoroType

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

    // epoch millis
    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @ColumnInfo(name = "ended_at")
    val endedAt: Long?,
)