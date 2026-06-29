package com.oussama_chatri.productivityx.features.notes.presentation.organization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateGalleryScreen(
    onNavigateBack: () -> Unit,
    onApplyTemplate: (String) -> Unit = {},
    viewModel: TemplateGalleryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = PxColors.OnSurface)
                    }
                },
                title = { Text("Templates", style = MaterialTheme.typography.titleLarge, color = PxColors.OnBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background)
            )
        }
    ) { innerPadding ->
        val defaultTemplates = listOf(
            NoteTemplate("t1", "", "Meeting Notes", "# Agenda\n- Topic 1\n- Topic 2\n- Action items", "meeting", java.time.Instant.now()),
            NoteTemplate("t2", "", "To-Do List", "- [ ] Task 1\n- [ ] Task 2\n- [ ] Task 3", "checklist", java.time.Instant.now()),
            NoteTemplate("t3", "", "Weekly Review", "## Wins\n- \n\n## Improvements\n- \n\n## Goals\n- ", "review", java.time.Instant.now()),
            NoteTemplate("t4", "", "Brain Dump", "## Ideas\n- \n\n## Notes\n- ", "bulb", java.time.Instant.now()),
            NoteTemplate("t5", "", "Project Plan", "# Overview\n## Timeline\n## Resources\n## Milestones", "project", java.time.Instant.now()),
            NoteTemplate("t6", "", "Journal Entry", "## Date:\n### How am I feeling?\n### What happened today?\n### Gratitude", "journal", java.time.Instant.now())
        )

        val displayTemplates = state.templates + defaultTemplates.filter { t -> state.templates.none { it.name == t.name } }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            items(displayTemplates, key = { it.id }) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onApplyTemplate(template.id) }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: NoteTemplate,
    onClick: () -> Unit
) {
    val iconMap = mapOf(
        "meeting" to Icons.Outlined.MeetingRoom,
        "checklist" to Icons.Outlined.Checklist,
        "review" to Icons.Outlined.ListAlt,
        "bulb" to Icons.Outlined.Lightbulb,
        "project" to Icons.Outlined.NoteAlt,
        "journal" to Icons.Outlined.EditNote,
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PxColors.Surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = iconMap[template.icon] ?: Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = PxColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = template.name,
                style = MaterialTheme.typography.bodyMedium,
                color = PxColors.OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
