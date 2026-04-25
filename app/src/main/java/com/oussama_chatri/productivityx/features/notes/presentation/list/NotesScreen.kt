package com.oussama_chatri.productivityx.features.notes.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.components.FilterTagChip
import com.oussama_chatri.productivityx.features.notes.presentation.components.NoteStaggeredCard
import com.oussama_chatri.productivityx.features.notes.presentation.event.NotesUiEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToEditor: (noteId: String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToTrash: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost   = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHost.showSnackbar(event.message)
                else                   -> {}
            }
        }
    }

    Scaffold(
        modifier        = modifier,
        containerColor  = Color(0xFF0F0F14),
        snackbarHost    = { SnackbarHost(snackbarHost) },
        topBar          = {
            TopAppBar(
                title  = { Text("Notes", style = MaterialTheme.typography.titleLarge, color = Color(0xFFEEEEF5)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F14)),
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color(0xFFCCCCD8))
                    }
                    IconButton(onClick = onNavigateToTrash) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Trash", tint = Color(0xFFCCCCD8))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick           = { onNavigateToEditor(null) },
                containerColor    = Color(0xFF6366F1),
                contentColor      = Color.White,
                icon              = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text              = { Text("New note", style = MaterialTheme.typography.labelMedium) }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = { viewModel.onEvent(NotesUiEvent.Refresh) },
            modifier     = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.notes.isEmpty() && !uiState.isLoading) {
                NotesEmptyState(modifier = Modifier.fillMaxSize())
            } else {
                LazyVerticalStaggeredGrid(
                    columns             = StaggeredGridCells.Fixed(2),
                    contentPadding      = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                    modifier            = Modifier.fillMaxSize()
                ) {
                    // Filter chips
                    item(span = StaggeredGridItemSpan.FullLine) {
                        FilterChipsRow(
                            tags           = uiState.tags,
                            selectedTagId  = uiState.selectedTagId,
                            showPinnedOnly = uiState.showPinnedOnly,
                            onTagSelected  = { viewModel.onEvent(NotesUiEvent.FilterByTag(it)) },
                            onPinnedToggle = { viewModel.onEvent(NotesUiEvent.TogglePinnedFilter) },
                            modifier       = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Pinned section header
                    val pinnedNotes = uiState.notes.filter { it.isPinned }
                    if (pinnedNotes.isNotEmpty() && uiState.selectedTagId == null && !uiState.showPinnedOnly) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text     = "Pinned",
                                style    = MaterialTheme.typography.labelMedium,
                                color    = Color(0xFF888899),
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }
                        items(pinnedNotes, key = { "pin_${it.id}" }) { note ->
                            NoteStaggeredCard(
                                note    = note,
                                onClick = { onNavigateToEditor(note.id) }
                            )
                        }

                        val otherNotes = uiState.notes.filter { !it.isPinned }
                        if (otherNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Text(
                                    text     = "Others",
                                    style    = MaterialTheme.typography.labelMedium,
                                    color    = Color(0xFF888899),
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
                            }
                            items(otherNotes, key = { it.id }) { note ->
                                NoteStaggeredCard(
                                    note    = note,
                                    onClick = { onNavigateToEditor(note.id) }
                                )
                            }
                        }
                    } else {
                        items(uiState.notes, key = { it.id }) { note ->
                            NoteStaggeredCard(
                                note    = note,
                                onClick = { onNavigateToEditor(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    tags: List<com.oussama_chatri.productivityx.features.notes.domain.model.Tag>,
    selectedTagId: String?,
    showPinnedOnly: Boolean,
    onTagSelected: (String?) -> Unit,
    onPinnedToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier              = modifier
    ) {
        item {
            FilterTagChip(
                label      = "All",
                isSelected = selectedTagId == null && !showPinnedOnly,
                onClick    = { onTagSelected(null) }
            )
        }
        item {
            FilterTagChip(
                label      = "Pinned",
                isSelected = showPinnedOnly,
                onClick    = onPinnedToggle
            )
        }
        items(tags, key = { it.id }) { tag ->
            FilterTagChip(
                label      = tag.name,
                isSelected = tag.id == selectedTagId,
                onClick    = { onTagSelected(tag.id) }
            )
        }
    }
}

@Composable
private fun NotesEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier              = modifier.padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Outlined.StickyNote2,
            contentDescription = null,
            tint               = Color(0xFF252533),
            modifier           = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text      = "No notes yet",
            style     = MaterialTheme.typography.bodyLarge,
            color     = Color(0xFFCCCCD8),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text      = "Tap + to start writing",
            style     = MaterialTheme.typography.bodyMedium,
            color     = Color(0xFF888899),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun NotesScreenPreview() {
    ProductivityXTheme {
        NotesEmptyState(modifier = Modifier.fillMaxSize())
    }
}
