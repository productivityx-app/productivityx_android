package com.oussama_chatri.productivityx.features.settings.presentation.profile.event

sealed class ProfileUiEvent {
    data object LoadData : ProfileUiEvent()
    data class ThemeChanged(val theme: String) : ProfileUiEvent()
    data class LanguageChanged(val language: String) : ProfileUiEvent()
    data object SignOutClicked : ProfileUiEvent()
    data object SignOutConfirmed : ProfileUiEvent()
    data object DismissError : ProfileUiEvent()
    data object DismissSuccess : ProfileUiEvent()
    data object DeleteAccountClicked : ProfileUiEvent()
    data object DeleteAccountConfirmed : ProfileUiEvent()
}
