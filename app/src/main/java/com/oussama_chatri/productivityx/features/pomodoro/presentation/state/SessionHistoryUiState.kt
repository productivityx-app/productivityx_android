package com.oussama_chatri.productivityx.features.pomodoro.presentation.state

import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats

data class SessionHistoryUiState(
    val sessions: List<PomodoroSession> = emptyList(),
    val stats: PomodoroStats?           = null,
    val isLoading: Boolean              = false,
    val error: String?                  = null,
    val page: Int                       = 0,
    val hasNextPage: Boolean            = false
)
