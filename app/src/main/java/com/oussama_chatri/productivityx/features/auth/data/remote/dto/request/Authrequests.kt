package com.oussama_chatri.productivityx.features.auth.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * POST /api/v1/auth/register
 * Required: email, username, password, firstName, lastName, birthDate (ISO date string)
 * Optional: phone, gender
 */
data class RegisterRequest(
    @SerializedName("email")      val email: String,
    @SerializedName("username")   val username: String,
    @SerializedName("password")   val password: String,
    @SerializedName("firstName")  val firstName: String,
    @SerializedName("lastName")   val lastName: String,
    @SerializedName("birthDate")  val birthDate: String,        // "yyyy-MM-dd"
    @SerializedName("phone")      val phone: String? = null,
    @SerializedName("gender")     val gender: String? = null    // "MALE" | "FEMALE"
)

/**
 * POST /api/v1/auth/login
 * identifier = email, username, or phone (backend resolves all three)
 */
data class LoginRequest(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("password")   val password: String
)

/** POST /api/v1/auth/verify-otp */
data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp")   val otp: String
)

/** POST /api/v1/auth/resend-verification */
data class ResendVerificationRequest(
    @SerializedName("email") val email: String
)

/** POST /api/v1/auth/forgot-password */
data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

/** POST /api/v1/auth/reset-password */
data class ResetPasswordRequest(
    @SerializedName("token")       val token: String,
    @SerializedName("newPassword") val newPassword: String
)

/** POST /api/v1/auth/change-password */
data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword")     val newPassword: String
)

/** DELETE /api/v1/auth/delete-account */
data class DeleteAccountRequest(
    @SerializedName("password") val password: String
)

data class VerifyForgotPasswordOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp")   val otp: String
)