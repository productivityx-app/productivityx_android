package com.oussama_chatri.productivityx.features.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResult
import com.oussama_chatri.productivityx.features.search.domain.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false, error = null)
            return
        }
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            delay(300)
            val local = repository.searchLocal(query)
            _uiState.value = _uiState.value.copy(results = local, isLoading = false)
            when (val remote = repository.search(query)) {
                is Resource.Success -> {
                    val merged = (local + remote.data)
                        .distinctBy { it.id }
                        .sortedByDescending { it.updatedAt }
                    _uiState.value = _uiState.value.copy(results = merged)
                }
                is Resource.Error -> {
                    if (local.isEmpty()) {
                        _uiState.value = _uiState.value.copy(error = remote.message)
                    }
                }
                Resource.Loading -> {}
            }
        }
    }
}
