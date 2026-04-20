package com.oussama_chatri.productivityx.features.auth.presentation.screen

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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.OtpInputField
import com.oussama_chatri.productivityx.core.ui.components.PxButton
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.event.VerifyEmailUiEvent
import com.oussama_chatri.productivityx.features.auth.presentation.state.VerifyEmailUiState
import com.oussama_chatri.productivityx.features.auth.presentation.viewmodel.VerifyEmailViewModel

@Composable
fun VerifyEmailScreen(
    email: String,
    onVerifySuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel()
) {
    LaunchedEffect(email) { viewModel.initEmail(email) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onVerifySuccess()
                else -> {}
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    VerifyEmailContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun VerifyEmailContent(
    uiState: VerifyEmailUiState,
    onEvent: (VerifyEmailUiEvent) -> Unit,
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
                    imageVector = Icons.Outlined.ArrowBack,
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Envelope icon in gradient box
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PxColors.Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = PxColors.Primary
                    )
                }

                Text(
                    text = stringResource(R.string.auth_verify_email_title),
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
                    onValueChange = { onEvent(VerifyEmailUiEvent.OtpChanged(it)) }
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
                    onClick = { onEvent(VerifyEmailUiEvent.Verify) },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Resend with countdown
                val canResend = uiState.resendCooldownSeconds == 0 && !uiState.isResending
                TextButton(
                    onClick = { onEvent(VerifyEmailUiEvent.Resend) },
                    enabled = canResend
                ) {
                    val label = if (uiState.resendCooldownSeconds > 0) {
                        val secs = uiState.resendCooldownSeconds.toString().padStart(2, '0')
                        stringResource(R.string.auth_resend_cooldown, "00:$secs")
                    } else {
                        stringResource(R.string.auth_resend_code)
                    }
                    Text(
                        text = label,
                        color = if (canResend) PxColors.Primary else PxColors.OnSurfaceDim,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun VerifyEmailScreenPreview() {
    ProductivityXTheme {
        VerifyEmailContent(
            uiState = VerifyEmailUiState(email = "user@example.com", resendCooldownSeconds = 45),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}