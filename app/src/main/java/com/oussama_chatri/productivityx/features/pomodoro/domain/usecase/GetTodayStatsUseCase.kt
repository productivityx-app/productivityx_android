package com.oussama_chatri.productivityx.features.pomodoro.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import javax.inject.Inject

class GetTodayStatsUseCase @Inject constructor(
    private val repository: PomodoroRepository
) {
    suspend operator fun invoke(): Resource<PomodoroStats> = repository.getTodayStats()
}
