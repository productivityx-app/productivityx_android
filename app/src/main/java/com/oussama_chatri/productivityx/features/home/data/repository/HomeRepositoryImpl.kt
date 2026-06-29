package com.oussama_chatri.productivityx.features.home.data.repository

import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.events.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import com.oussama_chatri.productivityx.features.home.domain.model.WidgetType
import com.oussama_chatri.productivityx.features.home.domain.repository.HomeRepository
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.pomodoro.data.remote.PomodoroApi
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val noteDao: NoteDao,
    private val eventDao: EventDao,
    private val pomodoroApi: PomodoroApi,
    private val preferencesDataStore: PreferencesDataStore,
) : HomeRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDashboard(): Flow<DashboardSummary> =
        preferencesDataStore.cachedUserId.flatMapLatest { userId ->
            if (userId.isNullOrBlank()) return@flatMapLatest flowOf(emptyDashboard())

            val nowMs = Instant.now().toEpochMilli()
            val today = LocalDate.now()

            combine(
                taskDao.observeTopLevelTasks(userId),
                noteDao.observeActiveNotes(userId),
                eventDao.observeUpcomingEvents(userId, nowMs, limit = 8),
                fetchStatsFlow(),
                preferencesDataStore.cachedUserFirstName,
            ) { tasks, notes, events, stats, firstName ->

                val activeTasks = tasks.filter { !it.isDeleted }

                val dueToday = activeTasks.filter { task ->
                    task.dueDate == today &&
                            task.status != TaskStatus.DONE &&
                            task.status != TaskStatus.CANCELLED
                }

                val overdueCount = activeTasks.count { task ->
                    task.dueDate != null &&
                            task.dueDate.isBefore(today) &&
                            task.status != TaskStatus.DONE &&
                            task.status != TaskStatus.CANCELLED
                }

                val completedToday = activeTasks.count { task ->
                    task.completedAt?.let {
                        val completedDate = java.time.Instant.ofEpochMilli(it.toEpochMilli())
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        completedDate == today
                    } ?: false
                }

                DashboardSummary(
                    firstName = firstName ?: "",
                    tasksDueToday = dueToday.size,
                    tasksOverdue = overdueCount,
                    totalActiveNotes = notes.size,
                    dueTodayTasks = dueToday.take(5).map { it.toDomain() },
                    upcomingEvents = events.map { it.toDomain() },
                    recentNotes = notes.take(8).map { it.toDomain() },
                    todayFocusMinutes = stats.first,
                    completedSessionsToday = stats.second,
                    tasksCompletedToday = completedToday,
                    widgetOrder = listOf(
                        WidgetType.GREETING,
                        WidgetType.TODAYS_TASKS,
                        WidgetType.UPCOMING_EVENTS,
                        WidgetType.FOCUS_TIME,
                        WidgetType.RECENT_NOTES,
                        WidgetType.DAILY_QUOTE,
                        WidgetType.AI_QUICK_ACTION,
                        WidgetType.FOCUS_MODE_TOGGLE,
                    ),
                    widgetVisibility = WidgetType.entries.associateWith { true },
                    widgetUsageCount = emptyMap(),
                    isFocusMode = false,
                )
            }
        }.catch { emit(emptyDashboard()) }

    private fun fetchStatsFlow(): Flow<Pair<Int, Int>> = flow {
        emit(Pair(0, 0))
        runCatching {
            val response = pomodoroApi.getTodayStats()
            if (response.isSuccessful) {
                val dto = response.body()?.data
                if (dto != null) {
                    emit(
                        Pair(
                            dto.totalFocusMinutesToday.toInt(),
                            dto.completedFocusSessionsToday.toInt(),
                        )
                    )
                }
            }
        }
    }

    private fun emptyDashboard() = DashboardSummary(
        firstName = "",
        tasksDueToday = 0,
        tasksOverdue = 0,
        totalActiveNotes = 0,
        dueTodayTasks = emptyList(),
        upcomingEvents = emptyList(),
        recentNotes = emptyList(),
        todayFocusMinutes = 0,
        completedSessionsToday = 0,
        tasksCompletedToday = 0,
    )
}
