package com.oussama_chatri.productivityx.features.pomodoro.domain.model

data class PomodoroStats(
    val completedFocusSessionsToday: Long,
    val totalFocusMinutesToday: Long,
    val totalFocusSecondsToday: Long
)
