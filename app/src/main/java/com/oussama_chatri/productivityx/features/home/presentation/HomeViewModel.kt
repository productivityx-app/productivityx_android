package com.oussama_chatri.productivityx.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.UiState
import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import com.oussama_chatri.productivityx.features.home.domain.model.WidgetType
import com.oussama_chatri.productivityx.features.home.domain.usecase.GetDailyQuoteUseCase
import com.oussama_chatri.productivityx.features.home.domain.usecase.ObserveDashboardUseCase
import com.oussama_chatri.productivityx.features.home.domain.usecase.UpdateWidgetOrderUseCase
import com.oussama_chatri.productivityx.features.home.presentation.event.HomeEvent
import com.oussama_chatri.productivityx.features.home.presentation.state.HomeUiState
import com.oussama_chatri.productivityx.features.home.presentation.state.QuickAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeDashboard: ObserveDashboardUseCase,
    private val getDailyQuoteUseCase: GetDailyQuoteUseCase,
    private val updateWidgetOrderUseCase: UpdateWidgetOrderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var usageCount = mutableMapOf<WidgetType, Int>()

    init {
        collectDashboard()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> refresh()
            HomeEvent.ToggleFocusMode -> toggleFocusMode()
            is HomeEvent.ToggleWidgetExpanded -> toggleWidgetExpanded(event.widget)
            is HomeEvent.ReorderWidgets -> reorderWidgets(event.newOrder)
            is HomeEvent.ToggleWidgetVisibility -> toggleWidgetVisibility(event.widget, event.visible)
            HomeEvent.ToggleRadialMenu -> toggleRadialMenu()
            HomeEvent.ToggleCalculator -> toggleCalculator()
            is HomeEvent.QuickActionSelected -> handleQuickAction(event.action)
            HomeEvent.DismissQuickAction -> dismissQuickAction()
            is HomeEvent.VoiceCommandResult -> handleVoiceCommand(event.text)
            HomeEvent.DismissVoiceCommand -> dismissVoiceCommand()
            is HomeEvent.NavigateTo -> { /* handled by navigator callbacks */ }
        }
    }

    private fun collectDashboard() {
        viewModelScope.launch {
            observeDashboard()
                .onStart {
                    _uiState.value = HomeUiState()
                }
                .catch { e ->
                    _uiState.update { it.copy(dashboardState = UiState.Error(e.message ?: "Something went wrong")) }
                }
                .collect { summary ->
                    val withQuote = getDailyQuoteUseCase(summary)
                    _uiState.update {
                        it.copy(
                            dashboardState = UiState.Success(withQuote),
                            widgetOrder = it.widgetOrder.ifEmpty { WidgetType.defaultOrder() },
                        )
                    }
                }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(800)
            collectDashboard()
            delay(300)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun toggleFocusMode() {
        val newMode = !_uiState.value.isFocusMode
        _uiState.update { it.copy(isFocusMode = newMode) }
        if (newMode) {
            recordWidgetUsage(WidgetType.FOCUS_MODE_TOGGLE)
        }
    }

    private fun toggleWidgetExpanded(widget: WidgetType) {
        val current = _uiState.value.expandedWidget
        _uiState.update { it.copy(expandedWidget = if (current == widget) null else widget) }
        recordWidgetUsage(widget)
    }

    private fun reorderWidgets(newOrder: List<WidgetType>) {
        _uiState.update { it.copy(widgetOrder = newOrder) }
    }

    private fun toggleWidgetVisibility(widget: WidgetType, visible: Boolean) {
        _uiState.update { state ->
            val currentSummary = (state.dashboardState as? UiState.Success)?.data ?: return@update state
            val newVisibility = currentSummary.widgetVisibility.toMutableMap().apply {
                put(widget, visible)
            }
            state.copy(
                dashboardState = UiState.Success(
                    currentSummary.copy(widgetVisibility = newVisibility),
                ),
            )
        }
    }

    private fun toggleRadialMenu() {
        _uiState.update { it.copy(showRadialMenu = !it.showRadialMenu) }
    }

    private fun toggleCalculator() {
        _uiState.update { it.copy(showCalculator = !it.showCalculator) }
    }

    private fun handleQuickAction(action: QuickAction) {
        if (action == QuickAction.CALCULATOR) {
            _uiState.update { it.copy(selectedQuickAction = null, showRadialMenu = false, showCalculator = true) }
        } else {
            _uiState.update { it.copy(selectedQuickAction = action, showRadialMenu = false) }
        }
    }

    fun dismissQuickAction() {
        _uiState.update { it.copy(selectedQuickAction = null) }
    }

    private fun handleVoiceCommand(text: String) {
        _uiState.update { it.copy(voiceCommandText = text, voiceCommandActive = false) }
    }

    private fun dismissVoiceCommand() {
        _uiState.update { it.copy(voiceCommandActive = false) }
    }

    private fun recordWidgetUsage(widget: WidgetType) {
        usageCount[widget] = (usageCount[widget] ?: 0) + 1
        if (usageCount.values.sum() % 10 == 0) {
            val currentOrder = _uiState.value.widgetOrder
            val newOrder = updateWidgetOrderUseCase(currentOrder, usageCount.toMap())
            if (newOrder != currentOrder) {
                _uiState.update { it.copy(widgetOrder = newOrder) }
            }
        }
    }

    fun getUsageCount(): Map<WidgetType, Int> = usageCount.toMap()
}
