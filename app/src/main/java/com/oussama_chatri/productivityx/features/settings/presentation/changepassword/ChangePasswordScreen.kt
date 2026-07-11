package com.oussama_chatri.productivityx.features.settings.presentation.changepassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.features.settings.presentation.changepassword.event.ChangePasswordUiEvent
import com.oussama_chatri.productivityx.features.settings.presentation.components.PasswordStrengthIndicator
import com.oussama_chatri.productivityx.features.settings.presentation.components.SettingsSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val passwordChangeSuccess = stringResource(R.string.password_change_success)

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar(passwordChangeSuccess)
            onNavigateBack()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ChangePasswordUiEvent.DismissError)
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
                title = { Text(stringResource(R.string.password_change)) },
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
            Spacer(Modifier.height(8.dp))

            SettingsSectionHeader(stringResource(R.string.field_password_current))

            PasswordField(
                value = state.currentPassword,
                onValueChange = { viewModel.onEvent(ChangePasswordUiEvent.CurrentPasswordChanged(it)) },
                label = stringResource(R.string.field_password_current),
                isVisible = state.currentPasswordVisible,
                onToggleVisibility = { viewModel.onEvent(ChangePasswordUiEvent.ToggleCurrentPasswordVisibility) },
                errorMessage = state.currentPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            SettingsSectionHeader(stringResource(R.string.field_password_new))

            PasswordField(
                value = state.newPassword,
                onValueChange = { viewModel.onEvent(ChangePasswordUiEvent.NewPasswordChanged(it)) },
                label = stringResource(R.string.field_password_new),
                isVisible = state.newPasswordVisible,
                onToggleVisibility = { viewModel.onEvent(ChangePasswordUiEvent.ToggleNewPasswordVisibility) },
                errorMessage = state.newPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.newPassword.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                PasswordStrengthIndicator(
                    strength = state.passwordStrength,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            PasswordField(
                value = state.confirmPassword,
                onValueChange = { viewModel.onEvent(ChangePasswordUiEvent.ConfirmPasswordChanged(it)) },
                label = stringResource(R.string.password_confirm_new),
                isVisible = state.confirmPasswordVisible,
                onToggleVisibility = { viewModel.onEvent(ChangePasswordUiEvent.ToggleConfirmPasswordVisibility) },
                errorMessage = state.confirmPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { viewModel.onEvent(ChangePasswordUiEvent.SaveClicked) },
                enabled = state.canSave,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PxColors.Primary,
                    disabledContainerColor = PxColors.OnSurface.copy(alpha = 0.12f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = stringResource(R.string.password_update),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = errorMessage != null,
        supportingText = errorMessage?.let { msg ->
            { Text(msg, color = PxColors.Error) }
        },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        leadingIcon = {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = PxColors.OnSurface.copy(alpha = 0.5f)
            )
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) Icons.Outlined.Visibility
                    else Icons.Outlined.VisibilityOff,
                    contentDescription = stringResource(if (isVisible) R.string.password_hide else R.string.password_show),
                    tint = PxColors.OnSurface.copy(alpha = 0.5f)
                )
            }
        },
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
