package com.oussama_chatri.productivityx.features.profile.presentation.editprofile.event

sealed class EditProfileUiEvent {
    data class FirstNameChanged(val value: String) : EditProfileUiEvent()
    data class LastNameChanged(val value: String) : EditProfileUiEvent()
    data class BioChanged(val value: String) : EditProfileUiEvent()
    data class TimezoneChanged(val value: String) : EditProfileUiEvent()
    data class LanguageChanged(val value: String) : EditProfileUiEvent()
    data class ThemeChanged(val value: String) : EditProfileUiEvent()
    data class AvatarUrlChanged(val url: String) : EditProfileUiEvent()
    data object SaveClicked : EditProfileUiEvent()
    data object DismissError : EditProfileUiEvent()
}
