package com.oussama_chatri.productivityx.features.pomodoro.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import javax.inject.Inject

class EndSessionUseCase @Inject constructor(
    private val repository: PomodoroRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        actualDurationSeconds: Int? = null
    ): Resource<PomodoroSession> = repository.endSession(sessionId, actualDurationSeconds)
}
