package com.oussama_chatri.productivityx.features.tasks.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.components.PxEmptyState
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.tasks.domain.model.Task
import com.oussama_chatri.productivityx.features.tasks.presentation.components.PriorityChip
import com.oussama_chatri.productivityx.features.tasks.presentation.event.TaskTrashEvent
import com.oussama_chatri.productivityx.features.tasks.presentation.viewmodel.TaskTrashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTrashScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaskTrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = PxColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trash",
                        color = PxColors.OnBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, stringResource(R.string.cd_back), tint = PxColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onEvent(TaskTrashEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PxColors.Primary)
                }

                uiState.tasks.isEmpty() -> PxEmptyState(
                    icon = Icons.Outlined.DeleteForever,
                    title = "Trash is empty",
                    subtitle = "Deleted tasks appear here."
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tasks, key = { it.id }) { task ->
                        TrashTaskItem(
                            task = task,
                            onRestore = { viewModel.onEvent(TaskTrashEvent.RestoreTask(task.id)) },
                            onHardDelete = { viewModel.onEvent(TaskTrashEvent.HardDeleteTask(task.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrashTaskItem(
    task: Task,
    onRestore: () -> Unit,
    onHardDelete: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = PxColors.OnSurfaceDim,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.LineThrough,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (task.dueDate != null) {
                Text(
                    text = "Due ${task.dueDate}",
                    color = PxColors.OnSurfaceDim,
                    fontSize = 12.sp
                )
            }
        }

        PriorityChip(priority = task.priority)

        IconButton(onClick = onRestore) {
            Icon(
                Icons.Outlined.Refresh,
                contentDescription = "Restore",
                tint = PxColors.Success,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(onClick = { showConfirm = true }) {
            Icon(
                Icons.Outlined.DeleteForever,
                contentDescription = "Delete permanently",
                tint = PxColors.Error,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = PxColors.Surface,
            title = {
                Text("Delete permanently?", color = PxColors.OnBackground, fontWeight = FontWeight.SemiBold)
            },
            text = {
                Text("This task cannot be recovered.", color = PxColors.OnSurfaceDim)
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onHardDelete()
                }) { Text("Delete", color = PxColors.Error, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel", color = PxColors.OnSurfaceDim)
                }
            }
        )
    }
}
