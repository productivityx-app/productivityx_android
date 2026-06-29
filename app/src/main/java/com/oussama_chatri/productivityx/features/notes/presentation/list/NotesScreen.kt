package com.oussama_chatri.productivityx.features.notes.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.PxEmptyState
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.components.FilterTagChip
import com.oussama_chatri.productivityx.features.notes.presentation.components.NoteCard
import com.oussama_chatri.productivityx.features.notes.presentation.components.relativeTime
import com.oussama_chatri.productivityx.features.notes.presentation.event.NotesUiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.state.NoteViewMode
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagedNotes = viewModel.pagedNotes.collectAsLazyPagingItems()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHost.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar
            AnimatedVisibility(visible = uiState.isSearchActive) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onEvent(NotesUiEvent.SetSearchQuery(it)) },
                    onClose = { viewModel.onEvent(NotesUiEvent.ClearSearch) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Selection mode bulk actions bar
            AnimatedVisibility(
                visible = uiState.isSelectionMode,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                BulkActionBar(
                    selectedCount = uiState.selectedNoteIds.size,
                    onDelete = { viewModel.onEvent(NotesUiEvent.BulkDelete) },
                    onArchive = { viewModel.onEvent(NotesUiEvent.BulkArchive) },
                    onPin = { viewModel.onEvent(NotesUiEvent.BulkPin) },
                    onClearSelection = { viewModel.onEvent(NotesUiEvent.ClearSelection) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.onEvent(NotesUiEvent.Refresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.notes.isEmpty() && !uiState.isLoading && !uiState.isSearchActive && uiState.selectedTagId == null && !uiState.showPinnedOnly && uiState.selectedFolderId == null) {
                    PxEmptyState(
                        icon = Icons.Outlined.StickyNote2,
                        title = stringResource(R.string.notes_empty_title),
                        subtitle = stringResource(R.string.notes_empty_body),
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (uiState.isSearchActive && uiState.searchResults.isEmpty() && uiState.searchQuery.isNotBlank()) {
                    PxEmptyState(
                        icon = Icons.Outlined.StickyNote2,
                        title = "No results",
                        subtitle = "No notes match \"${uiState.searchQuery}\"",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    when (uiState.viewMode) {
                        NoteViewMode.GRID -> NotesGridView(
                            notes = pagedNotes,
                            tags = uiState.tags,
                            selectedTagId = uiState.selectedTagId,
                            showPinnedOnly = uiState.showPinnedOnly,
                            isSelectionMode = uiState.isSelectionMode,
                            selectedNoteIds = uiState.selectedNoteIds,
                            searchQuery = uiState.searchQuery,
                            onTagSelected = { viewModel.onEvent(NotesUiEvent.FilterByTag(it)) },
                            onPinnedToggle = { viewModel.onEvent(NotesUiEvent.TogglePinnedFilter) },
                            onNoteClick = { id -> if (uiState.isSelectionMode) viewModel.onEvent(NotesUiEvent.ToggleNoteSelection(id)) else onNavigateToEditor(id) },
                            onNoteLongClick = { viewModel.onEvent(NotesUiEvent.ToggleNoteSelection(it)) },
                            onSwipeLeft = { viewModel.onEvent(NotesUiEvent.SwipeArchive(it)) },
                            onSwipeRight = { viewModel.onEvent(NotesUiEvent.SwipePin(it)) },
                            onSetViewMode = { viewModel.onEvent(NotesUiEvent.SetViewMode(it)) },
                            onToggleSearch = { viewModel.onEvent(NotesUiEvent.ToggleSearch) }
                        )
                        NoteViewMode.LIST -> NotesListView(
                            notes = pagedNotes,
                            tags = uiState.tags,
                            selectedTagId = uiState.selectedTagId,
                            showPinnedOnly = uiState.showPinnedOnly,
                            isSelectionMode = uiState.isSelectionMode,
                            selectedNoteIds = uiState.selectedNoteIds,
                            searchQuery = uiState.searchQuery,
                            onTagSelected = { viewModel.onEvent(NotesUiEvent.FilterByTag(it)) },
                            onPinnedToggle = { viewModel.onEvent(NotesUiEvent.TogglePinnedFilter) },
                            onNoteClick = { id -> if (uiState.isSelectionMode) viewModel.onEvent(NotesUiEvent.ToggleNoteSelection(id)) else onNavigateToEditor(id) },
                            onNoteLongClick = { viewModel.onEvent(NotesUiEvent.ToggleNoteSelection(it)) },
                            onSwipeLeft = { viewModel.onEvent(NotesUiEvent.SwipeArchive(it)) },
                            onSwipeRight = { viewModel.onEvent(NotesUiEvent.SwipePin(it)) },
                            onSetViewMode = { viewModel.onEvent(NotesUiEvent.SetViewMode(it)) },
                            onToggleSearch = { viewModel.onEvent(NotesUiEvent.ToggleSearch) }
                        )
                        NoteViewMode.COMPACT -> NotesCompactView(
                            notes = pagedNotes,
                            tags = uiState.tags,
                            selectedTagId = uiState.selectedTagId,
                            showPinnedOnly = uiState.showPinnedOnly,
                            isSelectionMode = uiState.isSelectionMode,
                            selectedNoteIds = uiState.selectedNoteIds,
                            searchQuery = uiState.searchQuery,
                            onTagSelected = { viewModel.onEvent(NotesUiEvent.FilterByTag(it)) },
                            onPinnedToggle = { viewModel.onEvent(NotesUiEvent.TogglePinnedFilter) },
                            onNoteClick = { id -> if (uiState.isSelectionMode) viewModel.onEvent(NotesUiEvent.ToggleNoteSelection(id)) else onNavigateToEditor(id) },
                            onNoteLongClick = { viewModel.onEvent(NotesUiEvent.ToggleNoteSelection(it)) },
                            onSetViewMode = { viewModel.onEvent(NotesUiEvent.SetViewMode(it)) },
                            onToggleSearch = { viewModel.onEvent(NotesUiEvent.ToggleSearch) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(PxColors.Background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search notes...", color = PxColors.OnSurfaceDim) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = PxColors.SurfaceVariant,
                unfocusedContainerColor = PxColors.SurfaceVariant,
                cursorColor = PxColors.Primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = PxColors.OnSurface)
        }
    }
}

@Composable
private fun BulkActionBar(
    selectedCount: Int,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onPin: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(PxColors.SurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Filled.Close, contentDescription = "Clear selection", tint = PxColors.OnSurface)
            }
            Text(
                "$selectedCount selected",
                style = MaterialTheme.typography.labelLarge,
                color = PxColors.OnSurface
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onPin) {
                Icon(Icons.Outlined.PushPin, contentDescription = "Pin", tint = PxColors.OnSurface)
            }
            IconButton(onClick = onArchive) {
                Icon(Icons.Outlined.Archive, contentDescription = "Archive", tint = PxColors.OnSurface)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = PxColors.Error)
            }
        }
    }
}

@Composable
private fun NotesGridView(
    notes: LazyPagingItems<com.oussama_chatri.productivityx.features.notes.domain.model.Note>,
    tags: List<com.oussama_chatri.productivityx.features.notes.domain.model.Tag>,
    selectedTagId: String?,
    showPinnedOnly: Boolean,
    isSelectionMode: Boolean,
    selectedNoteIds: Set<String>,
    searchQuery: String,
    onTagSelected: (String?) -> Unit,
    onPinnedToggle: () -> Unit,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onSwipeLeft: (String) -> Unit,
    onSwipeRight: (String) -> Unit,
    onSetViewMode: (NoteViewMode) -> Unit,
    onToggleSearch: () -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            NotesToolbar(
                tags = tags,
                selectedTagId = selectedTagId,
                showPinnedOnly = showPinnedOnly,
                currentViewMode = NoteViewMode.GRID,
                onTagSelected = onTagSelected,
                onPinnedToggle = onPinnedToggle,
                onSetViewMode = onSetViewMode,
                onToggleSearch = onToggleSearch
            )
        }

        items(
            count = notes.itemCount,
            key = notes.itemKey { it.id },
            span = { index ->
                val note = notes[index]
                if (note?.isPinned == true && selectedTagId == null && !showPinnedOnly) {
                    // StaggeredGrid does not support FullLine for individual items in items() easily like LazyColumn headers
                    // but we can just let them flow. Paging items don't mix well with manual headers in StaggeredGrid items()
                    StaggeredGridItemSpan.SingleLane
                } else StaggeredGridItemSpan.SingleLane
            }
        ) { index ->
            notes[index]?.let { note ->
                NoteCard(
                    note = note,
                    viewMode = NoteViewMode.GRID,
                    isSelected = note.id in selectedNoteIds,
                    isSelectionMode = isSelectionMode,
                    searchQuery = searchQuery,
                    onClick = { onNoteClick(note.id) },
                    onLongClick = { onNoteLongClick(note.id) },
                    onSwipeLeft = { onSwipeLeft(note.id) },
                    onSwipeRight = { onSwipeRight(note.id) }
                )
            }
        }
    }
}

@Composable
private fun NotesListView(
    notes: LazyPagingItems<com.oussama_chatri.productivityx.features.notes.domain.model.Note>,
    tags: List<com.oussama_chatri.productivityx.features.notes.domain.model.Tag>,
    selectedTagId: String?,
    showPinnedOnly: Boolean,
    isSelectionMode: Boolean,
    selectedNoteIds: Set<String>,
    searchQuery: String,
    onTagSelected: (String?) -> Unit,
    onPinnedToggle: () -> Unit,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onSwipeLeft: (String) -> Unit,
    onSwipeRight: (String) -> Unit,
    onSetViewMode: (NoteViewMode) -> Unit,
    onToggleSearch: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            NotesToolbar(
                tags = tags,
                selectedTagId = selectedTagId,
                showPinnedOnly = showPinnedOnly,
                currentViewMode = NoteViewMode.LIST,
                onTagSelected = onTagSelected,
                onPinnedToggle = onPinnedToggle,
                onSetViewMode = onSetViewMode,
                onToggleSearch = onToggleSearch
            )
        }

        items(
            count = notes.itemCount,
            key = notes.itemKey { it.id }
        ) { index ->
            notes[index]?.let { note ->
                NoteCard(
                    note = note,
                    viewMode = NoteViewMode.LIST,
                    isSelected = note.id in selectedNoteIds,
                    isSelectionMode = isSelectionMode,
                    searchQuery = searchQuery,
                    onClick = { onNoteClick(note.id) },
                    onLongClick = { onNoteLongClick(note.id) },
                    onSwipeLeft = { onSwipeLeft(note.id) },
                    onSwipeRight = { onSwipeRight(note.id) }
                )
            }
        }
    }
}

@Composable
private fun NotesCompactView(
    notes: LazyPagingItems<com.oussama_chatri.productivityx.features.notes.domain.model.Note>,
    tags: List<com.oussama_chatri.productivityx.features.notes.domain.model.Tag>,
    selectedTagId: String?,
    showPinnedOnly: Boolean,
    isSelectionMode: Boolean,
    selectedNoteIds: Set<String>,
    searchQuery: String,
    onTagSelected: (String?) -> Unit,
    onPinnedToggle: () -> Unit,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onSetViewMode: (NoteViewMode) -> Unit,
    onToggleSearch: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            NotesToolbar(
                tags = tags,
                selectedTagId = selectedTagId,
                showPinnedOnly = showPinnedOnly,
                currentViewMode = NoteViewMode.COMPACT,
                onTagSelected = onTagSelected,
                onPinnedToggle = onPinnedToggle,
                onSetViewMode = onSetViewMode,
                onToggleSearch = onToggleSearch
            )
        }
        items(
            count = notes.itemCount,
            key = notes.itemKey { it.id }
        ) { index ->
            notes[index]?.let { note ->
                NoteCard(
                    note = note,
                    viewMode = NoteViewMode.COMPACT,
                    isSelected = note.id in selectedNoteIds,
                    isSelectionMode = isSelectionMode,
                    searchQuery = searchQuery,
                    onClick = { onNoteClick(note.id) },
                    onLongClick = { onNoteLongClick(note.id) },
                    modifier = Modifier.animateContentSize()
                )
            }
        }
    }
}

@Composable
private fun NotesToolbar(
    tags: List<com.oussama_chatri.productivityx.features.notes.domain.model.Tag>,
    selectedTagId: String?,
    showPinnedOnly: Boolean,
    currentViewMode: NoteViewMode,
    onTagSelected: (String?) -> Unit,
    onPinnedToggle: () -> Unit,
    onSetViewMode: (NoteViewMode) -> Unit,
    onToggleSearch: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = PxColors.OnSurface)
            }
            IconButton(onClick = { onSetViewMode(NoteViewMode.GRID) }) {
                Icon(
                    Icons.Filled.GridView,
                    contentDescription = "Grid view",
                    tint = if (currentViewMode == NoteViewMode.GRID) PxColors.Primary else PxColors.OnSurfaceDim
                )
            }
            IconButton(onClick = { onSetViewMode(NoteViewMode.LIST) }) {
                Icon(
                    Icons.Filled.ViewList,
                    contentDescription = "List view",
                    tint = if (currentViewMode == NoteViewMode.LIST) PxColors.Primary else PxColors.OnSurfaceDim
                )
            }
        }

        FilterChipsRow(
            tags = tags,
            selectedTagId = selectedTagId,
            showPinnedOnly = showPinnedOnly,
            onTagSelected = onTagSelected,
            onPinnedToggle = onPinnedToggle,
            modifier = Modifier.padding(vertical = 4.dp)
        )
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
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        item {
            FilterTagChip(
                label = "All",
                isSelected = selectedTagId == null && !showPinnedOnly,
                onClick = { onTagSelected(null) }
            )
        }
        item {
            FilterTagChip(
                label = "Pinned",
                isSelected = showPinnedOnly,
                onClick = onPinnedToggle
            )
        }
        items(tags, key = { it.id }) { tag ->
            FilterTagChip(
                label = tag.name,
                isSelected = tag.id == selectedTagId,
                onClick = { onTagSelected(tag.id) }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun NotesScreenPreview() {
    ProductivityXTheme {
        PxEmptyState(
            icon = Icons.Outlined.StickyNote2,
            title = stringResource(R.string.notes_empty_title),
            subtitle = stringResource(R.string.notes_empty_body),
            modifier = Modifier.fillMaxSize()
        )
    }
}
