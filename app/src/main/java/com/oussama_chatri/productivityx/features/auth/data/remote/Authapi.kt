package com.oussama_chatri.productivityx.features.auth.data.remote

import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.ChangePasswordRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.DeleteAccountRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.ForgotPasswordRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.LoginRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.RegisterRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.ResendVerificationRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.ResetPasswordRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.VerifyForgotPasswordOtpRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.VerifyOtpRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.ApiResponse
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.AuthResponse
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.ForgotPasswordOtpVerifiedResponse
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    /** 201 — no data, just a message "Check your email to verify your account." */
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Void>>

    /**
     * 200 — returns AuthResponse (accessToken, tokenType, expiresIn).
     * Refresh token is set as HttpOnly cookie named "refreshToken".
     * After this, call /auth/me to fetch the full user object.
     */
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    /**
     * 200 — verifies the 6-digit OTP received in the email verification email.
     * Body: { email, otp }
     * Returns AuthResponse + sets refresh cookie. Call /auth/me after.
     */
    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<AuthResponse>>

    /** 200 — sends a new OTP + magic link to the email. No response data. */
    @POST("api/v1/auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequest): Response<ApiResponse<Void>>

    /**
     * 200 — rotates the refresh token cookie and returns a new AuthResponse.
     * The refresh cookie is sent automatically by OkHttp's cookie jar.
     */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(): Response<ApiResponse<AuthResponse>>

    /** 200 — revokes refresh token cookie. Always returns 200 (no body data). */
    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<ApiResponse<Void>>

    /** 200 — always 200 regardless of whether the email exists (no user enumeration). */
    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Void>>

    /**
     * 200 — verifies the 6-digit OTP from the password-reset email.
     * On success returns a short-lived resetToken (15-min TTL) to be passed to /reset-password.
     * On wrong OTP: 401 AUTH_OTP_INVALID.
     * On too many attempts: 429 RATE_FORGOT_OTP_EXCEEDED.
     */
    @POST("api/v1/auth/verify-forgot-otp")
    suspend fun verifyForgotPasswordOtp(
        @Body request: VerifyForgotPasswordOtpRequest
    ): Response<ApiResponse<ForgotPasswordOtpVerifiedResponse>>

    /** 200 — resets password and revokes all refresh tokens for that user. */
    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Void>>

    /** 200 — changes password and revokes all refresh tokens. Requires auth. */
    @POST("api/v1/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Void>>

    /**
     * 200 — flat UserResponse: user + profile + preferences all in one object.
     * Call this after every login / verify-otp to hydrate local storage.
     */
    @GET("api/v1/auth/me")
    suspend fun me(): Response<ApiResponse<UserResponse>>

    /** 200 — permanently deletes the account. Requires auth + password confirmation. */
    @DELETE("api/v1/auth/delete-account")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): Response<ApiResponse<Void>>
}