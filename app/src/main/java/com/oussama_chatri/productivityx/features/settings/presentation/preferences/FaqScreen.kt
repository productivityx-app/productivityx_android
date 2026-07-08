package com.oussama_chatri.productivityx.features.settings.presentation.preferences

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("FAQ") },
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

            FaqItem(
                question = "What is ProductivityX?",
                answer = "ProductivityX is a unified workspace app that combines notes, tasks, calendar, a Pomodoro focus timer, and an AI assistant in one place."
            )
            FaqItem(
                question = "Is my data secure?",
                answer = "Yes. Your data is encrypted in transit and at rest. You can also enable local-only mode to keep all data on your device without cloud sync."
            )
            FaqItem(
                question = "How does the AI assistant work?",
                answer = "The AI assistant can see your active tasks, upcoming events, and recent notes to provide context-aware answers. It uses Google Gemini models."
            )
            FaqItem(
                question = "Can I use ProductivityX offline?",
                answer = "Yes. You can enable offline mode in settings. Changes will sync automatically when you reconnect to the internet."
            )
            FaqItem(
                question = "How do I export my data?",
                answer = "Go to Settings > Data & Sync and tap 'Export data'. Your notes, tasks, events, and settings will be saved as an encrypted backup file."
            )
            FaqItem(
                question = "How does the Pomodoro timer work?",
                answer = "The Pomodoro timer alternates between focus sessions and breaks. You can customize durations, auto-start breaks, and link tasks to track focus time per task."
            )
            FaqItem(
                question = "Can I change the app language?",
                answer = "Yes. Go to your profile and tap the language setting. ProductivityX supports multiple languages including English, French, Arabic, Spanish, German, and more."
            )
            FaqItem(
                question = "How do I reset my password?",
                answer = "Tap 'Forgot password' on the login screen. A reset link will be sent to your email address."
            )
            FaqItem(
                question = "What happens when I delete my account?",
                answer = "All your data (notes, tasks, events, conversations) will be permanently deleted. This action cannot be undone."
            )
            FaqItem(
                question = "How do I contact support?",
                answer = "You can reach us at productivityx7@gmail.com or use the 'Send feedback' option in Settings > Help & Support.",
                showDivider = false
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String, showDivider: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 14.dp)
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = PxColors.OnBackground,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Outlined.Remove else Icons.Outlined.Add,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = PxColors.OnSurfaceDim,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Text(
                text = answer,
                style = MaterialTheme.typography.bodySmall,
                color = PxColors.OnSurfaceDim,
                modifier = Modifier.padding(bottom = 12.dp, end = 24.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = PxColors.Outline.copy(alpha = 0.2f))
        }
    }
}
