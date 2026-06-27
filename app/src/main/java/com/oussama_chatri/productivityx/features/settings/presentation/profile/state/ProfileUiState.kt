package com.oussama_chatri.productivityx.features.profile.presentation.profile.state

import com.oussama_chatri.productivityx.features.profile.domain.model.ProfileModel
import com.oussama_chatri.productivityx.features.profile.domain.model.UserPreferencesModel

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileModel? = null,
    val preferences: UserPreferencesModel? = null,
    val isLocalOnly: Boolean = false,
    val currentTheme: String = "DARK",
    val currentLanguage: String = "en",
    val isSigningOut: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
