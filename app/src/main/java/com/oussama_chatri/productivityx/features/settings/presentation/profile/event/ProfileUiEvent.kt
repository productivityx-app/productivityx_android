package com.oussama_chatri.productivityx.features.profile.presentation.profile.event

sealed class ProfileUiEvent {
    data object LoadData : ProfileUiEvent()
    data object SignOutClicked : ProfileUiEvent()
    data object SignOutConfirmed : ProfileUiEvent()
    data object DismissError : ProfileUiEvent()
    data object DismissSuccess : ProfileUiEvent()
}
