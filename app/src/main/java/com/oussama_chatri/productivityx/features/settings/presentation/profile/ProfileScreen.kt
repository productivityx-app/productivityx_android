package com.oussama_chatri.productivityx.features.profile.presentation.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.oussama_chatri.productivityx.features.profile.presentation.components.AvatarInitials
import com.oussama_chatri.productivityx.features.profile.presentation.components.SelectionChipRow
import com.oussama_chatri.productivityx.features.profile.presentation.components.SettingRow
import com.oussama_chatri.productivityx.features.profile.presentation.components.SettingRowSwitch
import com.oussama_chatri.productivityx.features.profile.presentation.components.SettingsSectionCard
import com.oussama_chatri.productivityx.features.profile.presentation.components.SettingsSectionHeader
import com.oussama_chatri.productivityx.features.profile.presentation.profile.event.ProfileUiEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.navEffect) {
        viewModel.navEffect.collectLatest { effect ->
            when (effect) {
                ProfileNavEffect.NavigateToLogin -> onSignedOut()
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileUiEvent.DismissError)
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign out?") },
            text = { Text("You'll need to sign in again to access your workspace.") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    viewModel.onEvent(ProfileUiEvent.SignOutConfirmed)
                }) {
                    Text("Sign out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading && state.profile == null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(8.dp))

            // Avatar + name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val avatarUrl = state.profile?.avatarUrl
                val initials = buildString {
                    state.profile?.firstName?.firstOrNull()?.let { append(it) }
                    state.profile?.lastName?.firstOrNull()?.let { append(it) }
                }
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = "avatarUrl",
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp).clip(CircleShape)
                    )
                } else {
                    AvatarInitials(initials = initials, size = 80)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.profile?.fullName ?: "—",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.profile?.bio ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Stats strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = "—", label = "Notes")
                StatItem(value = "—", label = "Tasks done")
                StatItem(value = "—", label = "Focus hrs")
            }

            // Appearance
            SettingsSectionHeader("Appearance")
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.Outlined.Palette,
                    label = "Theme",
                    showDivider = false,
                    trailing = {
                        SelectionChipRow(
                            options = listOf(
                                "DARK" to "Dark",
                                "LIGHT" to "Light",
                                "SYSTEM" to "System"
                            ),
                            selected = state.profile?.theme ?: "DARK",
                            onSelect = { }
                        )
                    }
                )
            }

            // Pomodoro shortcut
            SettingsSectionHeader("Pomodoro")
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.Outlined.Timer,
                    label = "Pomodoro settings",
                    subtitle = state.preferences?.let {
                        "${it.pomodoroFocusMinutes}m focus · ${it.pomodoroShortBreakMinutes}m short · ${it.pomodoroLongBreakMinutes}m long"
                    },
                    showDivider = false,
                    onClick = onNavigateToPreferences
                )
            }

            // Notifications
            SettingsSectionHeader("Notifications")
            SettingsSectionCard {
                SettingRowSwitch(
                    icon = Icons.Outlined.NotificationsActive,
                    label = "Task reminders",
                    checked = state.preferences?.notifyTaskReminders ?: true,
                    onCheckedChange = { }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Event reminders",
                    checked = state.preferences?.notifyEventReminders ?: true,
                    onCheckedChange = { }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.Timer,
                    label = "Session end sound",
                    checked = state.preferences?.notifyPomodoroEnd ?: true,
                    showDivider = false,
                    onCheckedChange = { }
                )
            }

            // AI
            SettingsSectionHeader("AI")
            SettingsSectionCard {
                SettingRowSwitch(
                    icon = Icons.Outlined.AutoAwesome,
                    label = "Workspace context",
                    subtitle = "Share tasks, events, and notes with AI",
                    checked = state.preferences?.aiContextEnabled ?: true,
                    onCheckedChange = { }
                )
                SettingRow(
                    icon = Icons.Outlined.SmartToy,
                    label = "AI Model",
                    showDivider = false,
                    trailing = {
                        Text(
                            text = state.preferences?.aiModel ?: "gemini-2.0-flash",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    onClick = onNavigateToPreferences
                )
            }

            // Account
            SettingsSectionHeader("Account")
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.Outlined.Lock,
                    label = "Change password",
                    onClick = onNavigateToChangePassword
                )
                SettingRow(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    label = "Sign out",
                    iconTint = MaterialTheme.colorScheme.error,
                    showDivider = false,
                    onClick = { showSignOutDialog = true },
                    trailing = {
                        if (state.isSigningOut) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
