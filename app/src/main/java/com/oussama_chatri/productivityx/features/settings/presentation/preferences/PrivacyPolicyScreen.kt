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
import androidx.compose.ui.text.font.FontWeight
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Privacy Policy") },
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
                text = "Privacy Policy",
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

            SectionText("1. Information We Collect")
            BodyText("We collect information you provide when creating an account, including your name, email address, and profile picture. We also collect data you create within the app such as notes, tasks, events, and focus sessions.")

            SectionText("2. How We Use Your Information")
            BodyText("Your data is used to provide and improve ProductivityX features, sync across devices, and send relevant notifications. We do not sell your personal information to third parties.")

            SectionText("3. Data Storage & Security")
            BodyText("Your data is encrypted in transit and at rest. We use industry-standard security measures to protect your information. You can choose to keep data local-only without cloud sync.")

            SectionText("4. Third-Party Services")
            BodyText("ProductivityX may use third-party services for AI assistance, push notifications, and analytics. These services are contractually bound to protect your data and use it only for the intended purposes.")

            SectionText("5. Your Rights")
            BodyText("You can access, modify, or delete your data at any time through the app settings. You can request a full export of your data or permanently delete your account.")

            SectionText("6. Data Retention")
            BodyText("We retain your data for as long as your account is active. Deleted items are kept in trash for 30 days before permanent deletion. Account deletion removes all associated data within 90 days.")

            SectionText("7. Children's Privacy")
            BodyText("ProductivityX is not intended for users under 13. We do not knowingly collect data from children. If we become aware of such data, we will delete it promptly.")

            SectionText("8. Changes to This Policy")
            BodyText("We may update this Privacy Policy. We will notify you of material changes through the app or via email.")

            SectionText("9. Contact Us")
            BodyText("For privacy-related inquiries, contact us at productivityx7@gmail.com.")

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
