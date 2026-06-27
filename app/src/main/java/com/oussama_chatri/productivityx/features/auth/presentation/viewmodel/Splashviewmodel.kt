package com.oussama_chatri.productivityx.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.features.auth.domain.usecase.RefreshTokenUseCase
import com.oussama_chatri.productivityx.features.auth.presentation.state.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val prefs: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Checking)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            try {
                delay(1500)
                val onboardingDone = prefs.onboardingCompleted.first()
                if (!onboardingDone) {
                    _uiState.value = SplashUiState.ShowOnboarding
                    return@launch
                }
                val authSkipped = prefs.authSkipCompleted.first()
                if (authSkipped) {
                    _uiState.value = SplashUiState.Authenticated
                    return@launch
                }
                val isLoggedIn = refreshTokenUseCase.isLoggedIn()
                _uiState.value = if (isLoggedIn) {
                    SplashUiState.Authenticated
                } else {
                    SplashUiState.ShowLogin
                }
            } catch (e: Exception) {
                _uiState.value = SplashUiState.ShowLogin
            }
        }
    }
}
