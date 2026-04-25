package com.oussama_chatri.productivityx.features.profile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.auth.domain.usecase.LogoutUseCase
import com.oussama_chatri.productivityx.features.profile.domain.usecase.GetPreferencesUseCase
import com.oussama_chatri.productivityx.features.profile.domain.usecase.GetProfileUseCase
import com.oussama_chatri.productivityx.features.profile.presentation.profile.event.ProfileUiEvent
import com.oussama_chatri.productivityx.features.profile.presentation.profile.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileNavEffect {
    data object NavigateToLogin : ProfileNavEffect()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getPreferencesUseCase: GetPreferencesUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _navEffect = Channel<ProfileNavEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    init {
        loadData()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.LoadData -> loadData()
            ProfileUiEvent.SignOutClicked -> _uiState.update { it.copy(isSigningOut = true) }
            ProfileUiEvent.SignOutConfirmed -> signOut()
            ProfileUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
            ProfileUiEvent.DismissSuccess -> _uiState.update { it.copy(successMessage = null) }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val profileDeferred = async { getProfileUseCase() }
            val prefsDeferred = async { getPreferencesUseCase() }

            val profileResult = profileDeferred.await()
            val prefsResult = prefsDeferred.await()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    profile = (profileResult as? Resource.Success)?.data ?: state.profile,
                    preferences = (prefsResult as? Resource.Success)?.data ?: state.preferences,
                    errorMessage = when {
                        profileResult is Resource.Error -> profileResult.message
                        prefsResult is Resource.Error -> prefsResult.message
                        else -> null
                    }
                )
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = false) }
            logoutUseCase()
            _navEffect.send(ProfileNavEffect.NavigateToLogin)
        }
    }
}
