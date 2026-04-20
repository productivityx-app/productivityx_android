package com.oussama_chatri.productivityx.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.core.util.ValidationUtils
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthResult
import com.oussama_chatri.productivityx.features.auth.domain.usecase.ResendVerificationUseCase
import com.oussama_chatri.productivityx.features.auth.domain.usecase.ResetPasswordUseCase
import com.oussama_chatri.productivityx.features.auth.domain.usecase.VerifyOtpUseCase
import com.oussama_chatri.productivityx.features.auth.presentation.event.ResetPasswordUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.event.VerifyEmailUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.state.ResetPasswordUiState
import com.oussama_chatri.productivityx.features.auth.presentation.state.VerifyEmailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resendVerificationUseCase: ResendVerificationUseCase
) : ViewModel()
{

    private val _uiState = MutableStateFlow(VerifyEmailUiState())
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var cooldownJob: Job? = null

    fun initEmail(email: String) {
        if (_uiState.value.email.isBlank()) {
            _uiState.update { it.copy(email = email) }
            startCooldown()
        }
    }

    fun onEvent(event: VerifyEmailUiEvent) {
        when (event) {
            is VerifyEmailUiEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, error = null)
            }
            is VerifyEmailUiEvent.OtpChanged -> _uiState.update {
                it.copy(otp = event.value, error = null)
            }
            VerifyEmailUiEvent.Verify -> verify()
            VerifyEmailUiEvent.Resend -> resend()
        }
    }

    private fun verify() {
        val state = _uiState.value
        if (state.otp.length != 6) {
            _uiState.update { it.copy(error = "Enter the complete 6-digit code") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (val result = verifyOtpUseCase(state.email, state.otp)) {
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(UiEvent.Navigate(com.oussama_chatri.productivityx.core.ui.navigation.MainRoute.Home))
                    }
                    is AuthResult.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                    is AuthResult.Unverified -> _uiState.update {
                        it.copy(isLoading = false, error = "Verification pending. Please try again.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Verification failed.")
                }
            }
        }
    }

    private fun resend() {
        val email = _uiState.value.email
        if (email.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, error = null) }
            try {
                resendVerificationUseCase(email)
                startCooldown()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isResending = false, error = e.message ?: "Failed to resend code.")
                }
            } finally {
                _uiState.update { it.copy(isResending = false) }
            }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.update { it.copy(resendCooldownSeconds = i) }
                if (i > 0) delay(1000)
            }
        }
    }
}

