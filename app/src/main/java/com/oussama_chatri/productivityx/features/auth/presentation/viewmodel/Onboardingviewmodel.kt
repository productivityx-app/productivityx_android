package com.oussama_chatri.productivityx.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: PreferencesDataStore
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.setOnboardingCompleted(true)
        }
    }
}
