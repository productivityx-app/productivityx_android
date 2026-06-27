package com.oussama_chatri.productivityx.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.auth.domain.model.AuthResult
import com.oussama_chatri.productivityx.features.auth.domain.usecase.LoginUseCase
import com.oussama_chatri.productivityx.features.auth.presentation.event.LoginUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.state.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val prefs: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.IdentifierChanged -> _uiState.update {
                it.copy(identifier = event.value, identifierError = null, generalError = null)
            }
            is LoginUiEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, passwordError = null, generalError = null)
            }
            LoginUiEvent.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }
            LoginUiEvent.Submit -> submit()
            LoginUiEvent.SkipLogin -> skipLogin()
        }
    }

    fun skipLogin() {
        viewModelScope.launch {
            val localId = UUID.randomUUID().toString()
            prefs.cacheUser(id = localId, firstName = "Local", email = "local@local")
            prefs.setLocalOnlyMode(true)
            prefs.setOnboardingCompleted(true)
            prefs.setAuthSkipCompleted(true)
            _events.send(UiEvent.Navigate(com.oussama_chatri.productivityx.core.ui.navigation.MainRoute.Home))
        }
    }

    private fun submit() {
        val state = _uiState.value
        var hasError = false

        if (state.identifier.isBlank()) {
            _uiState.update { it.copy(identifierError = "Email or username is required") }
            hasError = true
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                when (val result = loginUseCase(state.identifier.trim(), state.password)) {
                    is AuthResult.Success -> {
                        prefs.setLocalOnlyMode(false)
                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(UiEvent.Navigate(com.oussama_chatri.productivityx.core.ui.navigation.MainRoute.Home))
                    }
                    is AuthResult.Unverified -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(
                            UiEvent.Navigate(
                                com.oussama_chatri.productivityx.core.ui.navigation.AuthRoute.VerifyEmail(result.email)
                            )
                        )
                    }
                    is AuthResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                generalError = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, generalError = e.message ?: "Login failed. Please try again.")
                }
            }
        }
    }
}