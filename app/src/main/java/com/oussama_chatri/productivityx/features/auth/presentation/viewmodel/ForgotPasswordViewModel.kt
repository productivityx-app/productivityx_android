package com.oussama_chatri.productivityx.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.core.util.ValidationUtils
import com.oussama_chatri.productivityx.features.auth.domain.usecase.ForgotPasswordUseCase
import com.oussama_chatri.productivityx.features.auth.domain.usecase.VerifyForgotPasswordOtpUseCase
import com.oussama_chatri.productivityx.features.auth.presentation.event.ForgotPasswordUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.state.ForgotPasswordUiState
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
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val verifyForgotPasswordOtpUseCase: VerifyForgotPasswordOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var cooldownJob: Job? = null

    fun onEvent(event: ForgotPasswordUiEvent) {
        when (event) {
            is ForgotPasswordUiEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, emailError = null, error = null)
            }
            ForgotPasswordUiEvent.Submit -> submitEmail()

            is ForgotPasswordUiEvent.OtpChanged -> _uiState.update {
                it.copy(otp = event.value, error = null)
            }
            ForgotPasswordUiEvent.VerifyOtp -> verifyOtp()
            ForgotPasswordUiEvent.ResendOtp -> resendOtp()

            ForgotPasswordUiEvent.GoBackToEmail -> _uiState.update {
                it.copy(showOtpStep = false, otp = "", error = null)
            }
        }
    }

    private fun submitEmail() {
        val email = _uiState.value.email.trim()
        if (!ValidationUtils.isValidEmail(email)) {
            _uiState.update { it.copy(emailError = "Enter a valid email address") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = forgotPasswordUseCase(email)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, showOtpStep = true) }
                    startCooldown()
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun verifyOtp() {
        val otp   = _uiState.value.otp.trim()
        val email = _uiState.value.email.trim()

        if (otp.length < 6) {
            _uiState.update { it.copy(error = "Enter the 6-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = verifyForgotPasswordOtpUseCase(email, otp)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, resetToken = result.data) }
                    _events.send(
                        UiEvent.Navigate(
                            com.oussama_chatri.productivityx.core.ui.navigation
                                .AuthRoute.ResetPassword(token = result.data)
                        )
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun resendOtp() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, error = null) }
            when (val result = forgotPasswordUseCase(email)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isResending = false, otp = "") }
                    startCooldown()
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isResending = false, error = result.message)
                }
                is Resource.Loading -> {}
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