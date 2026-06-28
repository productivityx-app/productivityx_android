package com.oussama_chatri.productivityx.features.settings.presentation.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.settings.domain.model.ProfileModel
import com.oussama_chatri.productivityx.features.settings.domain.usecase.GetProfileUseCase
import com.oussama_chatri.productivityx.features.settings.domain.usecase.UpdateAvatarUseCase
import com.oussama_chatri.productivityx.features.settings.domain.usecase.UpdateProfileUseCase
import com.oussama_chatri.productivityx.features.settings.presentation.editprofile.event.EditProfileUiEvent
import com.oussama_chatri.productivityx.features.settings.presentation.editprofile.state.EditProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState(isLoading = true))
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var originalProfile: ProfileModel? = null

    init {
        loadProfile()
    }

    fun onEvent(event: EditProfileUiEvent) {
        when (event) {
            is EditProfileUiEvent.FirstNameChanged -> {
                _uiState.update { it.copy(firstName = event.value, firstNameError = null) }
            }
            is EditProfileUiEvent.LastNameChanged -> {
                _uiState.update { it.copy(lastName = event.value, lastNameError = null) }
            }
            is EditProfileUiEvent.BioChanged -> {
                if (event.value.length <= 500) {
                    _uiState.update { it.copy(bio = event.value, bioError = null) }
                }
            }
            is EditProfileUiEvent.TimezoneChanged -> {
                _uiState.update { it.copy(timezone = event.value) }
            }
            is EditProfileUiEvent.LanguageChanged -> {
                _uiState.update { it.copy(language = event.value) }
            }
            is EditProfileUiEvent.ThemeChanged -> {
                _uiState.update { it.copy(theme = event.value) }
            }
            is EditProfileUiEvent.AvatarUrlChanged -> updateAvatar(event.url)
            EditProfileUiEvent.SaveClicked -> save()
            EditProfileUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = getProfileUseCase()) {
                is Resource.Success -> {
                    originalProfile = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            firstName = result.data.firstName,
                            lastName = result.data.lastName,
                            bio = result.data.bio ?: "",
                            timezone = result.data.timezone,
                            language = result.data.language,
                            theme = result.data.theme,
                            avatarUrl = result.data.avatarUrl
                        )
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var valid = true

        if (state.firstName.isBlank()) {
            _uiState.update { it.copy(firstNameError = "First name is required") }
            valid = false
        } else if (state.firstName.length > 50) {
            _uiState.update { it.copy(firstNameError = "Max 50 characters") }
            valid = false
        }

        if (state.lastName.isBlank()) {
            _uiState.update { it.copy(lastNameError = "Last name is required") }
            valid = false
        } else if (state.lastName.length > 50) {
            _uiState.update { it.copy(lastNameError = "Max 50 characters") }
            valid = false
        }

        return valid
    }

    private fun save() {
        if (!validate()) return
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val result = updateProfileUseCase(
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                bio = state.bio.trim().ifBlank { null },
                timezone = state.timezone,
                language = state.language,
                theme = state.theme
            )

            when (result) {
                is Resource.Success -> {
                    originalProfile = result.data
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun updateAvatar(url: String) {
        viewModelScope.launch {
            when (val result = updateAvatarUseCase(url)) {
                is Resource.Success -> _uiState.update {
                    it.copy(avatarUrl = result.data.avatarUrl)
                }
                is Resource.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                Resource.Loading -> Unit
            }
        }
    }
}
