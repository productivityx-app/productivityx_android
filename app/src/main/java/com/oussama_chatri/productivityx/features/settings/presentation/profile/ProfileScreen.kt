package com.oussama_chatri.productivityx.features.profile.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.oussama_chatri.productivityx.features.profile.domain.model.ProfileModel
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.oussama_chatri.productivityx.features.profile.presentation.components.AvatarInitials
import com.oussama_chatri.productivityx.features.profile.presentation.components.SettingRow
import com.oussama_chatri.productivityx.features.profile.presentation.components.SettingRowSwitch
import com.oussama_chatri.productivityx.features.profile.presentation.components.SettingsSectionCard
import com.oussama_chatri.productivityx.features.profile.presentation.components.SettingsSectionHeader
import com.oussama_chatri.productivityx.features.profile.presentation.profile.event.ProfileUiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

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
    var showThemePicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val cacheFile = File(context.cacheDir, "productivityx_export.px")
                viewModel.exportToFile(cacheFile)
                context.contentResolver.openOutputStream(it)?.use { out ->
                    cacheFile.inputStream().use { inp -> inp.copyTo(out) }
                }
                cacheFile.delete()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val cacheFile = File(context.cacheDir, "productivityx_import.px")
                context.contentResolver.openInputStream(it)?.use { inp ->
                    cacheFile.outputStream().use { out -> inp.copyTo(out) }
                }
                viewModel.importFromFile(cacheFile)
                cacheFile.delete()
            }
        }
    }

    LaunchedEffect(viewModel.navEffect) {
        viewModel.navEffect.collectLatest { effect ->
            when (effect) {
                ProfileNavEffect.NavigateToLogin -> onSignedOut()
            }
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileUiEvent.DismissSuccess)
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
            title = { Text(stringResource(R.string.auth_sign_out)) },
            text = { Text("You'll need to sign in again to access your workspace.") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    viewModel.onEvent(ProfileUiEvent.SignOutConfirmed)
                }) {
                    Text(stringResource(R.string.auth_sign_out), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showThemePicker) {
        ThemePickerDialog(
            currentTheme = state.currentTheme,
            onSelect = { theme ->
                viewModel.onEvent(ProfileUiEvent.ThemeChanged(theme))
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false },
        )
    }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            currentLanguage = state.currentLanguage,
            onSelect = { lang ->
                viewModel.onEvent(ProfileUiEvent.LanguageChanged(lang))
                showLanguagePicker = false
            },
            onDismiss = { showLanguagePicker = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    if (!state.isLocalOnly && state.profile != null) {
                        IconButton(onClick = onNavigateToEditProfile) {
                            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.cd_edit))
                        }
                    }
                    IconButton(onClick = onNavigateToPreferences) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(8.dp))

            if (state.isLocalOnly) {
                LocalOnlyHero(onNavigateToLogin = onSignedOut)
            } else if (state.isLoading && state.profile == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                ProfileHeader(
                    profile = state.profile,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                )
            }

            Spacer(Modifier.height(24.dp))

            SettingsSectionHeader(stringResource(R.string.profile_section_appearance))
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.Outlined.Palette,
                    label = stringResource(R.string.profile_theme),
                    subtitle = themeDisplayName(state.currentTheme),
                    onClick = { showThemePicker = true },
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(themePrimaryColor(state.currentTheme))
                        )
                    }
                )
                SettingRow(
                    icon = Icons.Outlined.Language,
                    label = stringResource(R.string.field_language),
                    subtitle = languageDisplayName(state.currentLanguage),
                    showDivider = false,
                    onClick = { showLanguagePicker = true },
                    trailing = {
                        Text(
                            text = state.currentLanguage.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                )
            }

            SettingsSectionHeader(stringResource(R.string.profile_section_pomodoro))
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.Outlined.Timer,
                    label = stringResource(R.string.profile_section_pomodoro),
                    subtitle = state.preferences?.let {
                        "${it.pomodoroFocusMinutes}m focus · ${it.pomodoroShortBreakMinutes}m short · ${it.pomodoroLongBreakMinutes}m long"
                    },
                    showDivider = false,
                    onClick = onNavigateToPreferences
                )
            }

            SettingsSectionHeader(stringResource(R.string.profile_section_notifications))
            SettingsSectionCard {
                SettingRowSwitch(
                    icon = Icons.Outlined.NotificationsActive,
                    label = stringResource(R.string.preferences_notify_task_reminders),
                    checked = state.preferences?.notifyTaskReminders ?: true,
                    onCheckedChange = { }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.CalendarMonth,
                    label = stringResource(R.string.preferences_notify_event_reminders),
                    checked = state.preferences?.notifyEventReminders ?: true,
                    onCheckedChange = { }
                )
                SettingRowSwitch(
                    icon = Icons.Outlined.Timer,
                    label = stringResource(R.string.preferences_pomodoro_sound),
                    checked = state.preferences?.notifyPomodoroEnd ?: true,
                    showDivider = false,
                    onCheckedChange = { }
                )
            }

            SettingsSectionHeader(stringResource(R.string.profile_section_ai))
            SettingsSectionCard {
                SettingRowSwitch(
                    icon = Icons.Outlined.AutoAwesome,
                    label = stringResource(R.string.preferences_ai_context),
                    subtitle = stringResource(R.string.preferences_ai_context_desc),
                    checked = state.preferences?.aiContextEnabled ?: true,
                    onCheckedChange = { }
                )
                SettingRow(
                    icon = Icons.Outlined.SmartToy,
                    label = stringResource(R.string.preferences_ai_model),
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

            SettingsSectionHeader(stringResource(R.string.profile_section_data))
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.Outlined.Upload,
                    label = stringResource(R.string.data_export_title),
                    subtitle = stringResource(R.string.data_export_body),
                    showDivider = true,
                    onClick = { exportLauncher.launch("productivityx_backup.px") },
                    trailing = {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                )
                SettingRow(
                    icon = Icons.Outlined.Download,
                    label = "Import data",
                    subtitle = "Restore data from an encrypted file",
                    showDivider = false,
                    onClick = { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                    trailing = {
                        if (state.isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                )
            }

            if (!state.isLocalOnly) {
                SettingsSectionHeader(stringResource(R.string.profile_section_account))
                SettingsSectionCard {
                    SettingRow(
                        icon = Icons.Outlined.Lock,
                        label = stringResource(R.string.password_change),
                        onClick = onNavigateToChangePassword
                    )
                    SettingRow(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        label = stringResource(R.string.auth_sign_out),
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
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LocalOnlyHero(onNavigateToLogin: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 36.dp, horizontal = 24.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Local Mode",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "You're using ProductivityX without an account.\nYour data stays on this device only.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onNavigateToLogin,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                stringResource(R.string.auth_sign_in),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        Spacer(Modifier.height(12.dp))

        FilledTonalButton(
            onClick = onNavigateToLogin,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text(
                stringResource(R.string.auth_create_account),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: ProfileModel?,
    onNavigateToEditProfile: () -> Unit,
) {
    val initials = buildString {
        profile?.firstName?.firstOrNull()?.let { append(it) }
        profile?.lastName?.firstOrNull()?.let { append(it) }
    }
    val avatarUrl = profile?.avatarUrl

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 32.dp, horizontal = 24.dp)
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = stringResource(R.string.cd_profile_picture),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
            )
        } else {
            AvatarInitials(
                initials = initials.ifEmpty { "?" },
                size = 88,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = profile?.fullName ?: "—",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (!profile?.bio.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = profile!!.bio!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "—", label = stringResource(R.string.profile_stats_notes))
            StatItem(value = "—", label = stringResource(R.string.profile_stats_tasks_completed))
            StatItem(value = "—", label = stringResource(R.string.profile_stats_focus_this_week))
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

@Composable
private fun languageEntries(): List<Pair<String, String>> = listOf(
    "en" to stringResource(R.string.language_en),
    "fr" to stringResource(R.string.language_fr),
    "ar" to stringResource(R.string.language_ar),
)

@Composable
private fun languageDisplayName(code: String): String =
    languageEntries().firstOrNull { it.first == code }?.second ?: code

private val allThemeEntries = listOf(
    "DARK"    to "Dark",
    "LIGHT"   to "Light",
    "SYSTEM"  to "System",
    "OCEAN"   to "Ocean",
    "AMBER"   to "Amber",
    "FOREST"  to "Forest",
    "ROSE"    to "Rose",
    "MIDNIGHT" to "Midnight",
)

private val proThemeKeys = setOf("OCEAN", "AMBER", "FOREST", "ROSE", "MIDNIGHT")

private fun themeDisplayName(theme: String): String =
    allThemeEntries.firstOrNull { it.first == theme }?.second ?: theme

private fun themePrimaryColor(theme: String): Color = when (theme) {
    "DARK"    -> Color(0xFF6366F1)
    "LIGHT"   -> Color(0xFF4F46E5)
    "SYSTEM"  -> Color(0xFF6366F1)
    "OCEAN"   -> Color(0xFF06B6D4)
    "AMBER"   -> Color(0xFFF59E0B)
    "FOREST"  -> Color(0xFF22C55E)
    "ROSE"    -> Color(0xFFF43F5E)
    "MIDNIGHT" -> Color(0xFF6366F1)
    else      -> Color(0xFF6366F1)
}

@Composable
fun ThemePickerDialog(
    currentTheme: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.profile_theme),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                allThemeEntries.forEach { (key, label) ->
                    val isSelected = key == currentTheme
                    val isPro = key in proThemeKeys

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(key) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(themePrimaryColor(key))
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isPro) {
                                Text(
                                    "★",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (isPro) {
                                Text(
                                    text = "Pro theme",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
fun LanguagePickerDialog(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.field_language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                languageEntries().forEach { (code, label) ->
                    val isSelected = code == currentLanguage

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(code) }
                            .padding(vertical = 14.dp, horizontal = 4.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        if (isSelected) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
    )
}
