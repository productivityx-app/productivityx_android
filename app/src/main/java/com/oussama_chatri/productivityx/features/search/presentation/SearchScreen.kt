package com.oussama_chatri.productivityx.features.search.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResult
import com.oussama_chatri.productivityx.features.search.domain.model.SearchResultType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNoteClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0F0F14),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title), color = Color(0xFFEEEEF5)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = Color(0xFFCCCCD8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F14))
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
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_hint), color = Color(0xFF888899)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFF888899))
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.cd_clear_input), tint = Color(0xFF888899))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFEEEEF5),
                    unfocusedTextColor = Color(0xFFEEEEF5),
                    cursorColor = Color(0xFF6366F1),
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFF252533),
                    focusedContainerColor = Color(0xFF1A1A22),
                    unfocusedContainerColor = Color(0xFF1A1A22)
                )
            )

            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
                    }
                }
                state.error != null && state.results.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.error!!, color = Color(0xFF888899))
                    }
                }
                state.query.isNotEmpty() && state.results.isEmpty() && !state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = Color(0xFF888899),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.search_empty_title, state.query), color = Color(0xFF888899))
                        }
                    }
                }
                else -> {
                    @Composable fun typeLabel(type: SearchResultType): String = when (type) {
                        SearchResultType.NOTE -> stringResource(R.string.search_type_note)
                        SearchResultType.TASK -> stringResource(R.string.search_type_task)
                        SearchResultType.EVENT -> stringResource(R.string.search_type_event)
                    }
                    val typeIcon = { type: SearchResultType ->
                        when (type) {
                            SearchResultType.NOTE -> Icons.Outlined.Description
                            SearchResultType.TASK -> Icons.Outlined.CheckCircle
                            SearchResultType.EVENT -> Icons.Outlined.Event
                        }
                    }
                    val typeColor = { type: SearchResultType ->
                        when (type) {
                            SearchResultType.NOTE -> Color(0xFF6366F1)
                            SearchResultType.TASK -> Color(0xFF10B981)
                            SearchResultType.EVENT -> Color(0xFFF59E0B)
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(state.results, key = { "${it.type}_${it.id}" }) { result ->
                            SearchResultItem(
                                result = result,
                                typeIcon = typeIcon(result.type),
                                typeLabel = typeLabel(result.type),
                                typeColor = typeColor(result.type),
                                onClick = {
                                    when (result.type) {
                                        SearchResultType.NOTE -> onNoteClick(result.id)
                                        SearchResultType.TASK -> onTaskClick(result.id)
                                        SearchResultType.EVENT -> onEventClick(result.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    typeIcon: ImageVector,
    typeLabel: String,
    typeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A22)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                    color = Color(0xFFEEEEF5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (result.snippet.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = result.snippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888899),
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
