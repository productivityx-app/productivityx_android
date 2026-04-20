package com.oussama_chatri.productivityx.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.core.util.ValidationUtils
import com.oussama_chatri.productivityx.features.auth.domain.usecase.RegisterUseCase
import com.oussama_chatri.productivityx.features.auth.domain.usecase.ResendVerificationUseCase
import com.oussama_chatri.productivityx.features.auth.domain.usecase.VerifyOtpUseCase
import com.oussama_chatri.productivityx.features.auth.presentation.event.RegisterUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.state.RegisterUiState
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
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resendVerificationUseCase: ResendVerificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var resendCooldownJob: Job? = null

    fun onEvent(event: RegisterUiEvent) {
        when (event) {
            RegisterUiEvent.NextStep -> handleNextStep()
            RegisterUiEvent.PrevStep -> _uiState.update {
                it.copy(currentStep = maxOf(0, it.currentStep - 1), generalError = null)
            }

            is RegisterUiEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, emailError = null, generalError = null)
            }
            is RegisterUiEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, passwordError = null)
            }
            is RegisterUiEvent.ConfirmPasswordChanged -> _uiState.update {
                it.copy(confirmPassword = event.value, confirmPasswordError = null)
            }
            RegisterUiEvent.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }
            RegisterUiEvent.ToggleConfirmPasswordVisibility -> _uiState.update {
                it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
            }

            is RegisterUiEvent.FirstNameChanged -> _uiState.update {
                it.copy(firstName = event.value, firstNameError = null)
            }
            is RegisterUiEvent.LastNameChanged -> _uiState.update {
                it.copy(lastName = event.value, lastNameError = null)
            }
            is RegisterUiEvent.UsernameChanged -> _uiState.update {
                it.copy(username = event.value)
            }
            is RegisterUiEvent.PhoneChanged -> _uiState.update {
                it.copy(phone = event.value)
            }
            is RegisterUiEvent.BirthDateChanged -> _uiState.update {
                it.copy(birthDate = event.value, birthDateError = null)
            }

            is RegisterUiEvent.ThemeSelected -> _uiState.update {
                it.copy(selectedTheme = event.theme)
            }

            is RegisterUiEvent.OtpChanged -> _uiState.update {
                it.copy(otp = event.value, generalError = null)
            }
            RegisterUiEvent.ResendOtp -> resendOtp()
            RegisterUiEvent.VerifyOtp -> verifyOtp()
        }
    }

    private fun handleNextStep() {
        when (_uiState.value.currentStep) {
            0 -> validateAndAdvanceStep0()
            1 -> validateAndAdvanceStep1()
            2 -> submitRegistration()
            else -> {}
        }
    }

    private fun validateAndAdvanceStep0() {
        val state = _uiState.value
        var hasError = false

        if (!ValidationUtils.isValidEmail(state.email)) {
            _uiState.update { it.copy(emailError = "Enter a valid email address") }
            hasError = true
        }
        if (!ValidationUtils.isValidPassword(state.password)) {
            _uiState.update {
                it.copy(passwordError = "Password must be at least 8 characters with uppercase, number, and symbol")
            }
            hasError = true
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            hasError = true
        }
        if (!hasError) {
            _uiState.update { it.copy(currentStep = 1, generalError = null) }
        }
    }

    private fun validateAndAdvanceStep1() {
        val state = _uiState.value
        var hasError = false

        if (!ValidationUtils.isValidName(state.firstName)) {
            _uiState.update { it.copy(firstNameError = "First name is required") }
            hasError = true
        }
        if (!ValidationUtils.isValidName(state.lastName)) {
            _uiState.update { it.copy(lastNameError = "Last name is required") }
            hasError = true
        }
        if (state.birthDate.isBlank()) {
            _uiState.update { it.copy(birthDateError = "Date of birth is required") }
            hasError = true
        }
        if (!hasError) {
            _uiState.update { it.copy(currentStep = 2, generalError = null) }
        }
    }

    private fun submitRegistration() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                val result = registerUseCase(
                    email = state.email.trim(),
                    password = state.password,
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    username = state.username.trim().ifBlank { state.email.substringBefore("@") },
                    birthDate = state.birthDate,
                    phone = state.phone.trim().ifBlank { null }
                )
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, currentStep = 3, pendingEmail = state.email.trim())
                        }
                        startResendCooldown()
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, generalError = result.message)
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, generalError = e.message ?: "Registration failed.")
                }
            }
        }
    }

    private fun verifyOtp() {
        val state = _uiState.value
        if (state.otp.length < 6) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                when (val result = verifyOtpUseCase(state.pendingEmail, state.otp)) {
                    is com.oussama_chatri.productivityx.features.auth.domain.model.AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(UiEvent.Navigate(com.oussama_chatri.productivityx.core.ui.navigation.MainRoute.Home))
                    }
                    is com.oussama_chatri.productivityx.features.auth.domain.model.AuthResult.Error -> _uiState.update {
                        it.copy(isLoading = false, generalError = result.message)
                    }
                    is com.oussama_chatri.productivityx.features.auth.domain.model.AuthResult.Unverified -> _uiState.update {
                        it.copy(isLoading = false, generalError = "Verification pending. Please try again.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, generalError = e.message ?: "Verification failed.")
                }
            }
        }
    }

    private fun resendOtp() {
        val email = _uiState.value.pendingEmail
        if (email.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, generalError = null) }
            try {
                resendVerificationUseCase(email)
                startResendCooldown()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(generalError = e.message ?: "Failed to resend code.")
                }
            } finally {
                _uiState.update { it.copy(isResending = false) }
            }
        }
    }

    private fun startResendCooldown() {
        resendCooldownJob?.cancel()
        resendCooldownJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.update { it.copy(resendCooldownSeconds = i) }
                if (i > 0) delay(1000)
            }
        }
    }
}