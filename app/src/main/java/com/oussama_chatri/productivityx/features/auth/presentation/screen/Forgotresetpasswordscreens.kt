package com.oussama_chatri.productivityx.features.auth.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack  // FIX: was Icons.Outlined.ArrowBack (deprecated)
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.OtpInputField
import com.oussama_chatri.productivityx.core.ui.components.PasswordStrengthIndicator
import com.oussama_chatri.productivityx.core.ui.components.PxButton
import com.oussama_chatri.productivityx.core.ui.components.PxTextField
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.event.ForgotPasswordUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.event.ResetPasswordUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.state.ForgotPasswordUiState
import com.oussama_chatri.productivityx.features.auth.presentation.state.ResetPasswordUiState
import com.oussama_chatri.productivityx.features.auth.presentation.viewmodel.ForgotPasswordViewModel
import com.oussama_chatri.productivityx.features.auth.presentation.viewmodel.ResetPasswordViewModel

// ForgotPasswordScreen

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResetPassword: (token: String) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    val route = event.route
                    if (route is com.oussama_chatri.productivityx.core.ui.navigation.AuthRoute.ResetPassword) {
                        onNavigateToResetPassword(route.token)
                    }
                }
                else -> {}
            }
        }
    }
    BackHandler(enabled = uiState.showOtpStep) {
        viewModel.onEvent(ForgotPasswordUiEvent.GoBackToEmail)
    }

    ForgotPasswordContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PxColors.Background)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = {
                    if (uiState.showOtpStep) {
                        onEvent(ForgotPasswordUiEvent.GoBackToEmail)  // FIX
                    } else {
                        onNavigateBack()
                    }
                }
            ) {
                Icon(
                    // FIX: AutoMirrored variant — no deprecation warning
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = PxColors.OnBackground
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = uiState.showOtpStep,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "forgotStep"
            ) { showOtp ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PxColors.Surface)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showOtp) {
                        OtpStep(uiState = uiState, onEvent = onEvent)
                    } else {
                        EmailStep(uiState = uiState, onEvent = onEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailStep(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordUiEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PxColors.Primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = PxColors.Primary
        )
    }

    Text(
        text = stringResource(R.string.auth_forgot_password_title),
        style = MaterialTheme.typography.titleLarge,
        color = PxColors.OnBackground,
        textAlign = TextAlign.Center
    )

    Text(
        text = stringResource(R.string.auth_forgot_password_body),
        style = MaterialTheme.typography.bodyMedium,
        color = PxColors.OnSurfaceDim,
        textAlign = TextAlign.Center
    )

    PxTextField(
        value = uiState.email,
        onValueChange = { onEvent(ForgotPasswordUiEvent.EmailChanged(it)) },
        label = stringResource(R.string.field_email),
        placeholder = stringResource(R.string.field_email_hint),
        leadingIcon = Icons.Outlined.Email,
        isError = uiState.emailError != null,
        errorMessage = uiState.emailError,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Done,
        onDone = { onEvent(ForgotPasswordUiEvent.Submit) }
    )

    if (uiState.error != null) {
        Text(
            text = uiState.error,
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.Error
        )
    }

    PxButton(
        text = stringResource(R.string.submit),
        onClick = { onEvent(ForgotPasswordUiEvent.Submit) },
        isLoading = uiState.isLoading,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun OtpStep(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordUiEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PxColors.Primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = PxColors.Primary
        )
    }

    Text(
        text = stringResource(R.string.auth_check_your_email),
        style = MaterialTheme.typography.titleLarge,
        color = PxColors.OnBackground,
        textAlign = TextAlign.Center
    )

    Text(
        text = stringResource(R.string.auth_verify_email_body, uiState.email),
        style = MaterialTheme.typography.bodyMedium,
        color = PxColors.OnSurfaceDim,
        textAlign = TextAlign.Center
    )

    OtpInputField(
        value = uiState.otp,
        onValueChange = { onEvent(ForgotPasswordUiEvent.OtpChanged(it)) }
    )

    if (uiState.error != null) {
        Text(
            text = uiState.error,
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.Error,
            textAlign = TextAlign.Center
        )
    }

    PxButton(
        text = stringResource(R.string.auth_verify),
        onClick = { onEvent(ForgotPasswordUiEvent.VerifyOtp) },
        isLoading = uiState.isLoading,
        modifier = Modifier.fillMaxWidth()
    )

    val cooldown = uiState.resendCooldownSeconds
    TextButton(
        onClick = { onEvent(ForgotPasswordUiEvent.ResendOtp) },
        enabled = cooldown == 0 && !uiState.isResending
    ) {
        Text(
            text = if (cooldown > 0)
                stringResource(R.string.auth_resend_cooldown, cooldown)
            else
                stringResource(R.string.auth_resend_code),
            color = if (cooldown > 0) PxColors.OnSurfaceDim else PxColors.Primary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// ResetPasswordScreen

@Composable
fun ResetPasswordScreen(
    token: String,
    onResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    LaunchedEffect(token) { viewModel.initToken(token) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onResetSuccess()
                else -> {}
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    ResetPasswordContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun ResetPasswordContent(
    uiState: ResetPasswordUiState,
    onEvent: (ResetPasswordUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PxColors.Background)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = PxColors.OnBackground
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PxColors.Surface)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isSuccess) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = PxColors.Success
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.auth_reset_password_success_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = PxColors.OnBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.auth_reset_password_success_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PxColors.OnSurfaceDim,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PxButton(
                            text = stringResource(R.string.auth_sign_in),
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.auth_reset_password_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = PxColors.OnBackground
                    )

                    Text(
                        text = stringResource(R.string.auth_reset_password_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PxColors.OnSurfaceDim
                    )

                    PxTextField(
                        value = uiState.newPassword,
                        onValueChange = { onEvent(ResetPasswordUiEvent.NewPasswordChanged(it)) },
                        label = stringResource(R.string.field_password_new),
                        placeholder = stringResource(R.string.field_password_new_hint),
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.isPasswordVisible,
                        onPasswordToggle = { onEvent(ResetPasswordUiEvent.TogglePasswordVisibility) },
                        isError = uiState.newPasswordError != null,
                        errorMessage = uiState.newPasswordError,
                        imeAction = ImeAction.Next
                    )

                    PasswordStrengthIndicator(password = uiState.newPassword)

                    PxTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { onEvent(ResetPasswordUiEvent.ConfirmPasswordChanged(it)) },
                        label = stringResource(R.string.field_password_confirm),
                        placeholder = stringResource(R.string.field_password_confirm_hint),
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.isConfirmPasswordVisible,
                        onPasswordToggle = { onEvent(ResetPasswordUiEvent.ToggleConfirmPasswordVisibility) },
                        isError = uiState.confirmPasswordError != null,
                        errorMessage = uiState.confirmPasswordError,
                        imeAction = ImeAction.Done,
                        onDone = { onEvent(ResetPasswordUiEvent.Submit) }
                    )

                    if (uiState.error != null) {
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = PxColors.Error
                        )
                    }

                    PxButton(
                        text = stringResource(R.string.auth_reset_password_action),
                        onClick = { onEvent(ResetPasswordUiEvent.Submit) },
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun ForgotPasswordEmailPreview() {
    ProductivityXTheme {
        ForgotPasswordContent(
            uiState = ForgotPasswordUiState(),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun ForgotPasswordOtpPreview() {
    ProductivityXTheme {
        ForgotPasswordContent(
            uiState = ForgotPasswordUiState(showOtpStep = true, email = "alex@example.com"),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}