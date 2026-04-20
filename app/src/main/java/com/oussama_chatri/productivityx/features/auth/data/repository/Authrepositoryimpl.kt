package com.oussama_chatri.productivityx.features.auth.data.repository

import com.oussama_chatri.productivityx.core.network.safeApiCall
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.auth.data.local.AuthLocalDataSource
import com.oussama_chatri.productivityx.features.auth.data.remote.AuthApi
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.ChangePasswordRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.DeleteAccountRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.ForgotPasswordRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.LoginRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.RegisterRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.ResendVerificationRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.ResetPasswordRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.VerifyForgotPasswordOtpRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.request.VerifyOtpRequest
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.AuthResponse
import com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.UserResponse
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthResult
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthToken
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthUser
import com.oussama_chatri.productivityx.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    // Register

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,
        birthDate: String,
        phone: String?,
        gender: String?
    ): Resource<String> {
        val result = safeApiCall {
            api.register(
                RegisterRequest(
                    email = email,
                    username = username,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    birthDate = birthDate,
                    phone = phone,
                    gender = gender
                )
            )
        }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    Resource.Success(
                        response.body()?.message ?: "Check your email to verify your account."
                    )
                } else {
                    val error = parseError(response.errorBody()?.string())
                    Resource.Error(error.first, error.second)
                }
            }
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
        }
    }

    // Login

    override suspend fun login(identifier: String, password: String): AuthResult {
        return try {
            val result = safeApiCall { api.login(LoginRequest(identifier, password)) }
            when (result) {
                is Resource.Success -> {
                    val response = result.data
                    if (response.isSuccessful) {
                        val authData = response.body()?.data
                            ?: return AuthResult.Error("Invalid response from server")

                        val token = authData.toToken()
                        localDataSource.saveToken(token)

                        val userResult = fetchAndCacheUser()
                        if (userResult != null) {
                            AuthResult.Success(token, userResult)
                        } else {
                            AuthResult.Error("Failed to load user profile after login.")
                        }
                    } else {
                        val code = response.code()
                        val error = parseError(response.errorBody()?.string())
                        if (code == 403 && error.second == "AUTH_002") {
                            AuthResult.Unverified(identifier)
                        } else {
                            AuthResult.Error(error.first, error.second)
                        }
                    }
                }
                is Resource.Error -> AuthResult.Error(result.message, result.code)
                is Resource.Loading -> AuthResult.Error("Unexpected state")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed.")
        }
    }

    // Verify OTP (6-digit code from email verification)

    override suspend fun verifyOtp(email: String, otp: String): AuthResult {
        return try {
            val result = safeApiCall { api.verifyOtp(VerifyOtpRequest(email, otp)) }
            handleAuthResponse(result)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "OTP verification failed.")
        }
    }

    // Resend verification

    override suspend fun resendVerification(email: String): Resource<String> {
        val result = safeApiCall { api.resendVerification(ResendVerificationRequest(email)) }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    Resource.Success(response.body()?.message ?: "Verification email sent.")
                } else {
                    val error = parseError(response.errorBody()?.string())
                    Resource.Error(error.first, error.second)
                }
            }
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
        }
    }

    // Forgot password

    override suspend fun forgotPassword(email: String): Resource<String> {
        val result = safeApiCall { api.forgotPassword(ForgotPasswordRequest(email)) }
        return when (result) {
            is Resource.Success -> Resource.Success(
                result.data.body()?.message ?: "If that email exists, a reset link has been sent."
            )
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
        }
    }

    // Verify forgot-password OTP → returns short-lived resetToken

    override suspend fun verifyForgotPasswordOtp(email: String, otp: String): Resource<String> {
        val result = safeApiCall {
            api.verifyForgotPasswordOtp(VerifyForgotPasswordOtpRequest(email, otp))
        }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val resetToken = response.body()?.data?.resetToken
                        ?: return Resource.Error("Invalid response from server.")
                    Resource.Success(resetToken)
                } else {
                    val error = parseError(response.errorBody()?.string())
                    Resource.Error(error.first, error.second)
                }
            }
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
        }
    }

    // Reset password

    override suspend fun resetPassword(token: String, newPassword: String): Resource<String> {
        val result = safeApiCall { api.resetPassword(ResetPasswordRequest(token, newPassword)) }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    Resource.Success(response.body()?.message ?: "Password reset successful.")
                } else {
                    val error = parseError(response.errorBody()?.string())
                    Resource.Error(error.first, error.second)
                }
            }
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
        }
    }

    // Change password

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Resource<String> {
        val result = safeApiCall {
            api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
        }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    Resource.Success(response.body()?.message ?: "Password changed successfully.")
                } else {
                    val error = parseError(response.errorBody()?.string())
                    Resource.Error(error.first, error.second)
                }
            }
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
        }
    }

    // Logout — always clears local storage regardless of network outcome

    override suspend fun logout(): Resource<Unit> {
        runCatching { safeApiCall { api.logout() } }
        localDataSource.clearAll()
        return Resource.Success(Unit)
    }

    // Get current user

    override suspend fun getCurrentUser(): Resource<AuthUser> {
        val result = safeApiCall { api.me() }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val userResponse = response.body()?.data
                    if (userResponse != null) {
                        localDataSource.saveUserResponse(userResponse)
                        val user = userResponse.toDomain()
                        localDataSource.cacheUserForDataStore(user)
                        Resource.Success(user)
                    } else {
                        Resource.Error("Empty user response.")
                    }
                } else {
                    val error = parseError(response.errorBody()?.string())
                    Resource.Error(error.first, error.second)
                }
            }
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
        }
    }

    // Delete account

    override suspend fun deleteAccount(password: String): Resource<Unit> {
        val result = safeApiCall { api.deleteAccount(DeleteAccountRequest(password)) }
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    localDataSource.clearAll()
                    Resource.Success(Unit)
                } else {
                    val error = parseError(response.errorBody()?.string())
                    Resource.Error(error.first, error.second)
                }
            }
            is Resource.Error -> result
            is Resource.Loading -> Resource.Loading
        }
    }

    // State queries

    override fun isLoggedIn(): Boolean = localDataSource.isLoggedIn()

    override fun getCachedUser(): AuthUser? =
        localDataSource.getCachedUserResponse()?.toDomain()

    // Private helpers

    private suspend fun handleAuthResponse(
        result: Resource<retrofit2.Response<com.oussama_chatri.productivityx.features.auth.data.remote.dto.response.ApiResponse<AuthResponse>>>
    ): AuthResult {
        return when (result) {
            is Resource.Success -> {
                val response = result.data
                if (response.isSuccessful) {
                    val authData = response.body()?.data
                        ?: return AuthResult.Error("Empty response from server.")

                    val token = authData.toToken()
                    localDataSource.saveToken(token)

                    val user = fetchAndCacheUser()
                    if (user != null) {
                        AuthResult.Success(token, user)
                    } else {
                        AuthResult.Error("Failed to load user profile.")
                    }
                } else {
                    val error = parseError(response.errorBody()?.string())
                    AuthResult.Error(error.first, error.second)
                }
            }
            is Resource.Error -> AuthResult.Error(result.message, result.code)
            is Resource.Loading -> AuthResult.Error("Unexpected state.")
        }
    }

    private suspend fun fetchAndCacheUser(): AuthUser? {
        return try {
            val meResult = safeApiCall { api.me() }
            if (meResult is Resource.Success && meResult.data.isSuccessful) {
                val userResponse = meResult.data.body()?.data ?: return null
                localDataSource.saveUserResponse(userResponse)
                val user = userResponse.toDomain()
                localDataSource.cacheUserForDataStore(user)
                user
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseError(errorBody: String?): Pair<String, String?> {
        if (errorBody.isNullOrBlank()) return Pair("Something went wrong.", null)
        return try {
            val message = Regex("\"message\":\"([^\"]+)\"").find(errorBody)?.groupValues?.get(1)
            val code = Regex("\"errorCode\":\"([^\"]+)\"").find(errorBody)?.groupValues?.get(1)
            Pair(message ?: "Something went wrong.", code)
        } catch (e: Exception) {
            Pair("Something went wrong.", null)
        }
    }
}

// Extension functions (file-level, outside the class — this is where they belong)

private fun AuthResponse.toToken() = AuthToken(
    accessToken = accessToken,
    tokenType = tokenType,
    expiresInSeconds = expiresIn
)

private fun UserResponse.toDomain() = AuthUser(
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