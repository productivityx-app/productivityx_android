package com.oussama_chatri.productivityx.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "px_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.dataStore

    val onboardingCompleted: Flow<Boolean> = store.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }

    val appTheme: Flow<String> = store.data.map { it[KEY_THEME] ?: "DARK" }

    val lastSyncedAt: Flow<Long> = store.data.map { it[KEY_LAST_SYNCED_AT] ?: 0L }

    val cachedUserId: Flow<String?> = store.data.map { it[KEY_USER_ID] }

    val cachedUserFirstName: Flow<String?> = store.data.map { it[KEY_USER_FIRST_NAME] }

    val cachedUserEmail: Flow<String?> = store.data.map { it[KEY_USER_EMAIL] }

    val localOnlyMode: Flow<Boolean> = store.data.map { it[KEY_LOCAL_ONLY] ?: true }

    val offlineMode: Flow<Boolean> = store.data.map { it[KEY_OFFLINE_MODE] ?: false }

    val authSkipCompleted: Flow<Boolean> = store.data.map { it[KEY_AUTH_SKIP] ?: false }

    val language: Flow<String> = store.data.map { it[KEY_LANGUAGE] ?: "en" }

    val hapticFeedback: Flow<Boolean> = store.data.map { it[KEY_HAPTIC_FEEDBACK] ?: true }
    
    val fontScale: Flow<Float> = store.data.map { it[KEY_FONT_SCALE] ?: 1f }
    
    val fontFamily: Flow<String> = store.data.map { it[KEY_FONT_FAMILY] ?: "Nunito" }
    
    val compactMode: Flow<Boolean> = store.data.map { it[KEY_COMPACT_MODE] ?: false }
    
    val quietHoursStart: Flow<Int> = store.data.map { it[KEY_QUIET_HOURS_START] ?: 22 }
    
    val quietHoursEnd: Flow<Int> = store.data.map { it[KEY_QUIET_HOURS_END] ?: 8 }
    
    val aiModel: Flow<String> = store.data.map { it[KEY_AI_MODEL] ?: "gemini-2.0-flash" }
    
    val aiContextEnabled: Flow<Boolean> = store.data.map { it[KEY_AI_CONTEXT_ENABLED] ?: true }

    suspend fun isLocalOnly(): Boolean = store.data.map { it[KEY_LOCAL_ONLY] ?: true }.first()
    
    suspend fun isOfflineMode(): Boolean = store.data.map { it[KEY_OFFLINE_MODE] ?: false }.first()

    suspend fun setOnboardingCompleted(completed: Boolean) {
        store.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setTheme(theme: String) {
        store.edit { it[KEY_THEME] = theme }
    }

    suspend fun setLastSyncedAt(epochMs: Long) {
        store.edit { it[KEY_LAST_SYNCED_AT] = epochMs }
    }

    suspend fun cacheUser(id: String, firstName: String, email: String) {
        store.edit {
            it[KEY_USER_ID] = id
            it[KEY_USER_FIRST_NAME] = firstName
            it[KEY_USER_EMAIL] = email
        }
    }

    suspend fun setLocalOnlyMode(enabled: Boolean) {
        store.edit { it[KEY_LOCAL_ONLY] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        store.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        store.edit { it[KEY_HAPTIC_FEEDBACK] = enabled }
    }
    
    suspend fun setFontScale(scale: Float) {
        store.edit { it[KEY_FONT_SCALE] = scale }
    }
    
    suspend fun setFontFamily(family: String) {
        store.edit { it[KEY_FONT_FAMILY] = family }
    }
    
    suspend fun setCompactMode(enabled: Boolean) {
        store.edit { it[KEY_COMPACT_MODE] = enabled }
    }
    
    suspend fun setQuietHours(start: Int, end: Int) {
        store.edit { 
            it[KEY_QUIET_HOURS_START] = start
            it[KEY_QUIET_HOURS_END] = end
        }
    }
    
    suspend fun setAiModel(model: String) {
        store.edit { it[KEY_AI_MODEL] = model }
    }
    
    suspend fun setAiContextEnabled(enabled: Boolean) {
        store.edit { it[KEY_AI_CONTEXT_ENABLED] = enabled }
    }
    
    suspend fun setOfflineMode(enabled: Boolean) {
        store.edit { it[KEY_OFFLINE_MODE] = enabled }
    }

    suspend fun setAuthSkipCompleted(completed: Boolean) {
        store.edit { it[KEY_AUTH_SKIP] = completed }
    }

    // ── Pomodoro Goals ─────────────────────────────────────────────────────────

    val pomodoroDailyGoal: Flow<Int> = store.data.map { it[KEY_POMODORO_DAILY_GOAL] ?: 120 }
    val pomodoroWeeklyGoal: Flow<Int> = store.data.map { it[KEY_POMODORO_WEEKLY_GOAL] ?: 600 }

    suspend fun setPomodoroGoals(daily: Int, weekly: Int) {
        store.edit {
            it[KEY_POMODORO_DAILY_GOAL] = daily
            it[KEY_POMODORO_WEEKLY_GOAL] = weekly
        }
    }

    // ── Pomodoro active timer ──────────────────────────────────────────────────

    val pomodoroBackgroundImageUri: Flow<String?> = store.data.map { it[KEY_POMODORO_BACKGROUND_IMAGE_URI] }

    suspend fun setPomodoroBackgroundImageUri(uri: String?) {
        store.edit {
            if (uri != null) it[KEY_POMODORO_BACKGROUND_IMAGE_URI] = uri
            else it.remove(KEY_POMODORO_BACKGROUND_IMAGE_URI)
        }
    }

    val pomodoroSessionId: Flow<String?> = store.data.map { it[KEY_POMODORO_SESSION_ID] }
    val pomodoroType: Flow<String?> = store.data.map { it[KEY_POMODORO_TYPE] }
    val pomodoroTotalSeconds: Flow<Int> = store.data.map { it[KEY_POMODORO_TOTAL_SECONDS] ?: 0 }
    val pomodoroStartEpochMs: Flow<Long> = store.data.map { it[KEY_POMODORO_START_EPOCH_MS] ?: 0L }
    val pomodoroTaskId: Flow<String?> = store.data.map { it[KEY_POMODORO_TASK_ID] }
    val pomodoroTaskTitle: Flow<String?> = store.data.map { it[KEY_POMODORO_TASK_TITLE] }
    val pomodoroCycleIndex: Flow<Int> = store.data.map { it[KEY_POMODORO_CYCLE_INDEX] ?: 0 }
    val pomodoroIsPaused: Flow<Boolean> = store.data.map { it[KEY_POMODORO_IS_PAUSED] ?: false }
    val pomodoroPausedRemainingSeconds: Flow<Int> = store.data.map { it[KEY_POMODORO_PAUSED_REMAINING] ?: 0 }

    suspend fun savePomodoroTimer(
        sessionId: String,
        type: String,
        totalSeconds: Int,
        startEpochMs: Long,
        taskId: String?,
        taskTitle: String?,
        cycleIndex: Int
    ) {
        store.edit {
            it[KEY_POMODORO_SESSION_ID] = sessionId
            it[KEY_POMODORO_TYPE] = type
            it[KEY_POMODORO_TOTAL_SECONDS] = totalSeconds
            it[KEY_POMODORO_START_EPOCH_MS] = startEpochMs
            if (taskId != null) it[KEY_POMODORO_TASK_ID] = taskId else it.remove(KEY_POMODORO_TASK_ID)
            if (taskTitle != null) it[KEY_POMODORO_TASK_TITLE] = taskTitle else it.remove(KEY_POMODORO_TASK_TITLE)
            it[KEY_POMODORO_CYCLE_INDEX] = cycleIndex
            it[KEY_POMODORO_IS_PAUSED] = false
            it.remove(KEY_POMODORO_PAUSED_REMAINING)
        }
    }

    suspend fun savePomodoroPaused(remainingSeconds: Int) {
        store.edit {
            it[KEY_POMODORO_IS_PAUSED] = true
            it[KEY_POMODORO_PAUSED_REMAINING] = remainingSeconds
        }
    }

    suspend fun savePomodoroResumed() {
        store.edit {
            it[KEY_POMODORO_IS_PAUSED] = false
            it.remove(KEY_POMODORO_PAUSED_REMAINING)
        }
    }

    suspend fun clearPomodoroTimer() {
        store.edit {
            it.remove(KEY_POMODORO_SESSION_ID)
            it.remove(KEY_POMODORO_TYPE)
            it.remove(KEY_POMODORO_TOTAL_SECONDS)
            it.remove(KEY_POMODORO_START_EPOCH_MS)
            it.remove(KEY_POMODORO_TASK_ID)
            it.remove(KEY_POMODORO_TASK_TITLE)
            it.remove(KEY_POMODORO_CYCLE_INDEX)
            it.remove(KEY_POMODORO_IS_PAUSED)
            it.remove(KEY_POMODORO_PAUSED_REMAINING)
        }
    }

    suspend fun clearUser() {
        store.edit {
            it.remove(KEY_USER_ID)
            it.remove(KEY_USER_FIRST_NAME)
            it.remove(KEY_USER_EMAIL)
            it.remove(KEY_LAST_SYNCED_AT)
        }
    }

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_FIRST_NAME = stringPreferencesKey("user_first_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_LOCAL_ONLY = booleanPreferencesKey("local_only")
        private val KEY_OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        private val KEY_AUTH_SKIP = booleanPreferencesKey("auth_skip_completed")
        private val KEY_LANGUAGE = stringPreferencesKey("language")

        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val KEY_FONT_SCALE = androidx.datastore.preferences.core.floatPreferencesKey("font_scale")
        private val KEY_FONT_FAMILY = stringPreferencesKey("font_family")
        private val KEY_COMPACT_MODE = booleanPreferencesKey("compact_mode")
        private val KEY_QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        private val KEY_QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        private val KEY_AI_MODEL = stringPreferencesKey("ai_model")
        private val KEY_AI_CONTEXT_ENABLED = booleanPreferencesKey("ai_context_enabled")

        private val KEY_POMODORO_DAILY_GOAL = intPreferencesKey("pomodoro_daily_goal")
        private val KEY_POMODORO_WEEKLY_GOAL = intPreferencesKey("pomodoro_weekly_goal")
        private val KEY_POMODORO_BACKGROUND_IMAGE_URI = stringPreferencesKey("pomodoro_background_image_uri")

        private val KEY_POMODORO_SESSION_ID = stringPreferencesKey("pomodoro_session_id")
        private val KEY_POMODORO_TYPE = stringPreferencesKey("pomodoro_type")
        private val KEY_POMODORO_TOTAL_SECONDS = intPreferencesKey("pomodoro_total_seconds")
        private val KEY_POMODORO_START_EPOCH_MS = longPreferencesKey("pomodoro_start_epoch_ms")
        private val KEY_POMODORO_TASK_ID = stringPreferencesKey("pomodoro_task_id")
        private val KEY_POMODORO_TASK_TITLE = stringPreferencesKey("pomodoro_task_title")
        private val KEY_POMODORO_CYCLE_INDEX = intPreferencesKey("pomodoro_cycle_index")
        private val KEY_POMODORO_IS_PAUSED = booleanPreferencesKey("pomodoro_is_paused")
        private val KEY_POMODORO_PAUSED_REMAINING = intPreferencesKey("pomodoro_paused_remaining")
    }
}