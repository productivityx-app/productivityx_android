package com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.GetSessionsUseCase
import com.oussama_chatri.productivityx.features.pomodoro.domain.usecase.GetTodayStatsUseCase
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.SessionHistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val getSessionsUseCase:  GetSessionsUseCase,
    private val getTodayStatsUseCase: GetTodayStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionHistoryUiState())
    val uiState: StateFlow<SessionHistoryUiState> = _uiState.asStateFlow()

    init {
        loadPage(0)
        loadStats()
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (!state.hasNextPage || state.isLoading) return
        loadPage(state.page + 1)
    }

    fun refresh() {
        _uiState.update { it.copy(sessions = emptyList(), page = 0) }
        loadPage(0)
        loadStats()
    }

    private fun loadPage(page: Int) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val result = getSessionsUseCase(page = page, size = 20)) {
                is Resource.Success -> {
                    val current = _uiState.value.sessions
                    val newList = if (page == 0) result.data else current + result.data
                    _uiState.update {
                        it.copy(
                            sessions    = newList,
                            isLoading   = false,
                            page        = page,
                            hasNextPage = result.data.size == 20
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            when (val result = getTodayStatsUseCase()) {
                is Resource.Success -> _uiState.update { it.copy(stats = result.data) }
                else -> Unit
            }
        }
    }
}
