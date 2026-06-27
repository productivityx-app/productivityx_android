package com.oussama_chatri.productivityx.features.auth.presentation.event

sealed class LoginUiEvent {
    data class IdentifierChanged(val value: String) : LoginUiEvent()
    data class PasswordChanged(val value: String) : LoginUiEvent()
    data object TogglePasswordVisibility : LoginUiEvent()
    data object Submit : LoginUiEvent()
    data object SkipLogin : LoginUiEvent()
}

sealed class RegisterUiEvent {
    data object NextStep : RegisterUiEvent()
    data object PrevStep : RegisterUiEvent()

    // Step 0
    data class EmailChanged(val value: String) : RegisterUiEvent()
    data class PasswordChanged(val value: String) : RegisterUiEvent()
    data class ConfirmPasswordChanged(val value: String) : RegisterUiEvent()
    data object TogglePasswordVisibility : RegisterUiEvent()
    data object ToggleConfirmPasswordVisibility : RegisterUiEvent()

    // Step 1
    data class FirstNameChanged(val value: String) : RegisterUiEvent()
    data class LastNameChanged(val value: String) : RegisterUiEvent()
    data class UsernameChanged(val value: String) : RegisterUiEvent()
    data class PhoneChanged(val value: String) : RegisterUiEvent()
    data class BirthDateChanged(val value: String) : RegisterUiEvent()   // "yyyy-MM-dd"

    // Step 2
    data class ThemeSelected(val theme: String) : RegisterUiEvent()

    // Step 3
    data class OtpChanged(val value: String) : RegisterUiEvent()
    data object ResendOtp : RegisterUiEvent()
    data object VerifyOtp : RegisterUiEvent()
}

sealed class VerifyEmailUiEvent {
    data class EmailChanged(val value: String) : VerifyEmailUiEvent()
    data class OtpChanged(val value: String) : VerifyEmailUiEvent()
    data object Verify : VerifyEmailUiEvent()
    data object Resend : VerifyEmailUiEvent()
}

sealed class ForgotPasswordUiEvent {
    data class EmailChanged(val value: String) : ForgotPasswordUiEvent()
    data object Submit : ForgotPasswordUiEvent()

    data class OtpChanged(val value: String) : ForgotPasswordUiEvent()
    data object VerifyOtp : ForgotPasswordUiEvent()
    data object ResendOtp : ForgotPasswordUiEvent()

    data object GoBackToEmail : ForgotPasswordUiEvent()
}

sealed class ResetPasswordUiEvent {
    data class TokenChanged(val value: String) : ResetPasswordUiEvent()
    data class NewPasswordChanged(val value: String) : ResetPasswordUiEvent()
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordUiEvent()
    data object TogglePasswordVisibility : ResetPasswordUiEvent()
    data object ToggleConfirmPasswordVisibility : ResetPasswordUiEvent()
    data object Submit : ResetPasswordUiEvent()
}