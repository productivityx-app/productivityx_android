package com.oussama_chatri.productivityx.features.settings.presentation.profile.state

import com.oussama_chatri.productivityx.features.settings.domain.model.ProfileModel
import com.oussama_chatri.productivityx.features.settings.domain.model.UserPreferencesModel

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileModel? = null,
    val preferences: UserPreferencesModel? = null,
    val isLocalOnly: Boolean = false,
    val currentTheme: String = "DARK",
    val currentLanguage: String = "en",
    val isSigningOut: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val tasksCompleted: Int = 0,
    val focusHours: Int = 0,
    val notesCreated: Int = 0,
    val aiConversations: Int = 0,
    val subscriptionStatus: String = "Free",
    val subscriptionRenewal: String? = null,
    val storageUsedMb: Int = 0,
    val storageTotalMb: Int = 100,
    val connectedDevices: Int = 0,
    val isDeleting: Boolean = false,
    val recentActivity: List<ActivityItem> = emptyList(),
    val achievementBadges: List<BadgeItem> = emptyList(),
    val productivityTrend: Float = 0.5f,
    val username: String = "",
)

data class ActivityItem(
    val id: String,
    val type: String,
    val title: String,
    val timestamp: Long,
)

data class BadgeItem(
    val id: String,
    val label: String,
    val icon: String,
    val unlocked: Boolean,
)
