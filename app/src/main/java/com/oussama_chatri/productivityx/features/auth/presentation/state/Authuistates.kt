package com.oussama_chatri.productivityx.features.auth.presentation.state

// Splash — decides initial destination
sealed class SplashUiState {
    data object Checking : SplashUiState()
    data object Authenticated : SplashUiState()
    data object ShowOnboarding : SplashUiState()
    data object ShowLogin : SplashUiState()
}

// Login form state
data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val identifierError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

// Register form — 4 steps
data class RegisterUiState(
    val currentStep: Int = 0,

    // Step 0 — Account
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,

    // Step 1 — Profile
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val phone: String = "",
    val birthDate: String = "",         // "yyyy-MM-dd" — required by backend

    // Step 2 — Preferences
    val selectedTheme: String = "DARK",

    // Step 3 — Verify OTP
    val pendingEmail: String = "",
    val otp: String = "",
    val resendCooldownSeconds: Int = 0,
    val isResending: Boolean = false,

    // Validation errors
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val birthDateError: String? = null,

    // Loading / result
    val isLoading: Boolean = false,
    val generalError: String? = null
)

// Verify email screen (standalone deep-link path)
data class VerifyEmailUiState(
    val email: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val isResending: Boolean = false,
    val error: String? = null
)

// Forgot password screen
data class ForgotPasswordUiState(
    // Step 0 — email entry
    val email: String = "",
    val emailError: String? = null,

    // Step 1 — OTP entry (shown after successful email submission)
    val showOtpStep: Boolean = false,
    val otp: String = "",

    // Holds the short-lived reset token returned by the server after a correct OTP.
    // Populated just before navigating to ResetPasswordScreen — never shown in UI.
    val resetToken: String = "",

    val resendCooldownSeconds: Int = 0,
    val isResending: Boolean = false,

    val isLoading: Boolean = false,
    val error: String? = null
)

// Reset password screen
data class ResetPasswordUiState(
    val token: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)