package com.oussama_chatri.productivityx.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.UiState
import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import com.oussama_chatri.productivityx.features.home.domain.usecase.ObserveDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val dashboardState: UiState<DashboardSummary> = UiState.Loading,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeDashboard: ObserveDashboardUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        collectDashboard()
    }

    private fun collectDashboard() {
        viewModelScope.launch {
            observeDashboard()
                .onStart { _uiState.value = HomeUiState(UiState.Loading) }
                .catch { e ->
                    _uiState.value = HomeUiState(
                        UiState.Error(e.message ?: "Something went wrong")
                    )
                }
                .collect { summary ->
                    _uiState.value = HomeUiState(UiState.Success(summary))
                }
        }
    }
}
