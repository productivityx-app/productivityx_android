package com.oussama_chatri.productivityx.core.db

import androidx.room.TypeConverter
import com.oussama_chatri.productivityx.core.enums.AppTheme
import com.oussama_chatri.productivityx.core.enums.EntityType
import com.oussama_chatri.productivityx.core.enums.Gender
import com.oussama_chatri.productivityx.core.enums.MessageRole
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.SyncOperation
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class Converters {

    @TypeConverter fun fromUuid(value: UUID?): String? = value?.toString()
    @TypeConverter fun toUuid(value: String?): UUID? = value?.let { UUID.fromString(it) }

    @TypeConverter fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter fun fromLocalDate(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter fun fromLocalTime(value: LocalTime?): String? = value?.toString()
    @TypeConverter fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter fun fromSyncStatus(value: SyncStatus?): String? = value?.name
    @TypeConverter fun toSyncStatus(value: String?): SyncStatus? = value?.let { SyncStatus.valueOf(it) }

    @TypeConverter fun fromSyncOperation(value: SyncOperation?): String? = value?.name
    @TypeConverter fun toSyncOperation(value: String?): SyncOperation? = value?.let { SyncOperation.valueOf(it) }

    @TypeConverter fun fromEntityType(value: EntityType?): String? = value?.name
    @TypeConverter fun toEntityType(value: String?): EntityType? = value?.let { EntityType.valueOf(it) }

    @TypeConverter fun fromPriority(value: Priority?): String? = value?.name
    @TypeConverter fun toPriority(value: String?): Priority? = value?.let { Priority.valueOf(it) }

    @TypeConverter fun fromTaskStatus(value: TaskStatus?): String? = value?.name
    @TypeConverter fun toTaskStatus(value: String?): TaskStatus? = value?.let { TaskStatus.valueOf(it) }

    @TypeConverter fun fromPomodoroType(value: PomodoroType?): String? = value?.name
    @TypeConverter fun toPomodoroType(value: String?): PomodoroType? = value?.let { PomodoroType.valueOf(it) }

    @TypeConverter fun fromMessageRole(value: MessageRole?): String? = value?.name
    @TypeConverter fun toMessageRole(value: String?): MessageRole? = value?.let { MessageRole.valueOf(it) }

    @TypeConverter fun fromAppTheme(value: AppTheme?): String? = value?.name
    @TypeConverter fun toAppTheme(value: String?): AppTheme? = value?.let { AppTheme.valueOf(it) }

    @TypeConverter fun fromGender(value: Gender?): String? = value?.name
    @TypeConverter fun toGender(value: String?): Gender? = value?.let { Gender.valueOf(it) }

    @TypeConverter fun fromStringList(value: List<String>?): String? = value?.joinToString("|")
    @TypeConverter fun toStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList() else value.split("|")
}