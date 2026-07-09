package com.oussama_chatri.productivityx.features.notes.presentation.organization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag
import com.oussama_chatri.productivityx.features.notes.presentation.components.NoteTagChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: TagManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingTag by remember { mutableStateOf<Tag?>(null) }
    var deleteTagId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = PxColors.Background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = PxColors.OnSurface)
                    }
                },
                title = { Text("Tags", style = MaterialTheme.typography.titleLarge, color = PxColors.OnBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background),
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Create tag", tint = PxColors.Primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.tags.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No tags yet. Tap + to create one.", color = PxColors.OnSurfaceDim)
            }
        } else {
            // Tag cloud visualization
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                state.tags.forEach { tag ->
                    Surface(
                        onClick = { editingTag = tag },
                        shape = RoundedCornerShape(12.dp),
                        color = PxColors.Surface,
                        modifier = Modifier.fillMaxWidth(0.47f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tagColor = runCatching { Color(android.graphics.Color.parseColor(tag.color)) }
                                .getOrDefault(PxColors.Primary)
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(tagColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tag.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PxColors.OnSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { deleteTagId = tag.id }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Delete", tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Create dialog
    if (showCreateDialog) {
        TagFormDialog(
            title = "Create tag",
            initialName = "",
            initialColor = "#6366F1",
            onConfirm = { name, color ->
                viewModel.createTag(name, color)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Edit dialog
    editingTag?.let { tag ->
        TagFormDialog(
            title = "Edit tag",
            initialName = tag.name,
            initialColor = tag.color,
            onConfirm = { name, color ->
                viewModel.updateTag(tag.id, name, color)
                editingTag = null
            },
            onDismiss = { editingTag = null }
        )
    }

    // Delete confirmation
    deleteTagId?.let { tagId ->
        val tag = state.tags.find { it.id == tagId }
        AlertDialog(
            onDismissRequest = { deleteTagId = null },
            containerColor = PxColors.Surface,
            title = { Text("Delete tag?", color = PxColors.OnBackground) },
            text = {
                Text(
                    "The tag \"${tag?.name}\" will be removed from all notes.",
                    color = PxColors.OnSurfaceDim
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTag(tagId)
                    deleteTagId = null
                }) {
                    Text("Delete", color = PxColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTagId = null }) {
                    Text("Cancel", color = PxColors.OnSurfaceDim)
                }
            }
        )
    }
}

@Composable
private fun TagFormDialog(
    title: String,
    initialName: String,
    initialColor: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var color by rememberSaveable { mutableStateOf(initialColor) }

    val presetColors = listOf(
        "#6366F1", "#EF4444", "#22C55E", "#F59E0B",
        "#3B82F6", "#8B5CF6", "#EC4899", "#14B8A6"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PxColors.Surface,
        title = { Text(title, color = PxColors.OnBackground) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tag name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PxColors.Primary,
                        unfocusedBorderColor = PxColors.Outline,
                        focusedLabelColor = PxColors.Primary,
                        cursorColor = PxColors.Primary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Color", style = MaterialTheme.typography.labelMedium, color = PxColors.OnSurfaceDim)
                Spacer(modifier = Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { preset ->
                        val isSelected = color == preset
                        val presetColor = runCatching { Color(android.graphics.Color.parseColor(preset)) }.getOrDefault(PxColors.Primary)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(presetColor)
                                .clickable { color = preset },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, color) }, enabled = name.isNotBlank()) {
                Text("Save", color = PxColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PxColors.OnSurfaceDim)
            }
        }
    )
}
