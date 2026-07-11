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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
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
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = { Text(stringResource(R.string.profile_edit_title)) },
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
                                stringResource(R.string.save),
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
                            contentDescription = stringResource(R.string.field_avatar),
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
                            contentDescription = stringResource(R.string.register_change_photo),
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
                    label = stringResource(R.string.field_first_name),
                    errorMessage = state.firstNameError,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.size(12.dp))
                PxSettingsTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.onEvent(EditProfileUiEvent.LastNameChanged(it)) },
                    label = stringResource(R.string.field_last_name),
                    errorMessage = state.lastNameError,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            PxSettingsTextField(
                value = state.bio,
                onValueChange = { viewModel.onEvent(EditProfileUiEvent.BioChanged(it)) },
                label = stringResource(R.string.field_bio),
                errorMessage = state.bioError,
                minLines = 3,
                maxLines = 5,
                supportingText = "${state.bioCharCount}/500",
                modifier = Modifier.fillMaxWidth()
            )

            SettingsSectionHeader(stringResource(R.string.pref_section_appearance))

            Text(
                text = stringResource(R.string.profile_theme),
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChipRow(
                options = listOf("DARK" to stringResource(R.string.theme_dark), "LIGHT" to stringResource(R.string.theme_light), "SYSTEM" to stringResource(R.string.theme_system)),
                selected = state.theme,
                onSelect = { viewModel.onEvent(EditProfileUiEvent.ThemeChanged(it)) }
            )

            SettingsSectionHeader(stringResource(R.string.pref_section_locale))

            Text(
                text = stringResource(R.string.field_language),
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChipRow(
                options = listOf("EN" to stringResource(R.string.language_en), "FR" to stringResource(R.string.language_fr_native), "AR" to stringResource(R.string.language_ar_native)),
                selected = state.language,
                onSelect = { viewModel.onEvent(EditProfileUiEvent.LanguageChanged(it)) }
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.field_timezone),
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChipRow(
                options = listOf(
                    "UTC" to stringResource(R.string.timezone_utc),
                    "America/New_York" to stringResource(R.string.timezone_est),
                    "Europe/Paris" to stringResource(R.string.timezone_cet),
                    "Africa/Algiers" to stringResource(R.string.timezone_cet_plus_1)
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
