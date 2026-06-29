package com.oussama_chatri.productivityx.features.home.data.repository

import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.events.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import com.oussama_chatri.productivityx.features.home.domain.model.WidgetType
import com.oussama_chatri.productivityx.features.home.domain.repository.HomeRepository
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val noteDao: NoteDao,
    private val eventDao: EventDao,
    private val pomodoroRepository: PomodoroRepository,
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
                pomodoroRepository.observeSessions(),
                preferencesDataStore.cachedUserFirstName,
            ) { tasks, notes, events, sessions, firstName ->

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

                // Compute today's pomodoro stats from local sessions
                val zoneId = ZoneId.systemDefault()
                val todayStart = today.atStartOfDay(zoneId).toInstant()
                val todayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant()
                val todaySessions = sessions.filter { s ->
                    s.startedAt in todayStart..todayEnd
                }
                val focusSessionsToday = todaySessions.filter { it.type == PomodoroType.FOCUS }
                val totalFocusSeconds = focusSessionsToday.sumOf { it.actualDurationSeconds?.toLong() ?: 0L }
                val totalFocusMinutes = (totalFocusSeconds / 60).toInt()
                val completedSessionsToday = focusSessionsToday.count { it.completed }

                val dailyGoal = preferencesDataStore.pomodoroDailyGoal.first()

                DashboardSummary(
                    firstName = firstName ?: "",
                    tasksDueToday = dueToday.size,
                    tasksOverdue = overdueCount,
                    totalActiveNotes = notes.size,
                    dueTodayTasks = dueToday.take(5).map { it.toDomain() },
                    upcomingEvents = events.map { it.toDomain() },
                    recentNotes = notes.take(8).map { it.toDomain() },
                    todayFocusMinutes = totalFocusMinutes,
                    completedSessionsToday = completedSessionsToday,
                    tasksCompletedToday = completedToday,
                    totalEstimatedFocusMinutes = dailyGoal,
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
