package com.oussama_chatri.productivityx.features.auth.data.mapper

import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.AuthResponse
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.UserResponse
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthToken
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthUser

fun AuthResponse.toToken() = AuthToken(
    accessToken = accessToken,
    tokenType = tokenType,
    expiresInSeconds = expiresIn
)

fun UserResponse.toDomain() = AuthUser(
    id = id,
    email = email,
    username = username,
    firstName = firstName,
    lastName = lastName,
    avatarUrl = avatarUrl,
    theme = theme,
    isEmailVerified = emailVerified,
    pomodoroFocusMinutes = pomodoroFocusMinutes,
    pomodoroShortBreakMinutes = pomodoroShortBreakMinutes,
    pomodoroLongBreakMinutes = pomodoroLongBreakMinutes
)