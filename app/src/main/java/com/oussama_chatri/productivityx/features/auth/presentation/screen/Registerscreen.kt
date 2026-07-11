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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.OtpInputField
import com.oussama_chatri.productivityx.core.ui.components.PasswordStrengthIndicator
import com.oussama_chatri.productivityx.core.ui.components.PxButton
import com.oussama_chatri.productivityx.core.ui.components.PxButtonVariant
import com.oussama_chatri.productivityx.core.ui.components.PxTextField
import com.oussama_chatri.productivityx.core.ui.components.StepIndicator
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.event.RegisterUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.state.RegisterUiState
import com.oussama_chatri.productivityx.features.auth.presentation.viewmodel.RegisterViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onRegisterSuccess()
                else -> {}
            }
        }
    }

    BackHandler(enabled = uiState.currentStep > 0) {
        viewModel.onEvent(RegisterUiEvent.PrevStep)
    }

    if (uiState.isRegistrationComplete) {
        RegistrationCelebration(onContinue = onRegisterSuccess)
    } else {
        RegisterContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateToLogin = onNavigateToLogin,
        )
    }
}

@Composable
private fun RegistrationCelebration(onContinue: () -> Unit) {
    val scaleAnim = remember { Animatable(0f) }
    val confettiParticles = remember { (0 until 20).map { ConfettiParticle() } }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PxColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            confettiParticles.forEach { particle ->
                val x = size.width * particle.x
                val y = size.height * (particle.y + particle.drift)
                val path = Path().apply {
                    moveTo(x, y)
                    lineTo(x + 8.dp.toPx(), y + 8.dp.toPx())
                    lineTo(x - 4.dp.toPx(), y + 12.dp.toPx())
                    close()
                }
                drawPath(
                    path = path,
                    color = particle.color.copy(alpha = particle.alpha),
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
                Text(
                    text = "\u2713",
                    fontSize = 36.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.auth_welcome_aboard),
                    style = MaterialTheme.typography.headlineMedium,
                    color = PxColors.OnBackground,
                    fontWeight = FontWeight.Bold,
                )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.auth_account_created_success),
                style = MaterialTheme.typography.bodyLarge,
                color = PxColors.OnSurfaceDim,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            PxButton(
                text = stringResource(R.string.continue_action),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(0.7f),
            )
        }
    }
}

private data class ConfettiParticle(
    val x: Float = (0..100).random() / 100f,
    val y: Float = (0..100).random() / 100f,
    val drift: Float = (0..50).random() / 100f,
    val alpha: Float = 0.5f + (0..50).random() / 100f,
    val color: Color = listOf(
        PxColors.Primary,
        PxColors.Secondary,
        PxColors.Success,
        PxColors.Warning,
        Color(0xFFF43F5E),
        Color(0xFF06B6D4),
    ).random(),
)

@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    onEvent: (RegisterUiEvent) -> Unit,
    onNavigateToLogin: () -> Unit,
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.currentStep > 0) {
                    IconButton(onClick = { onEvent(RegisterUiEvent.PrevStep) }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = PxColors.OnBackground,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Text(
                    text = when (uiState.currentStep) {
                        0 -> stringResource(R.string.auth_create_account)
                        1 -> stringResource(R.string.register_step_profile)
                        2 -> stringResource(R.string.register_step_preferences)
                        3 -> stringResource(R.string.register_step_verify)
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = PxColors.OnBackground,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.currentStep < 3) {
                StepIndicator(
                    totalSteps = 3,
                    currentStep = uiState.currentStep,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
                        ) { it } + fadeIn(spring())) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(200),
                                ) { -it / 3 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
                        ) { -it } + fadeIn(spring())) togetherWith
                                (slideOutHorizontally(
                                    animationSpec = tween(200),
                                ) { it / 3 } + fadeOut(tween(200)))
                    }
                },
                label = "registerStep",
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PxColors.Surface)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    when (step) {
                        0 -> Step0Account(uiState, onEvent)
                        1 -> Step1Profile(uiState, onEvent)
                        2 -> Step2Preferences(uiState, onEvent)
                        3 -> Step3VerifyOtp(uiState, onEvent)
                    }

                    // Error display
                    AnimatedVisibility(
                        visible = uiState.generalError != null,
                        enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
                    ) {
                        Text(
                            text = uiState.generalError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = PxColors.Error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PxColors.Error.copy(alpha = 0.1f))
                                .padding(12.dp),
                        )
                    }

                    if (step < 3) {
                        PxButton(
                            text = if (step == 2) stringResource(R.string.auth_create_account)
                            else stringResource(R.string.next),
                            onClick = { onEvent(RegisterUiEvent.NextStep) },
                            isLoading = uiState.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.currentStep == 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.auth_already_have_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PxColors.OnSurfaceDim,
                    )
                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = stringResource(R.string.auth_sign_in),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PxColors.Primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Step0Account(uiState: RegisterUiState, onEvent: (RegisterUiEvent) -> Unit) {
    PxTextField(
        value = uiState.email,
        onValueChange = { onEvent(RegisterUiEvent.EmailChanged(it)) },
        label = stringResource(R.string.field_email),
        placeholder = stringResource(R.string.field_email_hint),
        leadingIcon = Icons.Outlined.Email,
        isError = uiState.emailError != null,
        errorMessage = uiState.emailError,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next,
    )

    PxTextField(
        value = uiState.password,
        onValueChange = { onEvent(RegisterUiEvent.PasswordChanged(it)) },
        label = stringResource(R.string.field_password),
        placeholder = stringResource(R.string.field_password_hint),
        leadingIcon = Icons.Outlined.Lock,
        isPassword = true,
        isPasswordVisible = uiState.isPasswordVisible,
        onPasswordToggle = { onEvent(RegisterUiEvent.TogglePasswordVisibility) },
        isError = uiState.passwordError != null,
        errorMessage = uiState.passwordError,
        imeAction = ImeAction.Next,
    )

    PasswordStrengthIndicator(password = uiState.password)

    PxTextField(
        value = uiState.confirmPassword,
        onValueChange = { onEvent(RegisterUiEvent.ConfirmPasswordChanged(it)) },
        label = stringResource(R.string.field_password_confirm),
        placeholder = stringResource(R.string.field_password_confirm_hint),
        leadingIcon = Icons.Outlined.Lock,
        isPassword = true,
        isPasswordVisible = uiState.isConfirmPasswordVisible,
        onPasswordToggle = { onEvent(RegisterUiEvent.ToggleConfirmPasswordVisibility) },
        isError = uiState.confirmPasswordError != null,
        errorMessage = uiState.confirmPasswordError,
        imeAction = ImeAction.Done,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1Profile(uiState: RegisterUiState, onEvent: (RegisterUiEvent) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    // Profile photo picker
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PxColors.Primary, PxColors.Secondary),
                    ),
                )
                .clickable { /* open photo picker */ },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = stringResource(R.string.field_avatar),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(R.string.field_avatar),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.register_profile_photo_hint),
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.OnSurfaceDim,
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(color = PxColors.Outline.copy(alpha = 0.2f))
    Spacer(modifier = Modifier.height(8.dp))

    PxTextField(
        value = uiState.firstName,
        onValueChange = { onEvent(RegisterUiEvent.FirstNameChanged(it)) },
        label = stringResource(R.string.field_first_name),
        placeholder = stringResource(R.string.field_first_name_hint),
        leadingIcon = Icons.Outlined.Person,
        isError = uiState.firstNameError != null,
        errorMessage = uiState.firstNameError,
        imeAction = ImeAction.Next,
    )

    PxTextField(
        value = uiState.lastName,
        onValueChange = { onEvent(RegisterUiEvent.LastNameChanged(it)) },
        label = stringResource(R.string.field_last_name),
        placeholder = stringResource(R.string.field_last_name_hint),
        leadingIcon = Icons.Outlined.Person,
        isError = uiState.lastNameError != null,
        errorMessage = uiState.lastNameError,
        imeAction = ImeAction.Next,
    )

    PxTextField(
        value = uiState.birthDate,
        onValueChange = {},
        label = stringResource(R.string.field_birth_date),
        placeholder = stringResource(R.string.field_birth_date_hint),
        leadingIcon = Icons.Outlined.CalendarMonth,
        isError = uiState.birthDateError != null,
        errorMessage = uiState.birthDateError,
        readOnly = true,
        trailingIcon = Icons.Outlined.CalendarMonth,
        onTrailingIconClick = { showDatePicker = true },
    )

    PxTextField(
        value = uiState.username,
        onValueChange = { onEvent(RegisterUiEvent.UsernameChanged(it)) },
        label = "${stringResource(R.string.field_username)} (${stringResource(R.string.optional)})",
        placeholder = stringResource(R.string.field_username_hint),
        leadingIcon = Icons.Outlined.Person,
        imeAction = ImeAction.Next,
    )

    PxTextField(
        value = uiState.phone,
        onValueChange = { onEvent(RegisterUiEvent.PhoneChanged(it)) },
        label = "${stringResource(R.string.field_phone)} (${stringResource(R.string.optional)})",
        placeholder = stringResource(R.string.field_phone_hint),
        leadingIcon = Icons.Outlined.Person,
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Done,
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatted = runCatching {
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.of("UTC"))
                                    .toLocalDate()
                                    .format(dateFormatter)
                            }.getOrNull()
                            if (formatted != null) onEvent(RegisterUiEvent.BirthDateChanged(formatted))
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(text = stringResource(R.string.ok), color = PxColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = PxColors.OnSurfaceDim,
                    )
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun Step2Preferences(uiState: RegisterUiState, onEvent: (RegisterUiEvent) -> Unit) {
    Text(
        text = stringResource(R.string.profile_theme),
        style = MaterialTheme.typography.titleMedium,
        color = PxColors.OnBackground,
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            "DARK" to R.string.theme_dark,
            "LIGHT" to R.string.theme_light,
            "SYSTEM" to R.string.theme_system,
        ).forEach { (theme, labelRes) ->
            val isSelected = uiState.selectedTheme == theme
            PxButton(
                text = stringResource(labelRes),
                onClick = { onEvent(RegisterUiEvent.ThemeSelected(theme)) },
                modifier = Modifier.weight(1f),
                variant = if (isSelected) PxButtonVariant.Primary else PxButtonVariant.Outlined,
            )
        }
    }

    Text(
        text = stringResource(R.string.profile_theme_hint),
        style = MaterialTheme.typography.bodySmall,
        color = PxColors.OnSurfaceDim,
    )
}

@Composable
private fun Step3VerifyOtp(uiState: RegisterUiState, onEvent: (RegisterUiEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PxColors.Primary.copy(alpha = 0.12f)),
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
            text = stringResource(R.string.auth_verify_email_title),
            style = MaterialTheme.typography.titleLarge,
            color = PxColors.OnBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.auth_verify_email_body, uiState.pendingEmail),
            style = MaterialTheme.typography.bodyMedium,
            color = PxColors.OnSurfaceDim,
            textAlign = TextAlign.Center,
        )

        OtpInputField(
            value = uiState.otp,
            onValueChange = { onEvent(RegisterUiEvent.OtpChanged(it)) },
        )

        PxButton(
            text = stringResource(R.string.auth_verify),
            onClick = { onEvent(RegisterUiEvent.VerifyOtp) },
            isLoading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        val cooldown = uiState.resendCooldownSeconds
        TextButton(
            onClick = { onEvent(RegisterUiEvent.ResendOtp) },
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
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun RegisterScreenPreview() {
    ProductivityXTheme {
        RegisterContent(
            uiState = RegisterUiState(),
            onEvent = {},
            onNavigateToLogin = {},
        )
    }
}
