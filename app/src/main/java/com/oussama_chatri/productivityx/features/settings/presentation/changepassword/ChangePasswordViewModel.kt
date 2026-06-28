package com.oussama_chatri.productivityx.features.settings.presentation.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.auth.domain.usecase.ChangePasswordUseCase
import com.oussama_chatri.productivityx.features.settings.presentation.changepassword.event.ChangePasswordUiEvent
import com.oussama_chatri.productivityx.features.settings.presentation.changepassword.state.ChangePasswordUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onEvent(event: ChangePasswordUiEvent) {
        when (event) {
            is ChangePasswordUiEvent.CurrentPasswordChanged -> _uiState.update {
                it.copy(currentPassword = event.value, currentPasswordError = null)
            }
            is ChangePasswordUiEvent.NewPasswordChanged -> _uiState.update {
                it.copy(
                    newPassword = event.value,
                    newPasswordError = null,
                    passwordStrength = computeStrength(event.value)
                )
            }
            is ChangePasswordUiEvent.ConfirmPasswordChanged -> _uiState.update {
                it.copy(confirmPassword = event.value, confirmPasswordError = null)
            }
            ChangePasswordUiEvent.ToggleCurrentPasswordVisibility -> _uiState.update {
                it.copy(currentPasswordVisible = !it.currentPasswordVisible)
            }
            ChangePasswordUiEvent.ToggleNewPasswordVisibility -> _uiState.update {
                it.copy(newPasswordVisible = !it.newPasswordVisible)
            }
            ChangePasswordUiEvent.ToggleConfirmPasswordVisibility -> _uiState.update {
                it.copy(confirmPasswordVisible = !it.confirmPasswordVisible)
            }
            ChangePasswordUiEvent.SaveClicked -> changePassword()
            ChangePasswordUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun computeStrength(password: String): Int {
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var valid = true

        if (state.currentPassword.isBlank()) {
            _uiState.update { it.copy(currentPasswordError = "Current password is required") }
            valid = false
        }

        if (state.newPassword.length < 8) {
            _uiState.update { it.copy(newPasswordError = "Minimum 8 characters") }
            valid = false
        }

        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            valid = false
        }

        if (state.newPassword == state.currentPassword) {
            _uiState.update { it.copy(newPasswordError = "New password must differ from current") }
            valid = false
        }

        return valid
    }

    private fun changePassword() {
        if (!validate()) return
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            when (val result = changePasswordUseCase(state.currentPassword, state.newPassword)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isSaving = false, saveSuccess = true)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}
