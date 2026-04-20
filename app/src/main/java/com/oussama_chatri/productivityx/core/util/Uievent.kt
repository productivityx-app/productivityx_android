package com.oussama_chatri.productivityx.core.util

sealed class UiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    ) : UiEvent()

    data class Navigate(val route: Any) : UiEvent()

    data object NavigateBack : UiEvent()

    data class ShowDialog(val title: String, val message: String) : UiEvent()

    data object HideKeyboard : UiEvent()
}