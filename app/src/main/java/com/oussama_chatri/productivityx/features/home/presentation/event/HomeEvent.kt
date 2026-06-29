package com.oussama_chatri.productivityx.features.home.presentation.event

import com.oussama_chatri.productivityx.features.home.domain.model.WidgetType
import com.oussama_chatri.productivityx.features.home.presentation.state.QuickAction

sealed interface HomeEvent {
    data object Refresh : HomeEvent
    data object ToggleFocusMode : HomeEvent
    data class ToggleWidgetExpanded(val widget: WidgetType) : HomeEvent
    data class ReorderWidgets(val newOrder: List<WidgetType>) : HomeEvent
    data class ToggleWidgetVisibility(val widget: WidgetType, val visible: Boolean) : HomeEvent
    data object ToggleRadialMenu : HomeEvent
    data class QuickActionSelected(val action: QuickAction) : HomeEvent
    data object DismissQuickAction : HomeEvent
    data class VoiceCommandResult(val text: String) : HomeEvent
    data object DismissVoiceCommand : HomeEvent
    data class NavigateTo(val route: String) : HomeEvent
}
