package com.oussama_chatri.productivityx.features.pomodoro.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import javax.inject.Inject

class GetSessionsUseCase @Inject constructor(
    private val repository: PomodoroRepository
) {
    suspend operator fun invoke(
        page: Int = 0,
        size: Int = 20,
        taskId: String? = null
    ): Resource<List<PomodoroSession>> = repository.getSessions(page, size, taskId)
}
