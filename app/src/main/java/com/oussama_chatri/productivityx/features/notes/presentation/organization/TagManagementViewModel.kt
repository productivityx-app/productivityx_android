package com.oussama_chatri.productivityx.features.notes.presentation.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import com.oussama_chatri.productivityx.features.notes.domain.usecase.CreateTagUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.DeleteTagUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveTagsUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.UpdateTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagManagementState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class TagManagementViewModel @Inject constructor(
    private val observeTags: ObserveTagsUseCase,
    private val createTag: CreateTagUseCase,
    private val updateTag: UpdateTagUseCase,
    private val deleteTag: DeleteTagUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TagManagementState())
    val state: StateFlow<TagManagementState> = _state.asStateFlow()

    init {
        observeTags().onEach { tags ->
            _state.update { it.copy(tags = tags, isLoading = false) }
        }.catch {}.launchIn(viewModelScope)
    }

    fun createTag(name: String, color: String) {
        viewModelScope.launch { createTag(name, color) }
    }

    fun updateTag(tagId: String, name: String, color: String) {
        viewModelScope.launch { updateTag(tagId, name, color) }
    }

    fun deleteTag(tagId: String) {
        viewModelScope.launch { deleteTag(tagId) }
    }
}
