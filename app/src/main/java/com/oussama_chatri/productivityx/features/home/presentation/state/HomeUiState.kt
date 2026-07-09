package com.oussama_chatri.productivityx.features.home.presentation.state

import com.oussama_chatri.productivityx.core.util.UiState
import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import com.oussama_chatri.productivityx.features.home.domain.model.WidgetType

data class HomeUiState(
    val dashboardState: UiState<DashboardSummary> = UiState.Loading,
    val isRefreshing: Boolean = false,
    val isFocusMode: Boolean = false,
    val expandedWidget: WidgetType? = null,
    val widgetOrder: List<WidgetType> = WidgetType.defaultOrder(),
    val showRadialMenu: Boolean = false,
    val showCalculator: Boolean = false,
    val selectedQuickAction: QuickAction? = null,
    val voiceCommandActive: Boolean = false,
    val voiceCommandText: String = "",
)

enum class QuickAction {
    NEW_NOTE, NEW_TASK, START_TIMER, AI_CHAT, CALCULATOR
}
