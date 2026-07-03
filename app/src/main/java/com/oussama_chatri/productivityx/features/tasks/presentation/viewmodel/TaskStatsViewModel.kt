package com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.enums.Badge
import com.oussama_chatri.productivityx.core.enums.BadgeType
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.domain.usecase.ObserveTasksUseCase
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskStatsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class TaskStatsViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskStatsUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadStats()
    }

    private fun loadStats() {
        observeTasks()
            .map { tasks -> computeStats(tasks) }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            }
            .onEach { stats ->
                _uiState.value = stats.copy(isRefreshing = false)
            }
            .launchIn(viewModelScope)
    }

    private fun computeStats(tasks: List<Task>): TaskStatsUiState {
        val now = LocalDate.now()
        val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val startOfMonth = now.withDayOfMonth(1)

        val activeTasks = tasks.filter { !it.isDeleted && it.status != TaskStatus.CANCELLED }
        val completedTasks = tasks.filter { it.status == TaskStatus.DONE }

        // Weekly counts
        val weekTasks = activeTasks.filter { it.dueDate != null && !it.dueDate.isBefore(startOfWeek) && !it.dueDate.isAfter(now) }
        val weekCompleted = completedTasks.filter { t -> t.dueDate?.let { !it.isBefore(startOfWeek) && !it.isAfter(now) } == true }
        val weekTotalCount = weekTasks.size + weekCompleted.size
        val weekCompletedCount = weekCompleted.size

        // Monthly counts
        val monthTasks = activeTasks.filter { it.dueDate != null && !it.dueDate.isBefore(startOfMonth) && !it.dueDate.isAfter(now) }
        val monthCompleted = completedTasks.filter { t -> t.dueDate?.let { !it.isBefore(startOfMonth) && !it.isAfter(now) } == true }
        val monthTotalCount = monthTasks.size + monthCompleted.size
        val monthCompletedCount = monthCompleted.size

        // Completion rates
        val weeklyCompletionRate = if (weekTotalCount > 0) weekCompletedCount.toFloat() / weekTotalCount else 0f
        val monthlyCompletionRate = if (monthTotalCount > 0) monthCompletedCount.toFloat() / monthTotalCount else 0f

        // Streak calculations
        val completionDates = completedTasks
            .mapNotNull { it.dueDate }
            .distinct()
            .sorted()
        val daysWithTasks = completionDates

        val currentStreak = computeCurrentStreak(completionDates, now)
        val longestStreak = computeLongestStreak(completionDates)

        // Productivity score
        val productivityScore = if (tasks.isNotEmpty()) {
            (completedTasks.size.toFloat() / (activeTasks.size + completedTasks.size).coerceAtLeast(1)) * 100f
        } else 0f

        // Time per priority
        val timePerPriority = Priority.entries.associateWith { p ->
            tasks.count { it.priority == p }
        }

        // Week created/completed/overdue
        val weekCreatedCount = tasks.count {
            !it.isDeleted && it.dueDate != null && !it.dueDate.isBefore(startOfWeek) && !it.dueDate.isAfter(now)
        }
        val weekOverdueCount = activeTasks.count { it.dueDate != null && it.dueDate.isBefore(now) }

        // Category breakdown
        val categoryBreakdown = Priority.entries.associateWith { p ->
            val count = tasks.count { it.priority == p && !it.isDeleted }
            if (tasks.isNotEmpty()) count.toFloat() / tasks.size else 0f
        }

        // Badges
        val badges = computeBadges(completedTasks.size, currentStreak, longestStreak)

        // Completion velocity (last 7 days)
        val completionVelocity = (0..6).map { daysAgo ->
            val date = now.minusDays(daysAgo.toLong())
            val count = completedTasks.count { it.dueDate == date }
            date to count
        }.reversed()

        return TaskStatsUiState(
            isLoading = false,
            weeklyCompletionRate = weeklyCompletionRate,
            monthlyCompletionRate = monthlyCompletionRate,
            weeklyCompletedCount = weekCompletedCount,
            weeklyTotalCount = weekTotalCount,
            monthlyCompletedCount = monthCompletedCount,
            monthlyTotalCount = monthTotalCount,
            productivityScore = productivityScore,
            productivityTrend = weeklyCompletionRate - monthlyCompletionRate,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            daysWithTasks = daysWithTasks,
            timePerPriority = timePerPriority,
            badges = badges,
            weekCreatedCount = weekCreatedCount,
            weekCompletedCount = weekCompletedCount,
            weekOverdueCount = weekOverdueCount,
            weekSuggestedFocus = suggestFocus(weekOverdueCount, weekCompletedCount, weekTotalCount),
            categoryBreakdown = categoryBreakdown,
            completionVelocity = completionVelocity
        )
    }

    private fun computeCurrentStreak(completionDates: List<LocalDate>, today: LocalDate): Int {
        if (completionDates.isEmpty()) return 0
        var streak = 0
        var current = today
        while (completionDates.contains(current)) {
            streak++
            current = current.minusDays(1)
        }
        return streak
    }

    private fun computeLongestStreak(completionDates: List<LocalDate>): Int {
        if (completionDates.isEmpty()) return 0
        var longest = 0
        var current = 1
        for (i in 1..completionDates.lastIndex) {
            if (completionDates[i] == completionDates[i - 1].plusDays(1)) {
                current++
            } else {
                longest = maxOf(longest, current)
                current = 1
            }
        }
        return maxOf(longest, current)
    }

    private fun computeBadges(totalCompleted: Int, currentStreak: Int, longestStreak: Int): List<Badge> {
        val badges = mutableListOf<Badge>()
        if (totalCompleted >= 1) badges.add(Badge(BadgeType.FIRST_TASK, "First Task", "Completed your first task", "first_task"))
        if (totalCompleted >= 10) badges.add(Badge(BadgeType.TEN_TASKS, "10 Tasks", "Completed 10 tasks", "ten_tasks"))
        if (totalCompleted >= 50) badges.add(Badge(BadgeType.FIFTY_TASKS, "50 Tasks", "Completed 50 tasks", "fifty_tasks"))
        if (totalCompleted >= 100) badges.add(Badge(BadgeType.HUNDRED_TASKS, "100 Tasks", "Completed 100 tasks", "hundred_tasks"))
        if (currentStreak >= 7 || longestStreak >= 7) badges.add(Badge(BadgeType.WEEK_STREAK, "Week Streak", "7-day streak", "week_streak"))
        if (currentStreak >= 14 || longestStreak >= 14) badges.add(Badge(BadgeType.TWO_WEEK_STREAK, "Two Week Streak", "14-day streak", "two_week_streak"))
        if (currentStreak >= 30 || longestStreak >= 30) badges.add(Badge(BadgeType.MONTH_STREAK, "Month Streak", "30-day streak", "month_streak"))
        return badges
    }

    private fun suggestFocus(overdue: Int, completed: Int, total: Int): String {
        return when {
            overdue > 5 -> "Focus on clearing overdue tasks"
            completed == 0 && total > 0 -> "Try to complete at least one task"
            total == 0 -> "Add some tasks to get started"
            else -> "Keep up the good work!"
        }
    }
}
