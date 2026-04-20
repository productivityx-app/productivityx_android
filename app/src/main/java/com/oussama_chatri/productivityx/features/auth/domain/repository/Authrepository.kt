package com.oussama_chatri.productivityx.features.auth.domain.repository

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthResult
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthUser

interface AuthRepository {

    /**
     * Register a new account.
     * username and birthDate are required by the backend.
     * phone and gender are optional.
     */
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,
        birthDate: String,       // "yyyy-MM-dd"
        phone: String? = null,
        gender: String? = null   // "MALE" | "FEMALE"
    ): Resource<String>

    /** Login with email, username, or phone. Returns Unverified if email not confirmed. */
    suspend fun login(identifier: String, password: String): AuthResult

    /** Verify 6-digit OTP received in email. Returns Success + user on valid OTP. */
    suspend fun verifyOtp(email: String, otp: String): AuthResult

    /** Resend verification email with new OTP + magic link. */
    suspend fun resendVerification(email: String): Resource<String>

    /** Trigger forgot-password email. Always returns Success (no user enumeration). */
    suspend fun forgotPassword(email: String): Resource<String>

    /** Reset password using the token from the email link. */
    suspend fun resetPassword(token: String, newPassword: String): Resource<String>

    /** Change password for authenticated user. */
    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<String>

    /** Logout — revokes refresh token cookie server-side, clears local storage. */
    suspend fun logout(): Resource<Unit>

    /** Fetch the full user from /auth/me and persist locally. */
    suspend fun getCurrentUser(): Resource<AuthUser>

    /** Permanently delete account after password confirmation. */
    suspend fun deleteAccount(password: String): Resource<Unit>

    /** True if an access token exists in local storage. */
    fun isLoggedIn(): Boolean

    /** Returns the cached AuthUser from local storage, or null if not yet fetched. */
    fun getCachedUser(): AuthUser?

    /** Verify the 6-digit OTP from the forgot-password email. Returns a short-lived resetToken on success. */
    suspend fun verifyForgotPasswordOtp(email: String, otp: String): Resource<String>
}