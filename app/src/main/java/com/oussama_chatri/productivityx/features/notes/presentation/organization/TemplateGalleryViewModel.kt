package com.oussama_chatri.productivityx.features.notes.presentation.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteTemplate
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveTemplatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TemplateGalleryState(
    val templates: List<NoteTemplate> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class TemplateGalleryViewModel @Inject constructor(
    private val observeTemplates: ObserveTemplatesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TemplateGalleryState())
    val state: StateFlow<TemplateGalleryState> = _state.asStateFlow()

    init {
        observeTemplates().onEach { templates ->
            _state.update { it.copy(templates = templates, isLoading = false) }
        }.catch {}.launchIn(viewModelScope)
    }
}
