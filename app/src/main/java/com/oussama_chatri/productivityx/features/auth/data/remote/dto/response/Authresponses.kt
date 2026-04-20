package com.oussama_chatri.productivityx.features.auth.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * Wrapper envelope used by every endpoint.
 * Backend: ApiResponse<T> { success, data, message, errorCode, timestamp }
 */
data class ApiResponse<T>(
    @SerializedName("success")   val success: Boolean,
    @SerializedName("data")      val data: T? = null,
    @SerializedName("message")   val message: String? = null,
    @SerializedName("errorCode") val errorCode: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

/**
 * POST /api/v1/auth/login  →  ApiResponse<AuthResponse>
 * POST /api/v1/auth/verify-otp  →  ApiResponse<AuthResponse>
 * POST /api/v1/auth/refresh  →  ApiResponse<AuthResponse>
 */
data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("tokenType")   val tokenType: String = "Bearer",
    @SerializedName("expiresIn")   val expiresIn: Long   // seconds
)

/**
 * GET /api/v1/auth/me  →  ApiResponse<UserResponse>
 */
data class UserResponse(
    @SerializedName("id")            val id: String,
    @SerializedName("email")         val email: String,
    @SerializedName("username")      val username: String? = null,
    @SerializedName("phone")         val phone: String? = null,
    @SerializedName("emailVerified") val emailVerified: Boolean = false,
    @SerializedName("gender")        val gender: String? = null,
    @SerializedName("birthDate")     val birthDate: String? = null,
    @SerializedName("lastLoginAt")   val lastLoginAt: String? = null,
    @SerializedName("createdAt")     val createdAt: String? = null,

    // Profile
    @SerializedName("firstName")  val firstName: String,
    @SerializedName("lastName")   val lastName: String,
    @SerializedName("avatarUrl")  val avatarUrl: String? = null,
    @SerializedName("bio")        val bio: String? = null,
    @SerializedName("timezone")   val timezone: String? = null,
    @SerializedName("language")   val language: String? = null,
    @SerializedName("theme")      val theme: String? = null,

    // Preferences snapshot
    @SerializedName("pomodoroFocusMinutes")         val pomodoroFocusMinutes: Int = 25,
    @SerializedName("pomodoroShortBreakMinutes")    val pomodoroShortBreakMinutes: Int = 5,
    @SerializedName("pomodoroLongBreakMinutes")     val pomodoroLongBreakMinutes: Int = 15,
    @SerializedName("pomodoroCyclesBeforeLongBreak") val pomodoroCyclesBeforeLongBreak: Int = 4,
    @SerializedName("pomodoroAutoStartBreaks")      val pomodoroAutoStartBreaks: Boolean = false,
    @SerializedName("pomodoroAutoStartFocus")       val pomodoroAutoStartFocus: Boolean = false,
    @SerializedName("pomodoroSoundEnabled")         val pomodoroSoundEnabled: Boolean = true,
    @SerializedName("notifyTaskReminders")          val notifyTaskReminders: Boolean = true,
    @SerializedName("notifyEventReminders")         val notifyEventReminders: Boolean = true,
    @SerializedName("notifyPomodoroEnd")            val notifyPomodoroEnd: Boolean = true,
    @SerializedName("notifyDailySummary")           val notifyDailySummary: Boolean = false,
    @SerializedName("defaultTaskView")              val defaultTaskView: String = "LIST",
    @SerializedName("defaultTaskSort")              val defaultTaskSort: String = "DUE_DATE",
    @SerializedName("showCompletedTasks")           val showCompletedTasks: Boolean = false,
    @SerializedName("defaultCalendarView")          val defaultCalendarView: String = "MONTH",
    @SerializedName("weekStartsOn")                 val weekStartsOn: String = "MON",
    @SerializedName("aiContextEnabled")             val aiContextEnabled: Boolean = true,
    @SerializedName("aiModel")                      val aiModel: String = "gemini-2.0-flash",
    @SerializedName("compactMode")                  val compactMode: Boolean = false
)

data class ForgotPasswordOtpVerifiedResponse(
    @SerializedName("resetToken") val resetToken: String
)
