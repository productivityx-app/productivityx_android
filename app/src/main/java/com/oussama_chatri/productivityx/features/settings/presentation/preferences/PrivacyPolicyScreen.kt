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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.oussama_chatri.productivityx.R
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = { Text(stringResource(R.string.privacy_title)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.privacy_subtitle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PxColors.OnBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.privacy_last_updated),
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim
            )
            Spacer(Modifier.height(16.dp))

            SectionText(stringResource(R.string.privacy_section_1_title))
            BodyText(stringResource(R.string.privacy_section_1_body))

            SectionText(stringResource(R.string.privacy_section_2_title))
            BodyText(stringResource(R.string.privacy_section_2_body))

            SectionText(stringResource(R.string.privacy_section_3_title))
            BodyText(stringResource(R.string.privacy_section_3_body))

            SectionText(stringResource(R.string.privacy_section_4_title))
            BodyText(stringResource(R.string.privacy_section_4_body))

            SectionText(stringResource(R.string.privacy_section_5_title))
            BodyText(stringResource(R.string.privacy_section_5_body))

            SectionText(stringResource(R.string.privacy_section_6_title))
            BodyText(stringResource(R.string.privacy_section_6_body))

            SectionText(stringResource(R.string.privacy_section_7_title))
            BodyText(stringResource(R.string.privacy_section_7_body))

            SectionText(stringResource(R.string.privacy_section_8_title))
            BodyText(stringResource(R.string.privacy_section_8_body))

            SectionText(stringResource(R.string.privacy_section_9_title))
            BodyText(stringResource(R.string.privacy_section_9_body))

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionText(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = PxColors.OnBackground,
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = PxColors.OnSurfaceDim,
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
    )
}
