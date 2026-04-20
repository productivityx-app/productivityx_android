package com.oussama_chatri.productivityx.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.core.util.ValidationUtils
import com.oussama_chatri.productivityx.features.auth.domain.usecase.ResetPasswordUseCase
import com.oussama_chatri.productivityx.features.auth.presentation.event.ResetPasswordUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.state.ResetPasswordUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun initToken(token: String) {
        if (_uiState.value.token.isBlank()) {
            _uiState.update { it.copy(token = token) }
        }
    }

    fun onEvent(event: ResetPasswordUiEvent) {
        when (event) {
            is ResetPasswordUiEvent.TokenChanged -> _uiState.update { it.copy(token = event.value) }
            is ResetPasswordUiEvent.NewPasswordChanged -> _uiState.update {
                it.copy(newPassword = event.value, newPasswordError = null, error = null)
            }
            is ResetPasswordUiEvent.ConfirmPasswordChanged -> _uiState.update {
                it.copy(confirmPassword = event.value, confirmPasswordError = null)
            }
            ResetPasswordUiEvent.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }
            ResetPasswordUiEvent.ToggleConfirmPasswordVisibility -> _uiState.update {
                it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
            }
            ResetPasswordUiEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        var hasError = false

        if (!ValidationUtils.isValidPassword(state.newPassword)) {
            _uiState.update {
                it.copy(newPasswordError = "Password must be at least 8 characters with uppercase, number, and symbol")
            }
            hasError = true
        }
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (val result = resetPasswordUseCase(state.token, state.newPassword)) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                        _events.send(
                            UiEvent.Navigate(
                                com.oussama_chatri.productivityx.core.ui.navigation.MainRoute.Home
                            )
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Password reset failed.")
                }
            }
        }
    }
}