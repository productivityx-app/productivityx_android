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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = { Text(stringResource(R.string.pref_faq)) },
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
                question = stringResource(R.string.faq_q_what_is),
                answer = stringResource(R.string.faq_a_what_is)
            )
            FaqItem(
                question = stringResource(R.string.faq_q_data_secure),
                answer = stringResource(R.string.faq_a_data_secure)
            )
            FaqItem(
                question = stringResource(R.string.faq_q_ai_work),
                answer = stringResource(R.string.faq_a_ai_work)
            )
            FaqItem(
                question = stringResource(R.string.faq_q_offline),
                answer = stringResource(R.string.faq_a_offline)
            )
            FaqItem(
                question = stringResource(R.string.faq_q_export),
                answer = stringResource(R.string.faq_a_export)
            )
            FaqItem(
                question = stringResource(R.string.faq_q_pomodoro),
                answer = stringResource(R.string.faq_a_pomodoro)
            )
            FaqItem(
                question = stringResource(R.string.faq_q_language),
                answer = stringResource(R.string.faq_a_language)
            )
            FaqItem(
                question = stringResource(R.string.faq_q_password),
                answer = stringResource(R.string.faq_a_password)
            )
            FaqItem(
                question = stringResource(R.string.faq_q_delete_account),
                answer = stringResource(R.string.faq_a_delete_account)
            )
            FaqItem(
                question = stringResource(R.string.faq_q_contact),
                answer = stringResource(R.string.faq_a_contact),
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
                contentDescription = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
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
