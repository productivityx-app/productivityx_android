package com.oussama_chatri.productivityx.features.settings.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.settings.presentation.components.AvatarInitials
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingRow
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingRowSwitch
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingsSectionCard
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingsSectionHeader
import com.oussama_chatri.productivityx.features.settings.presentation.profile.event.ProfileUiEvent
import com.oussama_chatri.productivityx.features.settings.presentation.profile.state.BadgeItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CoverGradientStart = Color(0xFF6366F1)
private val CoverGradientEnd = Color(0xFF1A1A2E)
private val CoverHeight = 200.dp

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
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteAccountPassword by remember { mutableStateOf("") }
    var showThemePicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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
                    Text(stringResource(R.string.auth_sign_out), color = PxColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = PxColors.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteAccountDialog = false 
                deleteAccountPassword = ""
            },
            title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "This action cannot be undone. All your data will be permanently deleted.",
                        color = PxColors.OnSurfaceDim
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "• All notes, tasks, and events will be removed",
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurfaceDim
                    )
                    Text(
                        "• Your account and subscription will be cancelled",
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurfaceDim
                    )
                    Text(
                        "• This cannot be reversed",
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.Error
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = deleteAccountPassword,
                        onValueChange = { deleteAccountPassword = it },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (deleteAccountPassword.isNotBlank()) {
                            showDeleteAccountDialog = false
                            viewModel.onEvent(ProfileUiEvent.DeleteAccountConfirmed(deleteAccountPassword))
                            deleteAccountPassword = ""
                        }
                    },
                    enabled = deleteAccountPassword.isNotBlank()
                ) {
                    Text("Delete permanently", color = if (deleteAccountPassword.isNotBlank()) PxColors.Error else PxColors.OnSurfaceDim)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteAccountDialog = false 
                    deleteAccountPassword = ""
                }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = PxColors.Surface,
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

    val scrollOffset = scrollState.value.toFloat()

    Scaffold(
        containerColor = PxColors.Background,
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
                    titleContentColor = PxColors.OnBackground,
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            if (state.isLocalOnly) {
                Spacer(Modifier.height(8.dp))
                LocalOnlyHero(onNavigateToLogin = onSignedOut)
            } else if (state.isLoading && state.profile == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PxColors.Primary)
                }
            } else {
                CoverPhotoHeader(
                    profile = state.profile,
                    username = state.username,
                    statsTasks = state.tasksCompleted,
                    statsFocus = state.focusHours,
                    statsNotes = state.notesCreated,
                    statsAi = state.aiConversations,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                )
            }

            Spacer(Modifier.height(24.dp))

            if (!state.isLocalOnly) {
                ActivitySection(
                    recentActivity = state.recentActivity,
                    achievementBadges = state.achievementBadges,
                    productivityTrend = state.productivityTrend,
                )

                Spacer(Modifier.height(20.dp))
            }

            SettingsSectionHeader("Actions")
            SettingsSectionCard {
                if (!state.isLocalOnly) {
                    SettingRow(
                        icon = Icons.Outlined.Person,
                        label = stringResource(R.string.profile_edit_title),
                        onClick = onNavigateToEditProfile,
                        trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(20.dp)) }
                    )
                    SettingRow(
                        icon = Icons.Outlined.Lock,
                        label = stringResource(R.string.password_change),
                        onClick = onNavigateToChangePassword,
                        trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(20.dp)) }
                    )
                }
                SettingRow(
                    icon = Icons.Outlined.NotificationsActive,
                    label = "Notification preferences",
                    onClick = onNavigateToPreferences,
                    trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(20.dp)) }
                )
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
                    showDivider = if (state.isLocalOnly) false else true,
                    onClick = { showLanguagePicker = true },
                    trailing = {
                        Text(
                            text = state.currentLanguage.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = PxColors.OnSurface.copy(alpha = 0.5f),
                        )
                    }
                )
                if (!state.isLocalOnly) {
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
                                color = PxColors.OnSurface.copy(alpha = 0.5f)
                            )
                        },
                        onClick = onNavigateToPreferences
                    )
                }
            }

            SettingsSectionHeader("Data & Sync")
            SettingsSectionCard {
                SettingRow(
                    icon = Icons.Outlined.Upload,
                    label = stringResource(R.string.data_export_title),
                    subtitle = stringResource(R.string.data_export_body),
                    onClick = { exportLauncher.launch("productivityx_backup.px") },
                    trailing = {
                        if (state.isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
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
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    }
                )
            }

            if (!state.isLocalOnly) {
                SettingsSectionHeader("Account & Subscription")
                SettingsSectionCard {
                    SettingRow(
                        icon = Icons.Outlined.WorkspacePremium,
                        label = "Subscription",
                        subtitle = "${state.subscriptionStatus} plan",
                        trailing = {
                            Text(
                                text = state.subscriptionStatus,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (state.subscriptionStatus == "Pro") PxColors.Primary else PxColors.OnSurfaceDim
                            )
                        }
                    )
                    SettingRow(
                        icon = Icons.Outlined.Storage,
                        label = "Storage",
                        subtitle = "${state.storageUsedMb} MB of ${state.storageTotalMb} MB used",
                        trailing = {
                            LinearProgressIndicator(
                                progress = { state.storageUsedMb.toFloat() / state.storageTotalMb.toFloat().coerceAtLeast(1f) },
                                modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (state.storageUsedMb.toFloat() / state.storageTotalMb > 0.8f) PxColors.Error else PxColors.Primary,
                                trackColor = PxColors.OnSurface.copy(alpha = 0.1f),
                            )
                        }
                    )
                    SettingRow(
                        icon = Icons.Outlined.Devices,
                        label = "Connected devices",
                        subtitle = "${state.connectedDevices} device(s)",
                        showDivider = true,
                    )
                }

                SettingsSectionHeader("Danger Zone")
                SettingsSectionCard {
                    SettingRow(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        label = stringResource(R.string.auth_sign_out),
                        iconTint = PxColors.Error,
                        onClick = { showSignOutDialog = true },
                        trailing = {
                            if (state.isSigningOut) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = PxColors.Error)
                            }
                        }
                    )
                    SettingRow(
                        icon = Icons.Outlined.DeleteForever,
                        label = "Delete account",
                        subtitle = "Permanently remove all data",
                        iconTint = PxColors.Error,
                        showDivider = false,
                        onClick = { showDeleteAccountDialog = true },
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CoverPhotoHeader(
    profile: com.oussama_chatri.productivityx.features.settings.domain.model.ProfileModel?,
    username: String,
    statsTasks: Int,
    statsFocus: Int,
    statsNotes: Int,
    statsAi: Int,
    onNavigateToEditProfile: () -> Unit,
) {
    val initials = buildString {
        profile?.firstName?.firstOrNull()?.let { append(it) }
        profile?.lastName?.firstOrNull()?.let { append(it) }
    }
    val avatarUrl = profile?.avatarUrl
    val memberSince = profile?.updatedAt?.let { raw ->
        try {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = fmt.parse(raw.take(19))
            val out = SimpleDateFormat("MMM yyyy", Locale.US)
            "Member since ${date?.let { out.format(it) } ?: "—"}"
        } catch (_: Exception) { null }
    } ?: "Member since —"

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CoverHeight)
                .background(
                    Brush.verticalGradient(listOf(CoverGradientStart, CoverGradientEnd))
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-48).dp)
                .zIndex(10f)
                .padding(horizontal = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = stringResource(R.string.cd_profile_picture),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(PxColors.Surface, CircleShape)
                        )
                    } else {
                        AvatarInitials(initials = initials.ifEmpty { "?" }, size = 96)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(PxColors.Primary)
                            .clickable { onNavigateToEditProfile() }
                    ) {
                        Icon(
                            Icons.Outlined.PhotoCamera,
                            contentDescription = "Change photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = profile?.fullName ?: "—",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PxColors.OnBackground,
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.Primary.copy(alpha = 0.8f),
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = memberSince,
                    style = MaterialTheme.typography.labelSmall,
                    color = PxColors.OnSurfaceDim,
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PxColors.Surface.copy(alpha = 0.6f))
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(value = statsNotes.toString(), label = "Notes")
                    StatItem(value = "$statsTasks", label = "Done")
                    StatItem(value = "${statsFocus}h", label = "Focus")
                    StatItem(value = "$statsAi", label = "AI chats")
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = PxColors.OnSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PxColors.OnSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ActivitySection(
    recentActivity: List<com.oussama_chatri.productivityx.features.settings.presentation.profile.state.ActivityItem>,
    achievementBadges: List<BadgeItem>,
    productivityTrend: Float,
) {
    SettingsSectionHeader("Activity")
    SettingsSectionCard {
        Text(
            text = "Recent activity",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PxColors.OnSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
        )
        recentActivity.take(3).forEach { item ->
            val activityIcon: ImageVector = when (item.type) {
                "task" -> Icons.Outlined.CheckCircle
                "note" -> Icons.Outlined.Edit
                "event" -> Icons.Outlined.CalendarMonth
                "pomo" -> Icons.Outlined.AccessTime
                else -> Icons.Outlined.AutoAwesome
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PxColors.Primary.copy(alpha = 0.5f))
                )
                Spacer(Modifier.width(12.dp))
                Icon(activityIcon, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        HorizontalDivider(color = PxColors.OnSurface.copy(alpha = 0.06f), modifier = Modifier.padding(start = 16.dp, end = 16.dp))

        Text(
            text = "Achievements",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PxColors.OnSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            achievementBadges.take(5).forEach { badge ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (badge.unlocked) PxColors.Primary.copy(alpha = 0.2f)
                                else PxColors.OnSurface.copy(alpha = 0.08f)
                            )
                    ) {
                        Icon(
                            Icons.Outlined.EmojiEvents,
                            contentDescription = badge.label,
                            tint = if (badge.unlocked) PxColors.Primary else PxColors.OnSurface.copy(alpha = 0.25f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = badge.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (badge.unlocked) PxColors.OnSurface else PxColors.OnSurface.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        HorizontalDivider(color = PxColors.OnSurface.copy(alpha = 0.06f), modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Outlined.TrendingUp, null, tint = if (productivityTrend > 0.4f) Color(0xFF22C55E) else PxColors.OnSurfaceDim, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Productivity trend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurface
                )
                Text(
                    text = "${(productivityTrend * 100).toInt()}% this week",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (productivityTrend > 0.4f) Color(0xFF22C55E) else PxColors.OnSurfaceDim
                )
            }
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
            .background(PxColors.SurfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 36.dp, horizontal = 24.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PxColors.SurfaceVariant)
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = PxColors.OnSurfaceDim,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Local Mode",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PxColors.OnBackground,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "You're using ProductivityX without an account.\nYour data stays on this device only.",
            style = MaterialTheme.typography.bodyMedium,
            color = PxColors.OnSurfaceDim,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onNavigateToLogin,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PxColors.Primary),
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text(stringResource(R.string.auth_sign_in), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
        }

        Spacer(Modifier.height(12.dp))

        FilledTonalButton(
            onClick = onNavigateToLogin,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text(stringResource(R.string.auth_create_account), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun languageEntries(): List<Pair<String, String>> = listOf(
    "en" to stringResource(R.string.language_en),
    "fr" to stringResource(R.string.language_fr),
    "ar" to stringResource(R.string.language_ar),
    "es" to stringResource(R.string.language_es),
    "de" to stringResource(R.string.language_de),
    "ko" to stringResource(R.string.language_ko),
    "ja" to stringResource(R.string.language_ja),
    "pt" to stringResource(R.string.language_pt),
    "zh-TW" to stringResource(R.string.language_zh),
    "hi" to stringResource(R.string.language_hi),
    "id" to stringResource(R.string.language_id),
)

@Composable
private fun languageDisplayName(code: String): String =
    languageEntries().firstOrNull { it.first == code }?.second ?: code

private val allThemeEntries = listOf(
    "DARK"    to "Dark",
    "LIGHT"   to "Light",
    "SYSTEM"  to "System",
)

private val proThemeKeys = setOf<String>()

private fun themeDisplayName(theme: String): String =
    allThemeEntries.firstOrNull { it.first == theme }?.second ?: theme

private fun themePrimaryColor(theme: String): Color = when (theme) {
    "DARK"    -> Color(0xFF6366F1)
    "LIGHT"   -> Color(0xFF4F46E5)
    "SYSTEM"  -> Color(0xFF6366F1)
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
            Text(stringResource(R.string.profile_theme), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                                    if (isSelected) Modifier.clip(CircleShape).background(PxColors.OnSurface.copy(alpha = 0.2f))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isPro) {
                                Text("★", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = PxColors.OnSurface)
                            if (isPro) {
                                Text(text = "Pro theme", style = MaterialTheme.typography.labelSmall, color = PxColors.Primary)
                            }
                        }

                        if (isSelected) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = PxColors.Primary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) }
        },
        containerColor = PxColors.Surface,
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
            Text(stringResource(R.string.field_language), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), color = PxColors.OnSurface)

                        if (isSelected) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = PxColors.Primary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.done)) }
        },
        containerColor = PxColors.Surface,
        shape = RoundedCornerShape(20.dp),
    )
}
