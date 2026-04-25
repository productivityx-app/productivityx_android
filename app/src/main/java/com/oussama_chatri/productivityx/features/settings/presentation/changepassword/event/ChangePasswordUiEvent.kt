package com.oussama_chatri.productivityx.features.profile.presentation.changepassword.event

sealed class ChangePasswordUiEvent {
    data class CurrentPasswordChanged(val value: String) : ChangePasswordUiEvent()
    data class NewPasswordChanged(val value: String) : ChangePasswordUiEvent()
    data class ConfirmPasswordChanged(val value: String) : ChangePasswordUiEvent()
    data object ToggleCurrentPasswordVisibility : ChangePasswordUiEvent()
    data object ToggleNewPasswordVisibility : ChangePasswordUiEvent()
    data object ToggleConfirmPasswordVisibility : ChangePasswordUiEvent()
    data object SaveClicked : ChangePasswordUiEvent()
    data object DismissError : ChangePasswordUiEvent()
}
