package com.oussama_chatri.productivityx.features.notes.presentation.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteFolder
import com.oussama_chatri.productivityx.features.notes.domain.usecase.CreateFolderUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.DeleteFolderUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.ObserveFoldersUseCase
import com.oussama_chatri.productivityx.features.notes.domain.usecase.UpdateFolderUseCase
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

data class FolderManagementState(
    val folders: List<NoteFolder> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FolderManagementViewModel @Inject constructor(
    private val observeFolders: ObserveFoldersUseCase,
    private val createFolder: CreateFolderUseCase,
    private val updateFolder: UpdateFolderUseCase,
    private val deleteFolder: DeleteFolderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FolderManagementState())
    val state: StateFlow<FolderManagementState> = _state.asStateFlow()

    init {
        observeFolders().onEach { folders ->
            _state.update { it.copy(folders = folders, isLoading = false) }
        }.catch {}.launchIn(viewModelScope)
    }

    fun createFolder(name: String, color: String) {
        viewModelScope.launch { createFolder(name, color) }
    }

    fun updateFolder(folderId: String, name: String, color: String) {
        viewModelScope.launch { updateFolder(folderId, name, color) }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch { deleteFolder(folderId) }
    }
}
