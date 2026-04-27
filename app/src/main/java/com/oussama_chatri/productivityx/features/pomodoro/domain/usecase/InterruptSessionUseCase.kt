package com.oussama_chatri.productivityx.features.pomodoro.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import javax.inject.Inject

class InterruptSessionUseCase @Inject constructor(
    private val repository: PomodoroRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        actualDurationSeconds: Int? = null,
        reason: String? = null
    ): Resource<PomodoroSession> = repository.interruptSession(sessionId, actualDurationSeconds, reason)
}
