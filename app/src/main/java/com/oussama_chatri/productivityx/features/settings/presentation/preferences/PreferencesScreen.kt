package com.oussama_chatri.productivityx.features.settings.presentation.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.HotelClass
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.features.settings.presentation.components.MinuteStepper
import com.oussama_chatri.productivityx.features.settings.presentation.components.SelectionChipRow
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingRow
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingRowSwitch
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingsSectionCard
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingsSectionHeader
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.PreferencesViewModel
import com.oussama_chatri.productivityx.features.settings.presentation.preferences.event.PreferencesUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = { Text(stringResource(R.string.preferences_title)) },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // ── Pomodoro ──────────────────────────────────────────────
            SettingsSectionHeader(stringResource(R.string.profile_section_pomodoro))
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.Outlined.Timer,
                    label = stringResource(R.string.preferences_pomodoro_focus),
                    showDivider = true,
                    trailing = {
                        MinuteStepper(
                            value = state.pomodoroFocusMinutes,
                            onDecrement = {
                                viewModel.onEvent(
                                    PreferencesUiEvent.FocusMinutesChanged(state.pomodoroFocusMinutes - 1)
                                )
                            },
                            onIncrement = {
                                viewModel.onEvent(
                                    PreferencesUiEvent.FocusMinutesChanged(state.pomodoroFocusMinutes + 1)
                                )
                            }
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
                            onDecrement = {
                                viewModel.onEvent(
                                    PreferencesUiEvent.ShortBreakMinutesChanged(state.pomodoroShortBreakMinutes - 1)
                                )
                            },
                            onIncrement = {
                                viewModel.onEvent(
                                    PreferencesUiEvent.ShortBreakMinutesChanged(state.pomodoroShortBreakMinutes + 1)
                                )
                            }
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
                            onDecrement = {
                                viewModel.onEvent(
                                    PreferencesUiEvent.LongBreakMinutesChanged(state.pomodoroLongBreakMinutes - 1)
                                )
                            },
                            onIncrement = {
                                viewModel.onEvent(
                                    PreferencesUiEvent.LongBreakMinutesChanged(state.pomodoroLongBreakMinutes + 1)
                                )
                            }
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
                            onDecrement = {
                                viewModel.onEvent(
                                    PreferencesUiEvent.CyclesChanged(state.pomodoroCyclesBeforeLongBreak - 1)
                                )
                            },
                            onIncrement = {
                                viewModel.onEvent(
                                    PreferencesUiEvent.CyclesChanged(state.pomodoroCyclesBeforeLongBreak + 1)
                                )
                            },
                            suffix = ""
                        )
                    }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.PlayCircle,
                    label = stringResource(R.string.preferences_pomodoro_auto_start_breaks),
                    checked = state.pomodoroAutoStartBreaks,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.AutoStartBreaksChanged(it))
                    }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.PlayCircle,
                    label = stringResource(R.string.preferences_pomodoro_auto_start_focus),
                    checked = state.pomodoroAutoStartFocus,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.AutoStartFocusChanged(it))
                    }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.Notifications,
                    label = stringResource(R.string.preferences_pomodoro_sound),
                    checked = state.pomodoroSoundEnabled,
                    showDivider = false,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.SoundEnabledChanged(it))
                    }
                )
            }

            // ── Notifications ──────────────────────────────────────────
            SettingsSectionHeader(stringResource(R.string.profile_section_notifications))
            SettingsSectionCard {
                SettingRowSwitch(
                    icon = Icons.Outlined.NotificationsActive,
                    label = stringResource(R.string.preferences_notify_task_reminders),
                    checked = state.notifyTaskReminders,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.NotifyTaskRemindersChanged(it))
                    }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.CalendarMonth,
                    label = stringResource(R.string.preferences_notify_event_reminders),
                    checked = state.notifyEventReminders,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.NotifyEventRemindersChanged(it))
                    }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.Timer,
                    label = stringResource(R.string.preferences_notify_pomodoro_end),
                    checked = state.notifyPomodoroEnd,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.NotifyPomodoroEndChanged(it))
                    }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.WbSunny,
                    label = stringResource(R.string.preferences_notify_daily_summary),
                    checked = state.notifyDailySummary,
                    showDivider = false,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.NotifyDailySummaryChanged(it))
                    }
                )
            }

            // ── Tasks ──────────────────────────────────────────────────
            SettingsSectionHeader(stringResource(R.string.nav_tasks))
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.AutoMirrored.Outlined.ViewList,  // fixed: was Outlined.ViewList
                    label = stringResource(R.string.preferences_default_task_view),
                    showDivider = true,
                    trailing = {
                        SelectionChipRow(
                            options = listOf("LIST" to stringResource(R.string.task_view_list), "KANBAN" to stringResource(R.string.task_view_kanban)),
                            selected = state.defaultTaskView,
                            onSelect = {
                                viewModel.onEvent(PreferencesUiEvent.DefaultTaskViewChanged(it))
                            }
                        )
                    }
                )
                SettingRow(
                    icon = Icons.AutoMirrored.Outlined.Sort,  // fixed: was Outlined.Sort
                    label = stringResource(R.string.preferences_default_task_sort),
                    showDivider = true,
                    trailing = {
                        SelectionChipRow(
                            options = listOf(
                                "DUE_DATE" to stringResource(R.string.tasks_sort_due_date),
                                "PRIORITY" to stringResource(R.string.tasks_sort_priority),
                                "CREATED" to stringResource(R.string.tasks_sort_created)
                            ),
                            selected = state.defaultTaskSort,
                            onSelect = {
                                viewModel.onEvent(PreferencesUiEvent.DefaultTaskSortChanged(it))
                            }
                        )
                    }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.CheckCircle,
                    label = stringResource(R.string.preferences_show_completed_tasks),
                    checked = state.showCompletedTasks,
                    showDivider = false,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.ShowCompletedTasksChanged(it))
                    }
                )
            }

            // ── Calendar ───────────────────────────────────────────────
            SettingsSectionHeader(stringResource(R.string.nav_calendar))
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.Outlined.CalendarMonth,
                    label = stringResource(R.string.preferences_default_calendar_view),
                    showDivider = true,
                    trailing = {
                        SelectionChipRow(
                            options = listOf("WEEK" to stringResource(R.string.calendar_view_week), "MONTH" to stringResource(R.string.calendar_view_month)),
                            selected = state.defaultCalendarView,
                            onSelect = {
                                viewModel.onEvent(PreferencesUiEvent.DefaultCalendarViewChanged(it))
                            }
                        )
                    }
                )
                SettingRow(
                    icon = Icons.Outlined.ViewWeek,
                    label = stringResource(R.string.preferences_week_starts_on),
                    showDivider = false,
                    trailing = {
                        SelectionChipRow(
                            options = listOf("MON" to stringResource(R.string.day_monday_short), "SUN" to stringResource(R.string.day_sunday_short), "SAT" to stringResource(R.string.day_saturday_short)),
                            selected = state.weekStartsOn,
                            onSelect = {
                                viewModel.onEvent(PreferencesUiEvent.WeekStartsOnChanged(it))
                            }
                        )
                    }
                )
            }

            // ── AI ─────────────────────────────────────────────────────
            SettingsSectionHeader(stringResource(R.string.profile_section_ai))
            SettingsSectionCard {
                SettingRowSwitch(
                    icon = Icons.Outlined.AutoAwesome,
                    label = stringResource(R.string.preferences_ai_context),
                    subtitle = stringResource(R.string.preferences_ai_context_desc),
                    checked = state.aiContextEnabled,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.AiContextEnabledChanged(it))
                    }
                )
                SettingRow(
                    icon = Icons.Outlined.SmartToy,
                    label = stringResource(R.string.preferences_ai_model),
                    showDivider = false,
                    trailing = {
                        SelectionChipRow(
                            options = listOf(
                                "gemini-2.0-flash" to "Flash",
                                "gemini-2.0-pro" to "Pro"
                            ),
                            selected = state.aiModel,
                            onSelect = {
                                viewModel.onEvent(PreferencesUiEvent.AiModelChanged(it))
                            }
                        )
                    }
                )
            }

            // ── Privacy ────────────────────────────────────────────────
            SettingsSectionHeader("Privacy")
            SettingsSectionCard {
                SettingRowSwitch(
                    icon = Icons.Outlined.Notifications,
                    label = "Local-only mode",
                    subtitle = "Keep all data stored locally, never sync to cloud",
                    checked = state.localOnlyMode,
                    showDivider = false,
                    onCheckedChange = {
                        viewModel.onEvent(PreferencesUiEvent.LocalOnlyModeChanged(it))
                    }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}