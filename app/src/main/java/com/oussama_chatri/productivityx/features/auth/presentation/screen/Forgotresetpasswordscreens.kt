package com.oussama_chatri.productivityx.features.auth.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

private data class EmailProvider(val name: String, val icon: String)

private val emailProviders = listOf(
    EmailProvider("Gmail", "G"),
    EmailProvider("Outlook", "O"),
    EmailProvider("Yahoo", "Y"),
)

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
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PxColors.Background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = {
                    if (uiState.showOtpStep) {
                        onEvent(ForgotPasswordUiEvent.GoBackToEmail)
                    } else {
                        onNavigateBack()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = PxColors.OnBackground,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = uiState.showOtpStep,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
                        ) { it } + fadeIn(spring())) togetherWith
                                (slideOutHorizontally(tween(200)) { -it / 3 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
                        ) { -it } + fadeIn(spring())) togetherWith
                                (slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(200)))
                    }
                },
                label = "forgotStep",
            ) { showOtp ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PxColors.Surface)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
    onEvent: (ForgotPasswordUiEvent) -> Unit,
) {
    var showEnvelope by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (uiState.showOtpStep) showEnvelope = true
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PxColors.Primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = PxColors.Primary,
        )
    }

    Text(
        text = stringResource(R.string.auth_forgot_password_title),
        style = MaterialTheme.typography.titleLarge,
        color = PxColors.OnBackground,
        textAlign = TextAlign.Center,
    )

    Text(
        text = stringResource(R.string.auth_forgot_password_body),
        style = MaterialTheme.typography.bodyMedium,
        color = PxColors.OnSurfaceDim,
        textAlign = TextAlign.Center,
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
        onDone = { onEvent(ForgotPasswordUiEvent.Submit) },
    )

    AnimatedVisibility(
        visible = uiState.error != null,
        enter = fadeIn(tween(200)) + scaleIn(tween(200)),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
    ) {
        Text(
            text = uiState.error ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.Error,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PxColors.Error.copy(alpha = 0.1f))
                .padding(12.dp),
        )
    }

    PxButton(
        text = stringResource(R.string.submit),
        onClick = { onEvent(ForgotPasswordUiEvent.Submit) },
        isLoading = uiState.isLoading,
        modifier = Modifier.fillMaxWidth(),
    )

    // Animated envelope on success
    AnimatedVisibility(
        visible = showEnvelope && uiState.showOtpStep,
        enter = fadeIn(tween(400)) + scaleIn(tween(400)),
        exit = fadeOut(tween(200)),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = PxColors.Outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Open your email to get the code",
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email provider quick links
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                emailProviders.forEach { provider ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PxColors.SurfaceVariant)
                            .clickable { /* open email provider */ }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = provider.icon,
                            style = MaterialTheme.typography.titleMedium,
                            color = PxColors.Primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpStep(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordUiEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PxColors.Primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = PxColors.Primary,
        )
    }

    Text(
        text = stringResource(R.string.auth_check_your_email),
        style = MaterialTheme.typography.titleLarge,
        color = PxColors.OnBackground,
        textAlign = TextAlign.Center,
    )

    Text(
        text = stringResource(R.string.auth_verify_email_body, uiState.email),
        style = MaterialTheme.typography.bodyMedium,
        color = PxColors.OnSurfaceDim,
        textAlign = TextAlign.Center,
    )

    OtpInputField(
        value = uiState.otp,
        onValueChange = { onEvent(ForgotPasswordUiEvent.OtpChanged(it)) },
    )

    AnimatedVisibility(
        visible = uiState.error != null,
        enter = fadeIn(tween(200)) + scaleIn(tween(200)),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
    ) {
        Text(
            text = uiState.error ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.Error,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PxColors.Error.copy(alpha = 0.1f))
                .padding(12.dp),
        )
    }

    PxButton(
        text = stringResource(R.string.auth_verify),
        onClick = { onEvent(ForgotPasswordUiEvent.VerifyOtp) },
        isLoading = uiState.isLoading,
        modifier = Modifier.fillMaxWidth(),
    )

    val cooldown = uiState.resendCooldownSeconds
    TextButton(
        onClick = { onEvent(ForgotPasswordUiEvent.ResendOtp) },
        enabled = cooldown == 0 && !uiState.isResending,
    ) {
        Text(
            text = if (cooldown > 0)
                stringResource(R.string.auth_resend_cooldown, cooldown)
            else
                stringResource(R.string.auth_resend_code),
            color = if (cooldown > 0) PxColors.OnSurfaceDim else PxColors.Primary,
            style = MaterialTheme.typography.labelMedium,
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
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun ResetPasswordContent(
    uiState: ResetPasswordUiState,
    onEvent: (ResetPasswordUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    if (uiState.isSuccess) {
        ResetSuccessAnimation(onSignIn = onNavigateBack)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PxColors.Background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = PxColors.OnBackground,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PxColors.Surface)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Security indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PxColors.Success),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Secure reset",
                        style = MaterialTheme.typography.labelMedium,
                        color = PxColors.Success,
                    )
                }

                Text(
                    text = stringResource(R.string.auth_reset_password_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = PxColors.OnBackground,
                )

                Text(
                    text = stringResource(R.string.auth_reset_password_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurfaceDim,
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
                    trailingIcon = if (uiState.isPasswordVisible) Icons.Outlined.VisibilityOff
                    else Icons.Outlined.Visibility,
                    onTrailingIconClick = { onEvent(ResetPasswordUiEvent.TogglePasswordVisibility) },
                    isError = uiState.newPasswordError != null,
                    errorMessage = uiState.newPasswordError,
                    imeAction = ImeAction.Next,
                )

                PasswordStrengthIndicator(password = uiState.newPassword)

                // Requirements checklist
                PasswordRequirements(password = uiState.newPassword)

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
                    onDone = { onEvent(ResetPasswordUiEvent.Submit) },
                )

                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                    exit = fadeOut(tween(200)) + scaleOut(tween(200)),
                ) {
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.Error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PxColors.Error.copy(alpha = 0.1f))
                            .padding(12.dp),
                    )
                }

                PxButton(
                    text = stringResource(R.string.auth_reset_password_action),
                    onClick = { onEvent(ResetPasswordUiEvent.Submit) },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PasswordRequirements(password: String) {
    val requirements = listOf(
        "At least 8 characters" to (password.length >= 8),
        "Uppercase & lowercase" to (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }),
        "At least one number" to (password.any { it.isDigit() }),
        "At least one symbol" to (password.any { !it.isLetterOrDigit() }),
    )

    Column {
        Text(
            text = "Password requirements",
            style = MaterialTheme.typography.labelMedium,
            color = PxColors.OnSurfaceDim,
        )
        Spacer(modifier = Modifier.height(4.dp))
        requirements.forEach { (text, met) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp),
            ) {
                val checkColor = if (met) PxColors.Success else PxColors.OnSurfaceDim.copy(alpha = 0.5f)
                Text(
                    text = if (met) "\u2713" else "\u2022",
                    style = MaterialTheme.typography.bodySmall,
                    color = checkColor,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (met) PxColors.OnSurface else PxColors.OnSurfaceDim.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun ResetSuccessAnimation(onSignIn: () -> Unit) {
    val scaleAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PxColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        // Confetti particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val particles = listOf(
                Offset(size.width * 0.2f, size.height * 0.1f),
                Offset(size.width * 0.5f, size.height * 0.05f),
                Offset(size.width * 0.8f, size.height * 0.15f),
                Offset(size.width * 0.3f, size.height * 0.2f),
                Offset(size.width * 0.7f, size.height * 0.08f),
                Offset(size.width * 0.4f, size.height * 0.12f),
                Offset(size.width * 0.6f, size.height * 0.18f),
                Offset(size.width * 0.9f, size.height * 0.1f),
            )
            particles.forEach { pos ->
                val path = Path().apply {
                    moveTo(pos.x, pos.y)
                    lineTo(pos.x + 10.dp.toPx(), pos.y + 6.dp.toPx())
                    lineTo(pos.x + 5.dp.toPx(), pos.y + 14.dp.toPx())
                    close()
                }
                drawPath(
                    path = path,
                    color = listOf(
                        PxColors.Primary,
                        PxColors.Secondary,
                        PxColors.Success,
                        PxColors.Warning,
                    ).random().copy(alpha = 0.6f),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scaleAnim.value),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PxColors.Success, PxColors.Success.copy(alpha = 0.5f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.auth_reset_password_success_title),
                style = MaterialTheme.typography.headlineMedium,
                color = PxColors.OnBackground,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.auth_reset_password_success_body),
                style = MaterialTheme.typography.bodyLarge,
                color = PxColors.OnSurfaceDim,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            PxButton(
                text = stringResource(R.string.auth_sign_in),
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth(0.7f),
            )
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
            onNavigateBack = {},
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
            onNavigateBack = {},
        )
    }
}
