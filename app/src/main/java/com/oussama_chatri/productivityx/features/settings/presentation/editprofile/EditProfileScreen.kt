package com.oussama_chatri.productivityx.features.settings.presentation.editprofile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.oussama_chatri.productivityx.features.settings.presentation.components.AvatarInitials
import com.oussama_chatri.productivityx.features.settings.presentation.components.SelectionChipRow
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingsSectionHeader
import com.oussama_chatri.productivityx.features.settings.presentation.editprofile.event.EditProfileUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(EditProfileUiEvent.AvatarUrlChanged(it.toString()))
        }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) onNavigateBack()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(EditProfileUiEvent.DismissError)
        }
    }

    Scaffold(
        containerColor = PxColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Edit Profile") },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp,
                            color = PxColors.Primary
                        )
                    } else {
                        TextButton(onClick = { viewModel.onEvent(EditProfileUiEvent.SaveClicked) }) {
                            Text(
                                "Save",
                                color = PxColors.Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PxColors.Background
                )
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
            Spacer(Modifier.height(16.dp))

            // Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                val initials = buildString {
                    state.firstName.firstOrNull()?.let { append(it) }
                    state.lastName.firstOrNull()?.let { append(it) }
                }

                Box {
                    if (!state.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = state.avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .border(2.dp, PxColors.Primary, CircleShape)
                        )
                    } else {
                        AvatarInitials(initials = initials.ifEmpty { "?" }, size = 88)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(PxColors.Primary)
                            .clickable { photoPickerLauncher.launch("image/*") }
                    ) {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = "Change photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Name row
            Row(modifier = Modifier.fillMaxWidth()) {
                PxSettingsTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.onEvent(EditProfileUiEvent.FirstNameChanged(it)) },
                    label = "First name",
                    errorMessage = state.firstNameError,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.size(12.dp))
                PxSettingsTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.onEvent(EditProfileUiEvent.LastNameChanged(it)) },
                    label = "Last name",
                    errorMessage = state.lastNameError,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            PxSettingsTextField(
                value = state.bio,
                onValueChange = { viewModel.onEvent(EditProfileUiEvent.BioChanged(it)) },
                label = "Bio",
                errorMessage = state.bioError,
                minLines = 3,
                maxLines = 5,
                supportingText = "${state.bioCharCount}/500",
                modifier = Modifier.fillMaxWidth()
            )

            SettingsSectionHeader("Appearance")

            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChipRow(
                options = listOf("DARK" to "Dark", "LIGHT" to "Light", "SYSTEM" to "System"),
                selected = state.theme,
                onSelect = { viewModel.onEvent(EditProfileUiEvent.ThemeChanged(it)) }
            )

            SettingsSectionHeader("Locale")

            Text(
                text = "Language",
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChipRow(
                options = listOf("EN" to "English", "FR" to "Français", "AR" to "العربية"),
                selected = state.language,
                onSelect = { viewModel.onEvent(EditProfileUiEvent.LanguageChanged(it)) }
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Timezone",
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChipRow(
                options = listOf(
                    "UTC" to "UTC",
                    "America/New_York" to "EST",
                    "Europe/Paris" to "CET",
                    "Africa/Algiers" to "CET+1"
                ),
                selected = state.timezone,
                onSelect = { viewModel.onEvent(EditProfileUiEvent.TimezoneChanged(it)) }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PxSettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    supportingText: String? = null,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = errorMessage != null,
        supportingText = {
            when {
                errorMessage != null -> Text(errorMessage, color = PxColors.Error)
                supportingText != null -> Text(
                    supportingText,
                    color = PxColors.OnSurface.copy(alpha = 0.4f)
                )
            }
        },
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PxColors.Primary,
            unfocusedBorderColor = PxColors.OnSurface.copy(alpha = 0.15f),
            focusedContainerColor = PxColors.SurfaceVariant,
            unfocusedContainerColor = PxColors.SurfaceVariant,
            errorContainerColor = PxColors.SurfaceVariant
        ),
        modifier = modifier
    )
}
