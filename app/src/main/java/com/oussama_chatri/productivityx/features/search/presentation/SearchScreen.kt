package com.oussama_chatri.productivityx.features.search.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.PxEmptyState
import com.oussama_chatri.productivityx.core.ui.components.PxLoadingState
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResult
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResultType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNoteClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var recentSearches by rememberSaveable { mutableStateOf(listOf<String>()) }

    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title), color = PxColors.OnBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = PxColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.onQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_hint), color = PxColors.OnSurfaceDim) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = PxColors.OnSurfaceDim)
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.cd_clear_input), tint = PxColors.OnSurfaceDim)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PxColors.OnBackground,
                    unfocusedTextColor = PxColors.OnBackground,
                    cursorColor = PxColors.Primary,
                    focusedBorderColor = PxColors.Primary,
                    unfocusedBorderColor = PxColors.SurfaceVariant,
                    focusedContainerColor = PxColors.Surface,
                    unfocusedContainerColor = PxColors.Surface
                )
            )

            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading -> PxLoadingState()
                state.error != null && state.results.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.error!!, color = PxColors.OnSurfaceDim)
                    }
                }
                state.query.isEmpty() -> {
                    RecentSearchesSection(
                        searches = recentSearches,
                        onSearch = { query ->
                            recentSearches = (listOf(query) + recentSearches.filter { it != query }).take(10)
                            viewModel.onQueryChanged(query)
                        },
                        onClear = { recentSearches = emptyList() }
                    )
                }
                state.results.isEmpty() -> {
                    NoResultsState(query = state.query)
                }
                else -> {
                    val notes = state.results.filter { it.type == SearchResultType.NOTE }
                    val tasks = state.results.filter { it.type == SearchResultType.TASK }
                    val events = state.results.filter { it.type == SearchResultType.EVENT }

                    val saveQuery: () -> Unit = {
                        val q = state.query.trim()
                        if (q.isNotEmpty()) {
                            recentSearches = (listOf(q) + recentSearches.filter { it != q }).take(10)
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (notes.isNotEmpty()) {
                            item(key = "header_notes") {
                                SectionHeader(
                                    icon = Icons.Outlined.Description,
                                    label = stringResource(R.string.search_type_note),
                                    color = PxColors.Primary,
                                    count = notes.size
                                )
                            }
                            items(notes, key = { "note_${it.id}" }) { result ->
                                AnimatedResultItem(
                                    result = result,
                                    typeIcon = Icons.Outlined.Description,
                                    typeLabel = stringResource(R.string.search_type_note),
                                    typeColor = PxColors.Primary,
                                    onClick = {
                                        saveQuery()
                                        onNoteClick(result.id)
                                    }
                                )
                            }
                        }
                        if (tasks.isNotEmpty()) {
                            item(key = "header_tasks") {
                                SectionHeader(
                                    icon = Icons.Outlined.CheckCircle,
                                    label = stringResource(R.string.search_type_task),
                                    color = PxColors.Success,
                                    count = tasks.size
                                )
                            }
                            items(tasks, key = { "task_${it.id}" }) { result ->
                                AnimatedResultItem(
                                    result = result,
                                    typeIcon = Icons.Outlined.CheckCircle,
                                    typeLabel = stringResource(R.string.search_type_task),
                                    typeColor = PxColors.Success,
                                    onClick = {
                                        saveQuery()
                                        onTaskClick(result.id)
                                    }
                                )
                            }
                        }
                        if (events.isNotEmpty()) {
                            item(key = "header_events") {
                                SectionHeader(
                                    icon = Icons.Outlined.Event,
                                    label = stringResource(R.string.search_type_event),
                                    color = PxColors.Warning,
                                    count = events.size
                                )
                            }
                            items(events, key = { "event_${it.id}" }) { result ->
                                AnimatedResultItem(
                                    result = result,
                                    typeIcon = Icons.Outlined.Event,
                                    typeLabel = stringResource(R.string.search_type_event),
                                    typeColor = PxColors.Warning,
                                    onClick = {
                                        saveQuery()
                                        onEventClick(result.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Recent searches ──────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun RecentSearchesSection(
    searches: List<String>,
    onSearch: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = null,
                    tint = PxColors.OnSurfaceDim,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.search_recent),
                    style = MaterialTheme.typography.labelLarge,
                    color = PxColors.OnSurfaceDim
                )
            }
            if (searches.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Icon(
                        Icons.Outlined.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                            Text(stringResource(R.string.search_clear_recent), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (searches.isEmpty()) {
            PxEmptyState(
                icon     = Icons.Outlined.Search,
                title    = stringResource(R.string.search_hint),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                searches.forEach { query ->
                    AnimatedVisibility(visible = true) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PxColors.SurfaceVariant)
                                .clickable { onSearch(query) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = query,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PxColors.OnSurfaceDim,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── No results state ─────────────────────────────────────────────────────────

@Composable
private fun NoResultsState(query: String) {
    PxEmptyState(
        icon     = Icons.Outlined.SearchOff,
        title    = stringResource(R.string.search_empty_title, query),
        subtitle = stringResource(R.string.search_empty_body),
        modifier = Modifier.fillMaxSize()
    )
}

// ─── Section header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icon: ImageVector,
    label: String,
    color: Color,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = PxColors.OnBackground
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

// ─── Animated result card ─────────────────────────────────────────────────────

@Composable
private fun AnimatedResultItem(
    result: SearchResult,
    typeIcon: ImageVector,
    typeLabel: String,
    typeColor: Color,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { it / 2 }
    ) {
        SearchResultCard(
            result = result,
            typeIcon = typeIcon,
            typeLabel = typeLabel,
            typeColor = typeColor,
            onClick = onClick
        )
    }
}

// ─── Result card with left accent bar ─────────────────────────────────────────

@Composable
private fun SearchResultCard(
    result: SearchResult,
    typeIcon: ImageVector,
    typeLabel: String,
    typeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
                .background(typeColor)
        )
        Spacer(Modifier.width(0.dp))
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = PxColors.Surface),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = PxColors.OnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (result.snippet.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = result.snippet,
                            style = MaterialTheme.typography.bodySmall,
                            color = PxColors.OnSurfaceDim,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
