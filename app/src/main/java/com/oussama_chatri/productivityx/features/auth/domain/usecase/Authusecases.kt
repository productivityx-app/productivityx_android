package com.oussama_chatri.productivityx.features.auth.domain.usecase

import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthResult
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthUser
import com.oussama_chatri.productivityx.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,           // non-null — ViewModel defaults to email prefix if blank
        birthDate: String,          // non-null — "yyyy-MM-dd", validated in ViewModel
        phone: String? = null,
        gender: String? = null
    ): Resource<String> = repository.register(
        email = email,
        password = password,
        firstName = firstName,
        lastName = lastName,
        username = username,
        birthDate = birthDate,
        phone = phone,
        gender = gender
    )
}
class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(identifier: String, password: String): AuthResult =
        repository.login(identifier, password)
}

class VerifyOtpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, otp: String): AuthResult =
        repository.verifyOtp(email, otp)
}

class ResendVerificationUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Resource<String> =
        repository.resendVerification(email)
}

class ForgotPasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Resource<String> =
        repository.forgotPassword(email)
}

class ResetPasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String, newPassword: String): Resource<String> =
        repository.resetPassword(token, newPassword)
}

class ChangePasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Resource<String> =
        repository.changePassword(currentPassword, newPassword)
}

class LogoutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<Unit> = repository.logout()
}

class GetCurrentUserUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<AuthUser> = repository.getCurrentUser()
}

class DeleteAccountUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(password: String): Resource<Unit> =
        repository.deleteAccount(password)
}

/** Synchronous — reads from in-memory / encrypted prefs, no network. */
class GetCachedUserUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): AuthUser? = repository.getCachedUser()
}

/** Used by SplashViewModel to decide whether to go to login or home. */
class IsLoggedInUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): Boolean = repository.isLoggedIn()
}

class RefreshTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun isLoggedIn(): Boolean = repository.isLoggedIn()
}

class VerifyForgotPasswordOtpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String): Resource<String> =
        repository.verifyForgotPasswordOtp(email, otp)
}