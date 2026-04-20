package com.oussama_chatri.productivityx.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.features.auth.domain.usecase.RefreshTokenUseCase
import com.oussama_chatri.productivityx.features.auth.presentation.state.SplashUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val refreshTokenUseCase: RefreshTokenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Checking)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            try {
                // Minimum splash duration for branding
                delay(1500)
                val isLoggedIn = refreshTokenUseCase.isLoggedIn()
                _uiState.value = if (isLoggedIn) {
                    SplashUiState.Authenticated
                } else {
                    SplashUiState.Unauthenticated
                }
            } catch (e: Exception) {
                _uiState.value = SplashUiState.Unauthenticated
            }
        }
    }
}