package com.oussama_chatri.productivityx.features.notes.presentation.trash

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.stringResource
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.domain.model.Note
import com.oussama_chatri.productivityx.features.notes.presentation.components.NoteTagChip
import com.oussama_chatri.productivityx.features.notes.presentation.components.relativeTime
import com.oussama_chatri.productivityx.features.notes.presentation.event.TrashUiEvent
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    var showEmptyConfirm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHost.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = PxColors.Background,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = PxColors.OnSurface
                        )
                    }
                },
                title = { Text(stringResource(R.string.nav_trash), style = MaterialTheme.typography.titleLarge, color = PxColors.OnBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background),
                actions = {
                    if (uiState.notes.isNotEmpty()) {
                        TextButton(onClick = { showEmptyConfirm = true }) {
                            Text(stringResource(R.string.notes_trash_empty_all), color = PxColors.Error, style = MaterialTheme.typography.labelMedium)
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
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    Text(
                        text = stringResource(if (uiState.notes.size == 1) R.string.notes_in_trash else R.string.notes_in_trash_plural, uiState.notes.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = PxColors.OnSurfaceDim,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(uiState.notes, key = { it.id }) { note ->
                    TrashNoteCard(
                        note = note,
                        onRestore = { viewModel.onEvent(TrashUiEvent.Restore(note.id)) },
                        onDelete = { viewModel.onEvent(TrashUiEvent.HardDelete(note.id)) }
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
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        color = PxColors.Surface,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PxColors.OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (note.preview.isNotBlank()) {
                Text(
                    text = note.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = PxColors.OnSurfaceDim,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            note.deletedAt?.let { deletedAt ->
                val daysRemaining = 30 - ChronoUnit.DAYS.between(deletedAt, Instant.now()).toInt().coerceAtLeast(0)
                val progress = (30 - daysRemaining).toFloat() / 30f

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (daysRemaining > 0) stringResource(R.string.trash_days_left, daysRemaining) else stringResource(R.string.trash_expiring_soon),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (daysRemaining <= 3) PxColors.Error else PxColors.OnSurfaceDim
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { if (daysRemaining > 0) 1f - progress else 1f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (daysRemaining <= 3) PxColors.Error else PxColors.Warning,
                        trackColor = PxColors.SurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRestore,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestoreFromTrash,
                        contentDescription = stringResource(R.string.restore),
                        tint = PxColors.Success,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteForever,
                        contentDescription = stringResource(R.string.trash_delete_permanently),
                        tint = PxColors.Error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrashEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.DeleteSweep,
            contentDescription = null,
            tint = PxColors.SurfaceVariant,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.notes_trash_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            color = PxColors.OnSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.notes_trash_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = PxColors.OnSurfaceDim,
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
        containerColor = PxColors.Surface,
        title = {
            Text(
                text = stringResource(R.string.notes_trash_empty_all_confirm_title),
                color = PxColors.OnBackground
            )
        },
        text = {
            Text(
                text = stringResource(R.string.trash_empty_confirm_body, "$noteCount ${if (noteCount == 1) "note" else "notes"}"),
                color = PxColors.OnSurfaceDim
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.notes_trash_empty_all), color = PxColors.Error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = PxColors.OnSurfaceDim)
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
