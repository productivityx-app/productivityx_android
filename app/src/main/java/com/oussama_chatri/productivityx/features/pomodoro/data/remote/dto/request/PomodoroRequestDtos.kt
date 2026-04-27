package com.oussama_chatri.productivityx.features.pomodoro.data.remote.dto.request

import com.oussama_chatri.productivityx.core.enums.PomodoroType

data class StartSessionRequestDto(
    val type: PomodoroType,
    val taskId: String? = null
)

data class EndSessionRequestDto(
    val actualDurationSeconds: Int? = null
)

data class InterruptSessionRequestDto(
    val actualDurationSeconds: Int? = null,
    val interruptReason: String? = null
)
