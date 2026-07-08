package com.oussama_chatri.productivityx.features.settings.presentation.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Terms & Conditions") },
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
                text = "Terms of Service",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PxColors.OnBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Last updated: July 2026",
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim
            )
            Spacer(Modifier.height(16.dp))

            SectionText("1. Acceptance of Terms")
            BodyText("By accessing or using ProductivityX, you agree to be bound by these Terms of Service. If you do not agree, do not use the app.")

            SectionText("2. Description of Service")
            BodyText("ProductivityX provides a unified workspace including notes, tasks, calendar, focus timer (Pomodoro), and AI assistant features. We reserve the right to modify or discontinue any feature at any time.")

            SectionText("3. User Accounts")
            BodyText("You are responsible for maintaining the confidentiality of your account credentials and for all activities under your account. You must notify us immediately of any unauthorized access.")

            SectionText("4. Acceptable Use")
            BodyText("You agree not to use ProductivityX for any unlawful purpose or in violation of any applicable laws. You may not attempt to disrupt, damage, or gain unauthorized access to our systems.")

            SectionText("5. Data & Privacy")
            BodyText("Your use of ProductivityX is also governed by our Privacy Policy. We take data security seriously and implement industry-standard measures to protect your information.")

            SectionText("6. Intellectual Property")
            BodyText("ProductivityX and its original content, features, and functionality are owned by Oussama Chatri and are protected by applicable copyright and intellectual property laws.")

            SectionText("7. Limitation of Liability")
            BodyText("ProductivityX is provided \"as is\" without warranties of any kind. We shall not be liable for any damages arising from your use of the app.")

            SectionText("8. Changes to Terms")
            BodyText("We reserve the right to update these terms at any time. We will notify users of material changes via the app or email.")

            SectionText("9. Contact")
            BodyText("For questions about these terms, contact us at productivityx7@gmail.com.")

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
