package com.oussama_chatri.productivityx.features.notes.presentation.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.components.MarkdownAction
import com.oussama_chatri.productivityx.features.notes.presentation.components.MarkdownToolbar
import com.oussama_chatri.productivityx.features.notes.presentation.components.NoteTagChip
import com.oussama_chatri.productivityx.features.notes.presentation.components.TagPickerSheet
import com.oussama_chatri.productivityx.features.notes.presentation.event.NoteEditorUiEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    noteId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val allTags    by viewModel.allTags.collectAsStateWithLifecycle()
    val snackbar   = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showTagSheet       by rememberSaveable { mutableStateOf(false) }
    var showMenu           by remember { mutableStateOf(false) }
    var showDeleteConfirm  by remember { mutableStateOf(false) }

    var contentValue by remember { mutableStateOf(TextFieldValue(uiState.content)) }

    LaunchedEffect(noteId) { viewModel.init(noteId) }

    LaunchedEffect(uiState.content) {
        if (contentValue.text != uiState.content) {
            contentValue = TextFieldValue(uiState.content, TextRange(uiState.content.length))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.NavigateBack -> onNavigateBack()
                is UiEvent.ShowSnackbar -> snackbar.showSnackbar(event.message)
                else                    -> {}
            }
        }
    }

    Scaffold(
        modifier       = modifier,
        containerColor = Color(0xFF0F0F14),
        snackbarHost   = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint               = Color(0xFFCCCCD8)
                        )
                    }
                },
                title = {
                    Text(
                        text     = uiState.title.ifBlank { "Untitled" }.take(30),
                        style    = MaterialTheme.typography.titleMedium,
                        color    = Color(0xFFCCCCD8),
                        maxLines = 1
                    )
                },
                colors  = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F14)),
                actions = {
                    AnimatedVisibility(visible = uiState.hasUnsavedChanges) {
                        TextButton(onClick = { viewModel.onEvent(NoteEditorUiEvent.Save) }) {
                            Text("Save", color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    IconButton(onClick = { viewModel.onEvent(NoteEditorUiEvent.TogglePin) }) {
                        Icon(
                            imageVector        = Icons.Outlined.PushPin,
                            contentDescription = if (uiState.isPinned) "Unpin" else "Pin",
                            tint               = if (uiState.isPinned) Color(0xFF6366F1) else Color(0xFF888899)
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = Color(0xFFCCCCD8))
                        }
                        EditorDropdownMenu(
                            expanded  = showMenu,
                            onDismiss = { showMenu = false },
                            onDelete  = { showMenu = false; showDeleteConfirm = true }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column {
                MarkdownToolbar(
                    onAction = { action ->
                        val (newText, newCursor) = applyMarkdownAction(action, contentValue)
                        contentValue = TextFieldValue(newText, TextRange(newCursor))
                        viewModel.onEvent(NoteEditorUiEvent.ContentChanged(newText))
                    }
                )
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value         = uiState.title,
                onValueChange = { viewModel.onEvent(NoteEditorUiEvent.TitleChanged(it)) },
                textStyle     = MaterialTheme.typography.titleLarge.copy(
                    color      = Color(0xFFEEEEF5),
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush  = SolidColor(Color(0xFF6366F1)),
                singleLine   = false,
                maxLines     = 3,
                decorationBox = { inner ->
                    if (uiState.title.isEmpty()) {
                        Text(
                            text  = "Untitled",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color    = Color(0xFF888899),
                                fontSize = 22.sp
                            )
                        )
                    }
                    inner()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tag row — existing tags + "Add tag" chip
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                uiState.tags.forEach { tag ->
                    NoteTagChip(
                        tag      = tag,
                        modifier = Modifier.clickable {
                            viewModel.onEvent(NoteEditorUiEvent.RemoveTag(tag.id))
                        }
                    )
                }
                AddTagChip(onClick = { showTagSheet = true })
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF252533))
            Spacer(modifier = Modifier.height(12.dp))

            BasicTextField(
                value         = contentValue,
                onValueChange = { newVal ->
                    contentValue = newVal
                    viewModel.onEvent(NoteEditorUiEvent.ContentChanged(newVal.text))
                },
                textStyle    = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFCCCCD8)),
                cursorBrush  = SolidColor(Color(0xFF6366F1)),
                decorationBox = { inner ->
                    if (contentValue.text.isEmpty()) {
                        Text(
                            text  = "Start writing...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF888899)
                        )
                    }
                    inner()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            )

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    // Tag picker sheet
    if (showTagSheet) {
        TagPickerSheet(
            allTags        = allTags,
            selectedTagIds = uiState.tags.map { it.id }.toSet(),
            sheetState     = sheetState,
            onDismiss      = { showTagSheet = false },
            onTagToggle    = { tagId ->
                val isSelected = uiState.tags.any { it.id == tagId }
                if (isSelected) viewModel.onEvent(NoteEditorUiEvent.RemoveTag(tagId))
                else            viewModel.onEvent(NoteEditorUiEvent.AddTag(tagId))
            },
            onCreateTag    = { name, color ->
                // ← previously an empty comment — now fires the real event
                viewModel.onEvent(NoteEditorUiEvent.CreateTag(name, color))
            }
        )
    }

    if (showDeleteConfirm) {
        DeleteNoteDialog(
            onConfirm = {
                showDeleteConfirm = false
                viewModel.onEvent(NoteEditorUiEvent.DeleteNote)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun AddTagChip(onClick: () -> Unit) {
    Row(
        modifier              = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFF252533))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector        = Icons.Outlined.Add,
            contentDescription = "Add tag",
            tint               = Color(0xFF888899),
            modifier           = Modifier.size(14.dp)
        )
        Text(
            text  = "Add tag",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF888899)
        )
    }
}

@Composable
private fun EditorDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded         = expanded,
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF1A1A24)
    ) {
        DropdownMenuItem(
            text    = {
                Text(
                    "Move to trash",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFEF4444)
                )
            },
            onClick = onDelete
        )
    }
}

@Composable
private fun DeleteNoteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF1A1A24),
        title  = { Text("Move to trash?", color = Color(0xFFEEEEF5)) },
        text   = {
            Text(
                "This note will be moved to trash. You can restore it within 30 days.",
                color = Color(0xFF888899)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Move to trash", color = Color(0xFFEF4444))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF888899))
            }
        }
    )
}

private fun applyMarkdownAction(action: MarkdownAction, current: TextFieldValue): Pair<String, Int> {
    val text     = current.text
    val start    = current.selection.start
    val end      = current.selection.end
    val selected = text.substring(start, end)

    return when {
        action.suffix.isNotEmpty() -> {
            val wrapped = "${action.prefix}${selected.ifEmpty { "text" }}${action.suffix}"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            val cursor  = if (selected.isEmpty()) start + action.prefix.length + 4
            else start + wrapped.length
            newText to cursor
        }
        else -> {
            val lineStart = text.lastIndexOf('\n', start - 1) + 1
            val newText   = text.substring(0, lineStart) + action.prefix + text.substring(lineStart)
            val cursor    = start + action.prefix.length
            newText to cursor
        }
    }
}