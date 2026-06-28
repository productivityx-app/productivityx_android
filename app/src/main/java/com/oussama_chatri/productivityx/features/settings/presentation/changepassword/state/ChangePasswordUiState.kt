package com.oussama_chatri.productivityx.features.settings.presentation.changepassword.state

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordVisible: Boolean = false,
    val newPasswordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    // Password strength: 0-4
    val passwordStrength: Int = 0
) {
    val canSave: Boolean
        get() = currentPassword.isNotBlank()
                && newPassword.isNotBlank()
                && confirmPassword.isNotBlank()
                && !isSaving
}
