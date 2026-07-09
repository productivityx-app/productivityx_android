package com.oussama_chatri.productivityx.features.notes.presentation.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil3.compose.AsyncImage
import com.oussama_chatri.productivityx.features.notes.presentation.util.PdfGenerator
import kotlinx.coroutines.launch
import android.content.Intent
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
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.components.MarkdownAction
import com.oussama_chatri.productivityx.features.notes.presentation.components.MarkdownToolbar
import com.oussama_chatri.productivityx.features.notes.presentation.components.NoteTagChip
import com.oussama_chatri.productivityx.features.notes.presentation.components.SyncDot
import com.oussama_chatri.productivityx.features.notes.presentation.components.TagPickerSheet
import com.oussama_chatri.productivityx.features.notes.presentation.state.EditorFocusMode
import com.oussama_chatri.productivityx.features.notes.presentation.event.NoteEditorUiEvent
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    noteId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // We must take persistable URI permission so we can read it later
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
                viewModel.onEvent(NoteEditorUiEvent.AddImage(uri.toString()))
            }
        }
    )

    val pdfScope = androidx.compose.runtime.rememberCoroutineScope()
    val pdfCreator = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            if (uri != null) {
                pdfScope.launch {
                    val result = PdfGenerator.generatePdf(
                        context = context,
                        uri = uri,
                        title = uiState.title,
                        content = uiState.content
                    )
                    if (result.isSuccess) {
                        viewModel.onEvent(NoteEditorUiEvent.HideExportSheet)
                        // Could show a success snackbar here
                    } else {
                        // Could show an error snackbar here
                    }
                }
            }
        }
    )

    var showTagSheet by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var contentValue by remember { mutableStateOf(TextFieldValue(uiState.content)) }

    val isFocusMode = uiState.focusMode != EditorFocusMode.NORMAL

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
                else -> {}
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier,
            containerColor = if (isFocusMode) Color(0xFF0A0A0F) else PxColors.Background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                if (!isFocusMode) {
                    EditorTopAppBar(
                        title = uiState.title,
                        isPinned = uiState.isPinned,
                        hasUnsavedChanges = uiState.hasUnsavedChanges,
                        showMenu = showMenu,
                        onBack = onNavigateBack,
                        onTogglePin = { viewModel.onEvent(NoteEditorUiEvent.TogglePin) },
                        onSave = { viewModel.onEvent(NoteEditorUiEvent.Save) },
                        onMoreClick = { showMenu = true },
                        onMenuDismiss = { showMenu = false },
                        onDelete = { showMenu = false; showDeleteConfirm = true },
                        onToggleFocus = { viewModel.onEvent(NoteEditorUiEvent.ToggleFocusMode) },
                        onToggleMetadata = { viewModel.onEvent(NoteEditorUiEvent.ToggleMetadata) },
                        onExport = { viewModel.onEvent(NoteEditorUiEvent.ShowExportSheet) }
                    )
                }
            },
            bottomBar = {
                if (!isFocusMode) {
                    Column {
                        MarkdownToolbar(
                            onAction = { action ->
                                val (newText, newCursor) = applyMarkdownAction(action, contentValue)
                                contentValue = TextFieldValue(newText, TextRange(newCursor))
                                viewModel.onEvent(NoteEditorUiEvent.ContentChanged(newText))
                            },
                            onAddImageClick = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isFocusMode) 24.dp else 20.dp)
                    .then(if (isFocusMode) Modifier.background(Color(0xFF0A0A0F)) else Modifier)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Title field
                BasicTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(NoteEditorUiEvent.TitleChanged(it)) },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = if (isFocusMode) PxColors.OnBackground else PxColors.OnBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(PxColors.Primary),
                    singleLine = false,
                    maxLines = 3,
                    decorationBox = { inner ->
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = "Untitled",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = PxColors.OnSurfaceDim,
                                    fontSize = 22.sp
                                )
                            )
                        }
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tag row
                if (!isFocusMode) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.tags.forEach { tag ->
                            NoteTagChip(
                                tag = tag,
                                modifier = Modifier.clickable {
                                    viewModel.onEvent(NoteEditorUiEvent.RemoveTag(tag.id))
                                }
                            )
                        }
                        AddTagChip(onClick = { showTagSheet = true })
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = PxColors.Outline)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Images
                if (uiState.imageUrls.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        uiState.imageUrls.forEach { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Note image",
                                    modifier = Modifier
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                IconButton(
                                    onClick = { viewModel.onEvent(NoteEditorUiEvent.RemoveImage(uri)) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Content field
                BasicTextField(
                    value = contentValue,
                    onValueChange = { newVal ->
                        contentValue = newVal
                        viewModel.onEvent(NoteEditorUiEvent.ContentChanged(newVal.text))
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isFocusMode) PxColors.OnSurface else PxColors.OnSurface,
                        lineHeight = if (isFocusMode) 28.sp else MaterialTheme.typography.bodyLarge.lineHeight
                    ),
                    cursorBrush = SolidColor(PxColors.Primary),
                    decorationBox = { inner ->
                        if (contentValue.text.isEmpty()) {
                            Text(
                                text = "Start writing...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = PxColors.OnSurfaceDim
                            )
                        }
                        inner()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                )

                // Metadata bar
                AnimatedVisibility(visible = uiState.showMetadata && !isFocusMode) {
                    MetadataBar(
                        wordCount = uiState.wordCount,
                        characterCount = uiState.characterCount,
                        readingTimeLabel = uiState.readingTimeLabel,
                        lastSavedAt = uiState.lastSavedAt,
                        syncStatus = uiState.syncStatus,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                // Auto-save indicator
                if (!isFocusMode && uiState.hasUnsavedChanges) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = PxColors.Warning,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Unsaved changes",
                            style = MaterialTheme.typography.labelSmall,
                            color = PxColors.Warning
                        )
                    }
                } else if (!isFocusMode && uiState.lastSavedAt != null) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = PxColors.Success,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = PxColors.Success
                        )
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // Focus mode exit button
        if (isFocusMode) {
            IconButton(
                onClick = { viewModel.onEvent(NoteEditorUiEvent.ToggleFocusMode) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .background(
                        color = PxColors.SurfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Outlined.CenterFocusWeak,
                    contentDescription = "Exit focus mode",
                    tint = PxColors.OnSurface
                )
            }
        }
    }

    // Tag picker sheet
    if (showTagSheet) {
        TagPickerSheet(
            allTags = allTags,
            selectedTagIds = uiState.tags.map { it.id }.toSet(),
            sheetState = sheetState,
            onDismiss = { showTagSheet = false },
            onTagToggle = { tagId ->
                val isSelected = uiState.tags.any { it.id == tagId }
                if (isSelected) viewModel.onEvent(NoteEditorUiEvent.RemoveTag(tagId))
                else viewModel.onEvent(NoteEditorUiEvent.AddTag(tagId))
            },
            onCreateTag = { name, color ->
                viewModel.onEvent(NoteEditorUiEvent.CreateTag(name, color))
            }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        DeleteNoteDialog(
            onConfirm = {
                showDeleteConfirm = false
                viewModel.onEvent(NoteEditorUiEvent.DeleteNote)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    // Export sheet
    if (uiState.showExportSheet) {
        ExportSheet(
            onDismiss = { viewModel.onEvent(NoteEditorUiEvent.HideExportSheet) },
            onExportPdf = { pdfCreator.launch("${uiState.title.ifBlank { "Note" }}.pdf") },
            onShare = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, uiState.title)
                    putExtra(Intent.EXTRA_TEXT, "${uiState.title}\n\n${uiState.content}")
                }
                context.startActivity(Intent.createChooser(intent, "Share Note"))
            },
            onPrint = { /* Print */ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopAppBar(
    title: String,
    isPinned: Boolean,
    hasUnsavedChanges: Boolean,
    showMenu: Boolean,
    onBack: () -> Unit,
    onTogglePin: () -> Unit,
    onSave: () -> Unit,
    onMoreClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onDelete: () -> Unit,
    onToggleFocus: () -> Unit,
    onToggleMetadata: () -> Unit,
    onExport: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = PxColors.OnSurface
                )
            }
        },
        title = {
            Text(
                text = title.ifBlank { "Untitled" }.take(30),
                style = MaterialTheme.typography.titleMedium,
                color = PxColors.OnSurface,
                maxLines = 1
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background),
        actions = {
            IconButton(onClick = onToggleFocus) {
                Icon(Icons.Outlined.CenterFocusWeak, contentDescription = "Focus mode", tint = PxColors.OnSurfaceDim)
            }
            AnimatedVisibility(visible = hasUnsavedChanges) {
                TextButton(onClick = onSave) {
                    Text("Save", color = PxColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            }
            IconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) "Unpin" else "Pin",
                    tint = if (isPinned) PxColors.Primary else PxColors.OnSurfaceDim
                )
            }
            Box {
                IconButton(onClick = onMoreClick) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = PxColors.OnSurface)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onMenuDismiss,
                    containerColor = PxColors.Surface
                ) {
                    DropdownMenuItem(
                        text = { Text("Show metadata", style = MaterialTheme.typography.bodyMedium, color = PxColors.OnSurface) },
                        onClick = { onMenuDismiss(); onToggleMetadata() }
                    )
                    DropdownMenuItem(
                        text = { Text("Export", style = MaterialTheme.typography.bodyMedium, color = PxColors.OnSurface) },
                        onClick = { onMenuDismiss(); onExport() }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to trash", style = MaterialTheme.typography.bodyMedium, color = PxColors.Error) },
                        onClick = onDelete
                    )
                }
            }
        }
    )
}

@Composable
private fun MetadataBar(
    wordCount: Int,
    characterCount: Int,
    readingTimeLabel: String,
    lastSavedAt: Instant?,
    syncStatus: com.oussama_chatri.productivityx.core.enums.SyncStatus,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a").withZone(ZoneId.systemDefault())

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PxColors.SurfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Note info", style = MaterialTheme.typography.labelMedium, color = PxColors.OnSurfaceDim)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetaItem("Words", "$wordCount")
                MetaItem("Characters", "$characterCount")
                MetaItem("Reading time", readingTimeLabel)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last saved: ${lastSavedAt?.let { runCatching { dateFormatter.format(it) }.getOrDefault("—") } ?: "—"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PxColors.OnSurfaceDim
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SyncDot(syncStatus = syncStatus)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = syncStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = PxColors.OnSurfaceDim
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = PxColors.OnSurface)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportSheet(
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PxColors.Surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Export", style = MaterialTheme.typography.titleMedium, color = PxColors.OnBackground)
            Spacer(modifier = Modifier.height(8.dp))
            ExportOption("Export as PDF", "Save a PDF copy of this note") { onDismiss(); onExportPdf() }
            ExportOption("Share", "Share via other apps") { onDismiss(); onShare() }
            ExportOption("Print", "Print this note") { onDismiss(); onPrint() }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExportOption(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = PxColors.SurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = PxColors.OnSurface)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
        }
    }
}

@Composable
private fun AddTagChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(PxColors.SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Add tag",
            tint = PxColors.OnSurfaceDim,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Add tag",
            style = MaterialTheme.typography.labelSmall,
            color = PxColors.OnSurfaceDim
        )
    }
}

@Composable
private fun DeleteNoteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PxColors.Surface,
        title = { Text("Move to trash?", color = PxColors.OnBackground) },
        text = {
            Text(
                "This note will be moved to trash. You can restore it within 30 days.",
                color = PxColors.OnSurfaceDim
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Move to trash", color = PxColors.Error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PxColors.OnSurfaceDim)
            }
        }
    )
}

private fun applyMarkdownAction(action: MarkdownAction, current: TextFieldValue): Pair<String, Int> {
    val text = current.text
    val start = current.selection.start
    val end = current.selection.end
    val selected = runCatching { text.substring(start, end) }.getOrElse { return text to start }

    return when {
        action.suffix.isNotEmpty() -> {
            val wrapped = "${action.prefix}${selected.ifEmpty { "text" }}${action.suffix}"
            val newText = runCatching { text.substring(0, start) + wrapped + text.substring(end) }.getOrElse { return text to start }
            val cursor = if (selected.isEmpty()) start + action.prefix.length + 4
            else start + wrapped.length
            newText to cursor
        }
        else -> {
            val lineStart = text.lastIndexOf('\n', start - 1) + 1
            val newText = runCatching { text.substring(0, lineStart) + action.prefix + text.substring(lineStart) }.getOrElse { return text to start }
            val cursor = start + action.prefix.length
            newText to cursor
        }
    }
}
