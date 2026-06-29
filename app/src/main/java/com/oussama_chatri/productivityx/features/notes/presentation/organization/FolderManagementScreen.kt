package com.oussama_chatri.productivityx.features.notes.presentation.organization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderManagementScreen(
    onNavigateBack: () -> Unit,
    onFolderClick: (String) -> Unit = {},
    viewModel: FolderManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingFolder by remember { mutableStateOf<NoteFolder?>(null) }
    var deleteFolderId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = PxColors.OnSurface)
                    }
                },
                title = { Text("Folders", style = MaterialTheme.typography.titleLarge, color = PxColors.OnBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background),
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Create folder", tint = PxColors.Primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.folders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.CreateNewFolder, contentDescription = null, tint = PxColors.SurfaceVariant, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No folders yet", color = PxColors.OnSurfaceDim)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.folders, key = { it.id }) { folder ->
                    FolderCard(
                        folder = folder,
                        onClick = { onFolderClick(folder.id) },
                        onEdit = { editingFolder = folder },
                        onDelete = { deleteFolderId = folder.id }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        FolderFormDialog(
            title = "Create folder",
            initialName = "",
            initialColor = "#6366F1",
            onConfirm = { name, color -> viewModel.createFolder(name, color); showCreateDialog = false },
            onDismiss = { showCreateDialog = false }
        )
    }

    editingFolder?.let { folder ->
        FolderFormDialog(
            title = "Edit folder",
            initialName = folder.name,
            initialColor = folder.color,
            onConfirm = { name, color -> viewModel.updateFolder(folder.id, name, color); editingFolder = null },
            onDismiss = { editingFolder = null }
        )
    }

    deleteFolderId?.let { id ->
        val folder = state.folders.find { it.id == id }
        AlertDialog(
            onDismissRequest = { deleteFolderId = null },
            containerColor = PxColors.Surface,
            title = { Text("Delete folder?", color = PxColors.OnBackground) },
            text = {
                Text(
                    "Notes in \"${folder?.name}\" will not be deleted.",
                    color = PxColors.OnSurfaceDim
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteFolder(id); deleteFolderId = null }) {
                    Text("Delete", color = PxColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteFolderId = null }) {
                    Text("Cancel", color = PxColors.OnSurfaceDim)
                }
            }
        )
    }
}

@Composable
private fun FolderCard(
    folder: NoteFolder,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val folderColor = runCatching { Color(android.graphics.Color.parseColor(folder.color)) }.getOrDefault(PxColors.Primary)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = PxColors.Surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(folderColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Folder, contentDescription = null, tint = folderColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = folder.name, style = MaterialTheme.typography.bodyLarge, color = PxColors.OnSurface)
                Text(
                    text = "${folder.noteCount} ${if (folder.noteCount == 1) "note" else "notes"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PxColors.OnSurfaceDim
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = PxColors.OnSurfaceDim, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = PxColors.Error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun FolderFormDialog(
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
                    label = { Text("Folder name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PxColors.Primary,
                        unfocusedBorderColor = PxColors.Outline,
                        focusedLabelColor = PxColors.Primary,
                        cursorColor = PxColors.Primary
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Color", style = MaterialTheme.typography.labelMedium, color = PxColors.OnSurfaceDim)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
