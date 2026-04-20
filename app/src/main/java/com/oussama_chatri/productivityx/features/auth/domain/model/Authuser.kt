package com.oussama_chatri.productivityx.features.auth.domain.model

data class AuthUser(
    val id: String,
    val email: String,
    val username: String?,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String?,
    val theme: String?,
    val isEmailVerified: Boolean,
    val pomodoroFocusMinutes: Int,
    val pomodoroShortBreakMinutes: Int,
    val pomodoroLongBreakMinutes: Int
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val initials: String get() {
        val parts = fullName.split(" ").filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
            parts.size == 1 -> parts.first().take(2).uppercase()
            else -> "?"
        }
    }
}