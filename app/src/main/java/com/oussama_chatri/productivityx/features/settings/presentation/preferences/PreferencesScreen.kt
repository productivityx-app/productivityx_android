package com.oussama_chatri.productivityx.features.settings.presentation.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.HotelClass
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.features.settings.presentation.components.MinuteStepper
import com.oussama_chatri.productivityx.features.settings.presentation.components.SelectionChipRow
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingRow
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingRowSwitch
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingsSectionCard
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingsSectionHeader
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.event.PreferencesUiEvent

private val SurfaceColor = Color(0xFF1A1A24)
private val CardColor = Color(0xFF1E1E2A)
private val Accent = Color(0xFF6366F1)
private val ErrorColor = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(PreferencesUiEvent.DismissError)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = { Text("Settings") },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(end = 16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            }

            val scrollState = rememberScrollState()
            val searchQuery = state.settingsSearchQuery.lowercase()
            val isSearching = searchQuery.isNotEmpty()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.settingsSearchQuery,
                    onValueChange = { viewModel.onEvent(PreferencesUiEvent.SettingsSearchQueryChanged(it)) },
                    placeholder = { Text("Search settings…", color = Color(0xFF888899)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Color(0xFF888899)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Color(0xFF2A2A38),
                        focusedContainerColor = CardColor,
                        unfocusedContainerColor = CardColor,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                if (isSearching) {
                    SettingsSearchResults(state = state, searchQuery = searchQuery, viewModel = viewModel)
                } else {
                    SettingsContent(state = state, viewModel = viewModel)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsSearchResults(
    state: com.oussama_chatri.productivityx.features.settings.presentation.preferences.state.PreferencesUiState,
    searchQuery: String,
    viewModel: PreferencesViewModel
) {
    val results = mutableListOf<Pair<String, () -> Unit>>()

    if ("pomodoro".contains(searchQuery) || "focus".contains(searchQuery) || "timer".contains(searchQuery)) {
        results.add("Pomodoro Timer" to { })
    }
    if ("theme".contains(searchQuery) || "dark".contains(searchQuery) || "appearance".contains(searchQuery)) {
        results.add("Appearance & Theme" to { })
    }
    if ("notification".contains(searchQuery) || "quiet".contains(searchQuery) || "sound".contains(searchQuery)) {
        results.add("Notifications & Quiet Hours" to { })
    }
    if ("sync".contains(searchQuery) || "backup".contains(searchQuery) || "export".contains(searchQuery) || "data".contains(searchQuery)) {
        results.add("Data & Sync" to { })
    }
    if ("ai".contains(searchQuery) || "model".contains(searchQuery) || "smart".contains(searchQuery)) {
        results.add("AI Assistant" to { })
    }
    if ("font".contains(searchQuery) || "size".contains(searchQuery) || "density".contains(searchQuery) || "compact".contains(searchQuery)) {
        results.add("Display & Font Size" to { })
    }
    if ("beta".contains(searchQuery) || "feature".contains(searchQuery) || "experiment".contains(searchQuery)) {
        results.add("Feature Flags (Beta)" to { })
    }
    if ("about".contains(searchQuery) || "version".contains(searchQuery) || "license".contains(searchQuery)) {
        results.add("About" to { })
    }
    if ("help".contains(searchQuery) || "faq".contains(searchQuery) || "contact".contains(searchQuery) || "feedback".contains(searchQuery)) {
        results.add("Help & Support" to { })
    }

    if (results.isEmpty()) {
        Text(
            text = "No results found for \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF888899),
            modifier = Modifier.padding(vertical = 24.dp)
        )
    } else {
        results.forEach { (label, _) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { }
                    .padding(vertical = 14.dp, horizontal = 4.dp)
            ) {
                Icon(Icons.Outlined.Search, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFEEEEF5))
            }
        }
    }
}

@Composable
private fun SettingsContent(
    state: com.oussama_chatri.productivityx.features.settings.presentation.preferences.state.PreferencesUiState,
    viewModel: PreferencesViewModel
) {
    // ── Account ──
    SettingsSectionHeader("Account")
    SettingsSectionCard {
        SettingRow(
            icon = Icons.Outlined.Palette,
            label = "Theme",
            subtitle = state.appTheme.lowercase().replaceFirstChar { it.uppercase() },
            trailing = {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(themeColor(state.appTheme)))
            }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.Fingerprint,
            label = "Local-only mode",
            subtitle = "Keep all data on this device",
            checked = state.localOnlyMode,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.LocalOnlyModeChanged(it)) }
        )
        SettingRow(
            icon = Icons.Outlined.Info,
            label = "Subscription",
            subtitle = "Free plan",
            showDivider = false,
            trailing = {
                Text("Free", style = MaterialTheme.typography.labelMedium, color = Accent)
            }
        )
    }

    // ── Appearance ──
    SettingsSectionHeader("Appearance")
    SettingsSectionCard {
        SettingRow(
            icon = Icons.Outlined.DarkMode,
            label = "Theme",
            trailing = {
                SelectionChipRow(
                    options = listOf("DARK" to "Dark", "LIGHT" to "Light", "SYSTEM" to "System"),
                    selected = state.appTheme,
                    onSelect = { viewModel.onEvent(PreferencesUiEvent.AppThemeChanged(it)) }
                )
            }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Outlined.FontDownload, null, tint = Color(0xFF888899), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Font size", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFEEEEF5))
                Text("${(state.fontScale * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF888899))
            }
            Slider(
                value = state.fontScale,
                onValueChange = { viewModel.onEvent(PreferencesUiEvent.FontScaleChanged(it)) },
                valueRange = 0.7f..1.5f,
                steps = 7,
                modifier = Modifier.width(160.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Accent,
                    activeTrackColor = Accent,
                    inactiveTrackColor = Color(0xFF2A2A38),
                )
            )
        }
        SettingRow(
            icon = Icons.Outlined.ViewAgenda,
            label = "Density",
            showDivider = false,
            trailing = {
                SelectionChipRow(
                    options = listOf("COMPACT" to "Compact", "COMFORTABLE" to "Comfortable", "SPACIOUS" to "Spacious"),
                    selected = state.density,
                    onSelect = { viewModel.onEvent(PreferencesUiEvent.DensityChanged(it)) }
                )
            }
        )
    }

    // ── Notifications ──
    SettingsSectionHeader("Notifications")
    SettingsSectionCard {
        SettingRowSwitch(
            icon = Icons.Outlined.NotificationsActive,
            label = stringResource(R.string.preferences_notify_task_reminders),
            checked = state.notifyTaskReminders,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.NotifyTaskRemindersChanged(it)) }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.CalendarMonth,
            label = stringResource(R.string.preferences_notify_event_reminders),
            checked = state.notifyEventReminders,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.NotifyEventRemindersChanged(it)) }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.Timer,
            label = stringResource(R.string.preferences_notify_pomodoro_end),
            checked = state.notifyPomodoroEnd,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.NotifyPomodoroEndChanged(it)) }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.WbSunny,
            label = stringResource(R.string.preferences_notify_daily_summary),
            checked = state.notifyDailySummary,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.NotifyDailySummaryChanged(it)) }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.Vibration,
            label = "Haptic feedback",
            subtitle = "Vibrate on interactions",
            checked = state.hapticFeedback,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.HapticFeedbackChanged(it)) }
        )
        SettingRow(
            icon = Icons.Outlined.Bedtime,
            label = "Quiet hours",
            showDivider = false,
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    HourChip(state.quietHoursStart, "Start") { viewModel.onEvent(PreferencesUiEvent.QuietHoursStartChanged(it)) }
                    Text("-", color = Color(0xFF888899))
                    HourChip(state.quietHoursEnd, "End") { viewModel.onEvent(PreferencesUiEvent.QuietHoursEndChanged(it)) }
                }
            }
        )
    }

    // ── Data & Sync ──
    SettingsSectionHeader("Data & Sync")
    SettingsSectionCard {
        SettingRowSwitch(
            icon = Icons.Outlined.Sync,
            label = "Auto-sync",
            subtitle = "Automatically sync data when connected",
            checked = state.autoSync,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.AutoSyncChanged(it)) }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.Wifi,
            label = "Offline mode",
            subtitle = "Work without internet connection",
            checked = state.offlineMode,
            showDivider = true,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.OfflineModeChanged(it)) }
        )
        SettingRow(
            icon = Icons.Outlined.FileUpload,
            label = "Export data",
            subtitle = "Save a backup of all your data",
            trailing = { Icon(Icons.Outlined.FileUpload, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) }
        )
        SettingRow(
            icon = Icons.Outlined.FileDownload,
            label = "Import data",
            subtitle = "Restore data from a backup file",
            showDivider = false,
            trailing = { Icon(Icons.Outlined.FileDownload, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) }
        )
    }

    // ── Pomodoro ──
    SettingsSectionHeader(stringResource(R.string.profile_section_pomodoro))
    SettingsSectionCard {
        SettingRow(
            icon = Icons.Outlined.Timer,
            label = stringResource(R.string.preferences_pomodoro_focus),
            showDivider = true,
            trailing = {
                MinuteStepper(
                    value = state.pomodoroFocusMinutes,
                    onDecrement = { viewModel.onEvent(PreferencesUiEvent.FocusMinutesChanged(state.pomodoroFocusMinutes - 1)) },
                    onIncrement = { viewModel.onEvent(PreferencesUiEvent.FocusMinutesChanged(state.pomodoroFocusMinutes + 1)) }
                )
            }
        )
        SettingRow(
            icon = Icons.Outlined.Coffee,
            label = stringResource(R.string.preferences_pomodoro_short_break),
            showDivider = true,
            trailing = {
                MinuteStepper(
                    value = state.pomodoroShortBreakMinutes,
                    onDecrement = { viewModel.onEvent(PreferencesUiEvent.ShortBreakMinutesChanged(state.pomodoroShortBreakMinutes - 1)) },
                    onIncrement = { viewModel.onEvent(PreferencesUiEvent.ShortBreakMinutesChanged(state.pomodoroShortBreakMinutes + 1)) }
                )
            }
        )
        SettingRow(
            icon = Icons.Outlined.HotelClass,
            label = stringResource(R.string.preferences_pomodoro_long_break),
            showDivider = true,
            trailing = {
                MinuteStepper(
                    value = state.pomodoroLongBreakMinutes,
                    onDecrement = { viewModel.onEvent(PreferencesUiEvent.LongBreakMinutesChanged(state.pomodoroLongBreakMinutes - 1)) },
                    onIncrement = { viewModel.onEvent(PreferencesUiEvent.LongBreakMinutesChanged(state.pomodoroLongBreakMinutes + 1)) }
                )
            }
        )
        SettingRow(
            icon = Icons.Outlined.Loop,
            label = stringResource(R.string.preferences_pomodoro_cycles),
            showDivider = true,
            trailing = {
                MinuteStepper(
                    value = state.pomodoroCyclesBeforeLongBreak,
                    onDecrement = { viewModel.onEvent(PreferencesUiEvent.CyclesChanged(state.pomodoroCyclesBeforeLongBreak - 1)) },
                    onIncrement = { viewModel.onEvent(PreferencesUiEvent.CyclesChanged(state.pomodoroCyclesBeforeLongBreak + 1)) },
                    suffix = ""
                )
            }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.PlayCircle,
            label = stringResource(R.string.preferences_pomodoro_auto_start_breaks),
            checked = state.pomodoroAutoStartBreaks,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.AutoStartBreaksChanged(it)) }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.PlayCircle,
            label = stringResource(R.string.preferences_pomodoro_auto_start_focus),
            checked = state.pomodoroAutoStartFocus,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.AutoStartFocusChanged(it)) }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.Notifications,
            label = stringResource(R.string.preferences_pomodoro_sound),
            checked = state.pomodoroSoundEnabled,
            showDivider = false,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.SoundEnabledChanged(it)) }
        )
    }

    // ── Tasks ──
    SettingsSectionHeader(stringResource(R.string.nav_tasks))
    SettingsSectionCard {
        SettingRow(
            icon = Icons.AutoMirrored.Outlined.ViewList,
            label = stringResource(R.string.preferences_default_task_view),
            showDivider = true,
            trailing = {
                SelectionChipRow(
                    options = listOf("LIST" to "List", "KANBAN" to "Kanban"),
                    selected = state.defaultTaskView,
                    onSelect = { viewModel.onEvent(PreferencesUiEvent.DefaultTaskViewChanged(it)) }
                )
            }
        )
        SettingRow(
            icon = Icons.AutoMirrored.Outlined.Sort,
            label = stringResource(R.string.preferences_default_task_sort),
            showDivider = true,
            trailing = {
                SelectionChipRow(
                    options = listOf("DUE_DATE" to "Due", "PRIORITY" to "Priority", "CREATED" to "Created"),
                    selected = state.defaultTaskSort,
                    onSelect = { viewModel.onEvent(PreferencesUiEvent.DefaultTaskSortChanged(it)) }
                )
            }
        )
        SettingRowSwitch(
            icon = Icons.Outlined.CheckCircle,
            label = stringResource(R.string.preferences_show_completed_tasks),
            checked = state.showCompletedTasks,
            showDivider = false,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.ShowCompletedTasksChanged(it)) }
        )
    }

    // ── Calendar ──
    SettingsSectionHeader(stringResource(R.string.nav_calendar))
    SettingsSectionCard {
        SettingRow(
            icon = Icons.Outlined.CalendarMonth,
            label = stringResource(R.string.preferences_default_calendar_view),
            showDivider = true,
            trailing = {
                SelectionChipRow(
                    options = listOf("WEEK" to "Week", "MONTH" to "Month"),
                    selected = state.defaultCalendarView,
                    onSelect = { viewModel.onEvent(PreferencesUiEvent.DefaultCalendarViewChanged(it)) }
                )
            }
        )
        SettingRow(
            icon = Icons.Outlined.ViewWeek,
            label = stringResource(R.string.preferences_week_starts_on),
            showDivider = false,
            trailing = {
                SelectionChipRow(
                    options = listOf("MON" to "Mon", "SUN" to "Sun", "SAT" to "Sat"),
                    selected = state.weekStartsOn,
                    onSelect = { viewModel.onEvent(PreferencesUiEvent.WeekStartsOnChanged(it)) }
                )
            }
        )
    }

    // ── AI ──
    SettingsSectionHeader(stringResource(R.string.profile_section_ai))
    SettingsSectionCard {
        SettingRowSwitch(
            icon = Icons.Outlined.AutoAwesome,
            label = stringResource(R.string.preferences_ai_context),
            subtitle = stringResource(R.string.preferences_ai_context_desc),
            checked = state.aiContextEnabled,
            onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.AiContextEnabledChanged(it)) }
        )
        SettingRow(
            icon = Icons.Outlined.SmartToy,
            label = stringResource(R.string.preferences_ai_model),
            showDivider = false,
            trailing = {
                SelectionChipRow(
                    options = listOf("gemini-2.0-flash" to "Flash", "gemini-2.0-pro" to "Pro"),
                    selected = state.aiModel,
                    onSelect = { viewModel.onEvent(PreferencesUiEvent.AiModelChanged(it)) }
                )
            }
        )
    }

    // ── Feature Flags (Beta) ──
    SettingsSectionHeader("Beta Features")
    SettingsSectionCard {
        state.featureFlags.forEach { (key, enabled) ->
            val label = key.split("_").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            SettingRowSwitch(
                icon = Icons.Outlined.Science,
                label = label,
                subtitle = if (enabled) "Enabled" else "Disabled",
                checked = enabled,
                showDivider = key != state.featureFlags.keys.last(),
                onCheckedChange = { viewModel.onEvent(PreferencesUiEvent.FeatureFlagToggled(key, it)) }
            )
        }
    }

    // ── About ──
    SettingsSectionHeader("About")
    SettingsSectionCard {
        SettingRow(
            icon = Icons.Outlined.Info,
            label = "Version",
            subtitle = "1.2.0 (build 42)",
            trailing = {
                Text("Latest", style = MaterialTheme.typography.labelSmall, color = Color(0xFF22C55E))
            }
        )
        SettingRow(
            icon = Icons.Outlined.DataObject,
            label = "Licenses",
            subtitle = "Open source libraries",
            trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) }
        )
        SettingRow(
            icon = Icons.Outlined.AutoAwesome,
            label = "Credits",
            showDivider = false,
            trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) }
        )
    }

    // ── Help & Support ──
    SettingsSectionHeader("Help & Support")
    SettingsSectionCard {
        SettingRow(
            icon = Icons.AutoMirrored.Outlined.Help,
            label = "FAQ",
            subtitle = "Frequently asked questions",
            trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) }
        )
        SettingRow(
            icon = Icons.Outlined.NotificationsActive,
            label = "Contact us",
            subtitle = "Get in touch with the team",
            trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) }
        )
        SettingRow(
            icon = Icons.Outlined.AutoAwesome,
            label = "Send feedback",
            subtitle = "Help us improve ProductivityX",
            showDivider = false,
            trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF888899), modifier = Modifier.size(20.dp)) }
        )
    }
}

@Composable
private fun HourChip(hour: Int, label: String, onChange: (Int) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A38))
            .clickable { showPicker = true }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "${hour.toString().padStart(2, '0')}:00",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFEEEEF5)
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Select $label hour", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    (0..23).forEach { h ->
                        val isSelected = h == hour
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChange(h); showPicker = false }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onChange(h); showPicker = false },
                                colors = RadioButtonDefaults.colors(selectedColor = Accent)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${h.toString().padStart(2, '0')}:00",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) Color(0xFFEEEEF5) else Color(0xFF888899)
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPicker = false }) { Text("Close") } },
            containerColor = Color(0xFF1A1A24),
            shape = RoundedCornerShape(20.dp),
        )
    }
}

private fun themeColor(theme: String): Color = when (theme) {
    "DARK" -> Color(0xFF6366F1)
    "LIGHT" -> Color(0xFF4F46E5)
    "SYSTEM" -> Color(0xFF6366F1)
    else -> Color(0xFF6366F1)
}
