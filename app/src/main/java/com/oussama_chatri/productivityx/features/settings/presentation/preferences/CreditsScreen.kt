package com.oussama_chatri.productivityx.features.settings.presentation.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = { Text(stringResource(R.string.pref_credits)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PxColors.OnBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = PxColors.OnSurfaceDim,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            HorizontalDivider(color = PxColors.Outline.copy(alpha = 0.3f))

            Spacer(Modifier.height(24.dp))

            CreditSection(stringResource(R.string.credits_role_development), "Oussama Chatri")
            CreditSection(stringResource(R.string.credits_role_design), "Oussama Chatri")
            CreditSection(stringResource(R.string.credits_role_ai_integration), "Google Gemini API")
            CreditSection(stringResource(R.string.credits_role_icons), "Material Design Icons")

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.credits_built_with),
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurfaceDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.credits_copyright),
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CreditSection(role: String, name: String) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall,
            color = PxColors.OnSurfaceDim,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = PxColors.OnBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}
