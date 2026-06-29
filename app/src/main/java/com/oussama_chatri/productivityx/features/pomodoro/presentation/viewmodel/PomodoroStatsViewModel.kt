package com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import com.oussama_chatri.productivityx.features.pomodoro.domain.repository.PomodoroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PomodoroStatsViewModel @Inject constructor(
    private val repository: PomodoroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroStatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val now = LocalDate.now()
            // Default to last 30 days for stats
            val result = repository.getDetailedStats(now.minusDays(30), now)
            
            if (result is Resource.Success) {
                _uiState.update { it.copy(stats = result.data, isLoading = false) }
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(error = result.message, isLoading = false) }
            }
        }
    }

    fun updateGoals(daily: Int, weekly: Int) {
        viewModelScope.launch {
            repository.updateGoals(daily, weekly)
            loadStats() // Refresh
        }
    }
}

data class PomodoroStatsUiState(
    val stats: PomodoroStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTimeRange: TimeRange = TimeRange.WEEKLY,
    val dailyGoal: Int = 120,
    val weeklyTarget: Int = 600
)

enum class TimeRange {
    DAILY, WEEKLY, MONTHLY
}
