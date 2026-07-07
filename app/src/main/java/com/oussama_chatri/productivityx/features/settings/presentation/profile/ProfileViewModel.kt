package com.oussama_chatri.productivityx.features.settings.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama_chatri.productivityx.core.data.DataExportImportManager
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.core.util.getOrNull
import com.oussama_chatri.productivityx.features.auth.domain.usecase.DeleteAccountUseCase
import com.oussama_chatri.productivityx.features.auth.domain.usecase.GetCurrentUserUseCase
import com.oussama_chatri.productivityx.features.auth.domain.usecase.LogoutUseCase
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.pomodoro.data.local.dao.PomodoroSessionDao
import com.oussama_chatri.productivityx.features.settings.domain.usecase.GetPreferencesUseCase
import com.oussama_chatri.productivityx.features.settings.domain.usecase.GetProfileUseCase
import com.oussama_chatri.productivityx.features.settings.presentation.profile.event.ProfileUiEvent
import com.oussama_chatri.productivityx.features.settings.presentation.profile.state.ActivityItem
import com.oussama_chatri.productivityx.features.settings.presentation.profile.state.BadgeItem
import com.oussama_chatri.productivityx.features.settings.presentation.profile.state.ProfileUiState
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ProfileNavEffect {
    data object NavigateToLogin : ProfileNavEffect()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getPreferencesUseCase: GetPreferencesUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val taskDao: TaskDao,
    private val noteDao: NoteDao,
    private val pomodoroSessionDao: PomodoroSessionDao,
    private val exportImportManager: DataExportImportManager,
    private val prefs: PreferencesDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _navEffect = Channel<ProfileNavEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val isLocalOnly = prefs.isLocalOnly()
            _uiState.update { it.copy(
                isLocalOnly    = isLocalOnly,
                currentTheme   = prefs.appTheme.first(),
                currentLanguage = prefs.language.first(),
            ) }
            if (isLocalOnly) {
                _uiState.update { it.copy(isLoading = false) }
            } else {
                loadData()
            }
            loadLocalStats()
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.LoadData -> loadData()
            is ProfileUiEvent.ThemeChanged -> setTheme(event.theme)
            is ProfileUiEvent.LanguageChanged -> setLanguage(event.language)
            ProfileUiEvent.SignOutClicked -> _uiState.update { it.copy(isSigningOut = true) }
            ProfileUiEvent.SignOutConfirmed -> signOut()
            ProfileUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
            ProfileUiEvent.DismissSuccess -> _uiState.update { it.copy(successMessage = null) }
            ProfileUiEvent.DeleteAccountClicked -> _uiState.update { it.copy(isDeleting = true) }
            is ProfileUiEvent.DeleteAccountConfirmed -> deleteAccount(event.password)
        }
    }

    private fun setTheme(theme: String) {
        viewModelScope.launch { prefs.setTheme(theme) }
    }

    private fun setLanguage(language: String) {
        viewModelScope.launch { prefs.setLanguage(language) }
    }

    fun exportToFile(file: File) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isExporting = true) }
                exportImportManager.exportToFile(file)
                _uiState.update { it.copy(isExporting = false, successMessage = "Data exported successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, errorMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun importFromFile(file: File) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isImporting = true) }
                exportImportManager.importFromFile(file)
                _uiState.update { it.copy(isImporting = false, successMessage = "Data imported successfully") }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, errorMessage = "Import failed: ${e.message}") }
            }
        }
    }

    private fun loadData() {
        if (_uiState.value.isLocalOnly) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val profileDeferred = async { getProfileUseCase() }
            val prefsDeferred = async { getPreferencesUseCase() }

            val profileResult = profileDeferred.await()
            val prefsResult = prefsDeferred.await()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    profile = (profileResult as? Resource.Success)?.data ?: state.profile,
                    preferences = (prefsResult as? Resource.Success)?.data ?: state.preferences,
                    errorMessage = when {
                        profileResult is Resource.Error -> profileResult.message
                        prefsResult is Resource.Error -> prefsResult.message
                        else -> null
                    }
                )
            }
        }
    }

    private fun loadLocalStats() {
        viewModelScope.launch {
            val userResult = getCurrentUserUseCase()
            val userId = userResult.getOrNull()?.id ?: return@launch
            val username = userResult.getOrNull()?.username ?: "user_${(1000..9999).random()}"

            val completedTasks = taskDao.countCompleted(userId)
            val activeNotes = noteDao.countActiveNotes(userId)
            val focusSeconds = pomodoroSessionDao.getTotalFocusSeconds(userId)
            val focusHours = (focusSeconds / 3600).toInt()

            _uiState.update {
                it.copy(
                    tasksCompleted = completedTasks,
                    focusHours = focusHours,
                    notesCreated = activeNotes.toInt(),
                    username = username,
                    // Keep mock for others if there's no real backend for it yet
                    aiConversations = (0..30).random(),
                    storageUsedMb = (10..90).random(),
                    connectedDevices = (0..3).random(),
                    productivityTrend = 0.3f + (0..7).random() * 0.1f,
                    recentActivity = listOf(
                        ActivityItem("a1", "task", "Completed project setup", System.currentTimeMillis() - 3600000),
                        ActivityItem("a2", "note", "Created meeting notes", System.currentTimeMillis() - 7200000),
                        ActivityItem("a3", "event", "Added team standup", System.currentTimeMillis() - 14400000),
                        ActivityItem("a4", "pomo", "Completed 2 focus sessions", System.currentTimeMillis() - 28800000),
                    ),
                    achievementBadges = listOf(
                        BadgeItem("b1", "Early Bird", "wb_sunny", true),
                        BadgeItem("b2", "Focused", "timer", true),
                        BadgeItem("b3", "Organizer", "folder", true),
                        BadgeItem("b4", "Streak 7", "whatshot", focusHours >= 7),
                        BadgeItem("b5", "Century", "star", completedTasks >= 100),
                    ),
                )
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = false) }
            logoutUseCase()
            _navEffect.send(ProfileNavEffect.NavigateToLogin)
        }
    }

    private fun deleteAccount(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            val result = deleteAccountUseCase(password)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDeleting = false, successMessage = "Account deleted successfully") }
                    _navEffect.send(ProfileNavEffect.NavigateToLogin)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isDeleting = false, errorMessage = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
