package com.oussama_chatri.productivityx.features.auth.presentation.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.PxButton
import com.oussama_chatri.productivityx.core.ui.components.PxTextField
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.event.LoginUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.state.LoginUiState
import com.oussama_chatri.productivityx.features.auth.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToVerifyEmail: (String) -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    val route = event.route
                    when {
                        route is com.oussama_chatri.productivityx.core.ui.navigation.MainRoute.Home ||
                                route is com.oussama_chatri.productivityx.core.ui.navigation.MainGraph -> onLoginSuccess()
                        route is com.oussama_chatri.productivityx.core.ui.navigation.AuthRoute.VerifyEmail ->
                            onNavigateToVerifyEmail(route.email)
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        onSkipLogin = {
            viewModel.onEvent(LoginUiEvent.SkipLogin)
        }
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onSkipLogin: () -> Unit
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PxColors.Primary, PxColors.Secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("PX", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = PxColors.OnBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.auth_welcome_back),
                style = MaterialTheme.typography.headlineMedium,
                color = PxColors.OnBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.auth_sign_in),
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurfaceDim
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PxColors.Surface)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PxTextField(
                    value = uiState.identifier,
                    onValueChange = { onEvent(LoginUiEvent.IdentifierChanged(it)) },
                    label = stringResource(R.string.field_email),
                    placeholder = stringResource(R.string.field_email_hint),
                    leadingIcon = Icons.Outlined.Email,
                    isError = uiState.identifierError != null,
                    errorMessage = uiState.identifierError,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )

                PxTextField(
                    value = uiState.password,
                    onValueChange = { onEvent(LoginUiEvent.PasswordChanged(it)) },
                    label = stringResource(R.string.field_password),
                    placeholder = stringResource(R.string.field_password_hint),
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    isPasswordVisible = uiState.isPasswordVisible,
                    onPasswordToggle = { onEvent(LoginUiEvent.TogglePasswordVisibility) },
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError,
                    imeAction = ImeAction.Done,
                    onDone = { onEvent(LoginUiEvent.Submit) }
                )

                // Forgot password
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = onNavigateToForgotPassword) {
                        Text(
                            text = stringResource(R.string.auth_forgot_password),
                            style = MaterialTheme.typography.labelMedium,
                            color = PxColors.Primary
                        )
                    }
                }

                // General error
                if (uiState.generalError != null) {
                    Text(
                        text = uiState.generalError,
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.Error
                    )
                }

                PxButton(
                    text = stringResource(R.string.auth_sign_in),
                    onClick = { onEvent(LoginUiEvent.Submit) },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Skip login — use app locally without an account
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onSkipLogin, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.auth_skip_login),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurfaceDim
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigate to register
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.auth_dont_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurfaceDim
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = stringResource(R.string.auth_sign_up),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PxColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun LoginScreenPreview() {
    ProductivityXTheme {
        LoginContent(
            uiState = LoginUiState(),
            onEvent = {},
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {},
            onSkipLogin = {}
        )
    }
}