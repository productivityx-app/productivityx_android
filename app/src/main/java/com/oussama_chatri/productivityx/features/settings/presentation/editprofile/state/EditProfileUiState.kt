package com.oussama_chatri.productivityx.features.profile.presentation.editprofile.state

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val bio: String = "",
    val timezone: String = "UTC",
    val language: String = "EN",
    val theme: String = "DARK",
    val avatarUrl: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val bioError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val hasChanges: Boolean get() = true // always allow save attempt; VM tracks original
    val bioCharCount: Int get() = bio.length
}
