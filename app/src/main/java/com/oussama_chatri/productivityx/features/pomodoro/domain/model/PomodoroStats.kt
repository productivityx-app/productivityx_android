package com.oussama_chatri.productivityx.features.pomodoro.domain.model

import java.time.LocalDate

data class PomodoroStats(
    val completedFocusSessionsToday: Long,
    val totalFocusMinutesToday: Long,
    val totalFocusSecondsToday: Long,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val focusQualityScore: Float = 0f, // 0 to 1, based on interruptions
    val weeklyHeatMap: Map<LocalDate, Int> = emptyMap(), // Date to minutes
    val categoryDistribution: Map<String, Int> = emptyMap(), // Category/Tag to minutes
    val dailyGoalMinutes: Int = 120,
    val weeklyGoalMinutes: Int = 600,
    val totalFocusTimeAllTime: Long = 0 // Seconds
)
