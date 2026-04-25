package com.oussama_chatri.productivityx.features.notes.presentation.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.presentation.components.NoteTagChip
import com.oussama_chatri.productivityx.features.notes.presentation.event.TrashUiEvent
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost  = remember { SnackbarHostState() }
    var showEmptyConfirm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHost.showSnackbar(event.message)
                else                    -> {}
            }
        }
    }

    Scaffold(
        modifier       = modifier,
        containerColor = Color(0xFF0F0F14),
        snackbarHost   = { SnackbarHost(snackbarHost) },
        topBar         = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint               = Color(0xFFCCCCD8)
                        )
                    }
                },
                title   = { Text("Trash", style = MaterialTheme.typography.titleLarge, color = Color(0xFFEEEEF5)) },
                colors  = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F14)),
                actions = {
                    if (uiState.notes.isNotEmpty()) {
                        TextButton(onClick = { showEmptyConfirm = true }) {
                            Text("Empty trash", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.notes.isEmpty()) {
            TrashEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns               = StaggeredGridCells.Fixed(2),
                contentPadding        = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing   = 8.dp,
                modifier              = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(uiState.notes, key = { it.id }) { note ->
                    TrashNoteCard(
                        note      = note,
                        onRestore = { viewModel.onEvent(TrashUiEvent.Restore(note.id)) },
                        onDelete  = { viewModel.onEvent(TrashUiEvent.HardDelete(note.id)) }
                    )
                }
            }
        }
    }

    if (showEmptyConfirm) {
        EmptyTrashDialog(
            noteCount = uiState.notes.size,
            onConfirm = {
                showEmptyConfirm = false
                viewModel.onEvent(TrashUiEvent.EmptyTrash)
            },
            onDismiss = { showEmptyConfirm = false }
        )
    }
}

@Composable
private fun TrashNoteCard(
    note: Note,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier       = modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(12.dp),
        color          = Color(0xFF1A1A24),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (note.title.isNotBlank()) {
                Text(
                    text     = note.title,
                    style    = MaterialTheme.typography.titleMedium,
                    color    = Color(0xFFEEEEF5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (note.preview.isNotBlank()) {
                Text(
                    text     = note.preview,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = Color(0xFF888899),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            note.deletedAt?.let { deletedAt ->
                val formatter = java.time.format.DateTimeFormatter
                    .ofPattern("MMM d")
                    .withZone(java.time.ZoneId.systemDefault())
                Text(
                    text  = "Deleted ${formatter.format(deletedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF888899)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = onRestore,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.RestoreFromTrash,
                        contentDescription = "Restore",
                        tint               = Color(0xFF22C55E),
                        modifier           = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick  = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.DeleteForever,
                        contentDescription = "Delete permanently",
                        tint               = Color(0xFFEF4444),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrashEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier              = modifier.padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Outlined.DeleteForever,
            contentDescription = null,
            tint               = Color(0xFF252533),
            modifier           = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text      = "Trash is empty",
            style     = MaterialTheme.typography.bodyLarge,
            color     = Color(0xFFCCCCD8),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text      = "Deleted notes appear here for 30 days",
            style     = MaterialTheme.typography.bodyMedium,
            color     = Color(0xFF888899),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyTrashDialog(
    noteCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF1A1A24),
        title = {
            Text(
                text  = "Empty trash?",
                color = Color(0xFFEEEEF5)
            )
        },
        text = {
            Text(
                text  = "$noteCount ${if (noteCount == 1) "note" else "notes"} will be permanently deleted and cannot be recovered.",
                color = Color(0xFF888899)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Empty trash", color = Color(0xFFEF4444))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF888899))
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun TrashEmptyStatePreview() {
    ProductivityXTheme {
        TrashEmptyState(modifier = Modifier.fillMaxSize())
    }
}
