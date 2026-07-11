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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.oussama_chatri.productivityx.R
import coil3.compose.AsyncImage
import com.oussama_chatri.productivityx.features.notes.presentation.util.PdfGenerator
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.components.MarkdownAction
import com.oussama_chatri.productivityx.features.notes.presentation.components.MarkdownToolbar
import com.oussama_chatri.productivityx.features.notes.presentation.components.NoteTagChip
import com.oussama_chatri.productivityx.features.notes.presentation.components.TableCreationDialog
import com.oussama_chatri.productivityx.features.notes.presentation.components.SyncDot
import com.oussama_chatri.productivityx.features.notes.presentation.components.TagPickerSheet
import com.oussama_chatri.productivityx.features.notes.presentation.state.EditorFocusMode
import com.oussama_chatri.productivityx.features.notes.presentation.event.NoteEditorUiEvent
import com.oussama_chatri.productivityx.features.notes.presentation.util.PreviewBlock
import com.oussama_chatri.productivityx.features.notes.presentation.util.PreviewModeRenderer
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.oussama_chatri.productivityx.features.notes.presentation.util.MarkdownVisualTransformation

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

    var pendingImageCursor by remember { mutableStateOf(-1) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) { }
                val cursor = pendingImageCursor
                pendingImageCursor = -1
                viewModel.onEvent(NoteEditorUiEvent.AddImage(uri.toString(), cursor))
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
                        content = uiState.content,
                        imageUrls = uiState.imageUrls
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
    var showTableDialog by rememberSaveable { mutableStateOf(false) }
    var contentValue by remember { mutableStateOf(TextFieldValue(uiState.content)) }

    var zoomScale by remember { mutableStateOf(1f) }
    var zoomOffsetX by remember { mutableStateOf(0f) }
    var zoomOffsetY by remember { mutableStateOf(0f) }
    var fullscreenImageUri by remember { mutableStateOf<String?>(null) }

    val editorTableHeaderTemplate = stringResource(R.string.editor_table_header_template)
    val editorTableCellTemplate = stringResource(R.string.editor_table_cell_template)
    val pdfFallbackName = stringResource(R.string.editor_pdf_filename)
    val shareChooserTitle = stringResource(R.string.editor_share_note)

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
                        isPreviewMode = uiState.isPreviewMode,
                        hasUnsavedChanges = uiState.hasUnsavedChanges,
                        showMenu = showMenu,
                        onBack = onNavigateBack,
                        onTogglePin = { viewModel.onEvent(NoteEditorUiEvent.TogglePin) },
                        onSave = { viewModel.onEvent(NoteEditorUiEvent.Save) },
                        onMoreClick = { showMenu = true },
                        onMenuDismiss = { showMenu = false },
                        onDelete = { showMenu = false; showDeleteConfirm = true },
                        onToggleFocus = { viewModel.onEvent(NoteEditorUiEvent.ToggleFocusMode) },
                        onTogglePreview = { viewModel.onEvent(NoteEditorUiEvent.TogglePreviewMode) },
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
                                val (newText, newCursor) = applyMarkdownAction(action, contentValue, editorTableHeaderTemplate, editorTableCellTemplate)
                                contentValue = TextFieldValue(newText, TextRange(newCursor))
                                viewModel.onEvent(NoteEditorUiEvent.ContentChanged(newText))
                            },
                            onAddImageClick = {
                                pendingImageCursor = contentValue.selection.start
                                photoPicker.launch("image/*")
                            },
                            onAddTableClick = {
                                showTableDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            val newScale = (zoomScale * gestureZoom).coerceIn(1f, 3f)
                            zoomScale = newScale
                            if (newScale > 1f) {
                                zoomOffsetX += pan.x
                                zoomOffsetY += pan.y
                            } else {
                                zoomOffsetX = 0f
                                zoomOffsetY = 0f
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (zoomScale > 1.1f) {
                                    zoomScale = 1f
                                    zoomOffsetX = 0f
                                    zoomOffsetY = 0f
                                } else {
                                    zoomScale = 2f
                                }
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .imePadding()
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = zoomOffsetX
                            translationY = zoomOffsetY
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                        }
                        .then(
                            if (zoomScale <= 1.01f) Modifier.verticalScroll(rememberScrollState())
                            else Modifier
                        )
                        .padding(horizontal = if (isFocusMode) 24.dp else 20.dp)
                        .then(if (isFocusMode) Modifier.background(Color(0xFF0A0A0F)) else Modifier)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Title field
                    if (uiState.isPreviewMode) {
                        Text(
                            text = uiState.title.ifBlank { stringResource(R.string.untitled) },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PxColors.OnBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        BasicTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.onEvent(NoteEditorUiEvent.TitleChanged(it)) },
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                color = PxColors.OnBackground,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(PxColors.Primary),
                            singleLine = false,
                            maxLines = 3,
                            decorationBox = { inner ->
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.untitled),
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
                    }

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

                    // Content
                    if (uiState.isPreviewMode) {
                        val blocks = remember(uiState.content) { PreviewModeRenderer.renderToBlocks(uiState.content) }
                        Column(modifier = Modifier.fillMaxWidth()) {
                            blocks.forEach { block ->
                                when (block) {
                                    is PreviewBlock.Text -> {
                                        Text(
                                            text = block.annotatedString,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = PxColors.OnSurface
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    is PreviewBlock.Image -> {
                                        AsyncImage(
                                            model = block.uri,
                                            contentDescription = block.caption ?: stringResource(R.string.cd_note_image),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .heightIn(max = 300.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { fullscreenImageUri = block.uri }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val imageRegex = remember { Regex("""!\[([^\]]*)\]\(([^)]+)\)""") }
                        val segments = remember(contentValue.text) {
                            val parts = imageRegex.split(contentValue.text)
                            val matches = imageRegex.findAll(contentValue.text).toList()
                            buildList {
                                for ((idx, part) in parts.withIndex()) {
                                    if (part.isNotEmpty()) add(EditSegment.Text(part))
                                    if (idx < matches.size) {
                                        add(EditSegment.Image(matches[idx].groupValues[2], matches[idx].groupValues[1]))
                                    }
                                }
                                if (isEmpty()) add(EditSegment.Text(""))
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            segments.forEachIndexed { index, segment ->
                                key(index) {
                                    when (segment) {
                                        is EditSegment.Text -> {
                                            var segmentValue by remember {
                                                mutableStateOf(TextFieldValue(segment.text, TextRange(segment.text.length)))
                                            }
                                            LaunchedEffect(segment.text) {
                                                if (segmentValue.text != segment.text) {
                                                    segmentValue = TextFieldValue(segment.text, TextRange(segment.text.length))
                                                }
                                            }
                                            BasicTextField(
                                                value = segmentValue,
                                                onValueChange = { newVal ->
                                                    segmentValue = newVal
                                                    val fullText = buildString {
                                                        segments.forEachIndexed { i, seg ->
                                                            append(
                                                                when (seg) {
                                                                    is EditSegment.Text ->
                                                                        if (i == index) newVal.text else seg.text
                                                                    is EditSegment.Image ->
                                                                        "![${seg.alt}](${seg.uri})"
                                                                }
                                                            )
                                                        }
                                                    }
                                                    val offsetBefore = segments.take(index).sumOf { seg ->
                                                        when (seg) {
                                                            is EditSegment.Text -> seg.text.length
                                                            is EditSegment.Image -> "![${seg.alt}](${seg.uri})".length
                                                        }
                                                    }
                                                    contentValue = TextFieldValue(fullText, TextRange(
                                                        offsetBefore + newVal.selection.start,
                                                        offsetBefore + newVal.selection.end
                                                    ))
                                                    viewModel.onEvent(NoteEditorUiEvent.ContentChanged(fullText))
                                                },
                                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                    color = PxColors.OnSurface,
                                                    lineHeight = if (isFocusMode) 28.sp else MaterialTheme.typography.bodyLarge.lineHeight
                                                ),
                                                cursorBrush = SolidColor(PxColors.Primary),
                                                visualTransformation = MarkdownVisualTransformation(),
                                                decorationBox = { inner ->
                                                    if (segment.text.isEmpty() && segments.size == 1) {
                                                        Text(
                                                            text = stringResource(R.string.editor_start_writing),
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = PxColors.OnSurfaceDim
                                                        )
                                                    }
                                                    inner()
                                                },
                                                modifier = Modifier.fillMaxWidth().animateContentSize()
                                            )
                                        }
                                        is EditSegment.Image -> {
                                            AsyncImage(
                                                model = segment.uri,
                                                contentDescription = stringResource(R.string.cd_note_image),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .heightIn(max = 300.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { fullscreenImageUri = segment.uri }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

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
                                text = stringResource(R.string.dialog_unsaved_changes_title),
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
                                text = stringResource(R.string.editor_saved),
                                style = MaterialTheme.typography.labelSmall,
                                color = PxColors.Success
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }

                // Zoom reset FAB
                if (zoomScale > 1.05f) {
                    IconButton(
                        onClick = {
                            zoomScale = 1f
                            zoomOffsetX = 0f
                            zoomOffsetY = 0f
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(40.dp)
                            .background(PxColors.SurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            Icons.Outlined.CenterFocusWeak,
                            contentDescription = stringResource(R.string.reset_zoom),
                            tint = PxColors.OnSurface
                        )
                    }
                }
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
                    contentDescription = stringResource(R.string.exit_focus_mode),
                    tint = PxColors.OnSurface
                )
            }
        }
    }

    // Fullscreen image dialog
    if (fullscreenImageUri != null) {
        val dialogUri = fullscreenImageUri!!
        Dialog(
            onDismissRequest = { fullscreenImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { fullscreenImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = dialogUri,
                    contentDescription = stringResource(R.string.fullscreen_image),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .clickable { /* prevent dismiss on image tap */ }
                )
                IconButton(
                    onClick = { fullscreenImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = Color.White
                    )
                }
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

    // Table creation dialog
    if (showTableDialog) {
        TableCreationDialog(
            onDismiss = { showTableDialog = false },
            onConfirm = { rows, cols, hasHeader ->
                showTableDialog = false
                val tableMarkdown = buildTableMarkdown(rows, cols, hasHeader, editorTableHeaderTemplate, editorTableCellTemplate)
                val cursor = contentValue.selection.start
                val newText = contentValue.text.substring(0, cursor) +
                    tableMarkdown + contentValue.text.substring(contentValue.selection.end)
                contentValue = TextFieldValue(newText, TextRange(cursor + tableMarkdown.length))
                viewModel.onEvent(NoteEditorUiEvent.ContentChanged(newText))
            }
        )
    }

    // Export sheet
    if (uiState.showExportSheet) {
        ExportSheet(
            onDismiss = { viewModel.onEvent(NoteEditorUiEvent.HideExportSheet) },
            onExportPdf = { pdfCreator.launch("${uiState.title.ifBlank { pdfFallbackName }}.pdf") },
            onShare = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, uiState.title)
                    putExtra(Intent.EXTRA_TEXT, "${uiState.title}\n\n${uiState.content}")
                }
                context.startActivity(Intent.createChooser(intent, shareChooserTitle))
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
    isPreviewMode: Boolean,
    hasUnsavedChanges: Boolean,
    showMenu: Boolean,
    onBack: () -> Unit,
    onTogglePin: () -> Unit,
    onSave: () -> Unit,
    onMoreClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onDelete: () -> Unit,
    onToggleFocus: () -> Unit,
    onTogglePreview: () -> Unit,
    onToggleMetadata: () -> Unit,
    onExport: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = PxColors.OnSurface
                )
            }
        },
        title = {
            Text(
                text = title.ifBlank { stringResource(R.string.untitled) }.take(30),
                style = MaterialTheme.typography.titleMedium,
                color = PxColors.OnSurface,
                maxLines = 1
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background),
        actions = {
            IconButton(onClick = onTogglePreview) {
                Icon(
                    if (isPreviewMode) Icons.Outlined.Edit else Icons.Outlined.Visibility,
                    contentDescription = if (isPreviewMode) stringResource(R.string.edit_mode) else stringResource(R.string.preview_mode),
                    tint = if (isPreviewMode) PxColors.Primary else PxColors.OnSurfaceDim
                )
            }
            IconButton(onClick = onToggleFocus) {
                Icon(Icons.Outlined.CenterFocusWeak, contentDescription = stringResource(R.string.focus_mode), tint = PxColors.OnSurfaceDim)
            }
            AnimatedVisibility(visible = hasUnsavedChanges) {
                TextButton(onClick = onSave) {
                    Text(stringResource(R.string.save), color = PxColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            }
            IconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) stringResource(R.string.unpin) else stringResource(R.string.pin),
                    tint = if (isPinned) PxColors.Primary else PxColors.OnSurfaceDim
                )
            }
            Box {
                IconButton(onClick = onMoreClick) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.more), tint = PxColors.OnSurface)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onMenuDismiss,
                    containerColor = PxColors.Surface
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.show_metadata), style = MaterialTheme.typography.bodyMedium, color = PxColors.OnSurface) },
                        onClick = { onMenuDismiss(); onToggleMetadata() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.export), style = MaterialTheme.typography.bodyMedium, color = PxColors.OnSurface) },
                        onClick = { onMenuDismiss(); onExport() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.move_to_trash), style = MaterialTheme.typography.bodyMedium, color = PxColors.Error) },
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
            Text(stringResource(R.string.note_info), style = MaterialTheme.typography.labelMedium, color = PxColors.OnSurfaceDim)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetaItem(stringResource(R.string.words), "$wordCount")
                MetaItem(stringResource(R.string.characters), "$characterCount")
                MetaItem(stringResource(R.string.reading_time), readingTimeLabel)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.last_saved, lastSavedAt?.let { runCatching { dateFormatter.format(it) }.getOrDefault("—") } ?: "—"),
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
            Text(stringResource(R.string.export), style = MaterialTheme.typography.titleMedium, color = PxColors.OnBackground)
            Spacer(modifier = Modifier.height(8.dp))
            ExportOption(stringResource(R.string.export_as_pdf), stringResource(R.string.export_as_pdf_subtitle)) { onDismiss(); onExportPdf() }
            ExportOption(stringResource(R.string.share), stringResource(R.string.share_subtitle)) { onDismiss(); onShare() }
            ExportOption(stringResource(R.string.print), stringResource(R.string.print_subtitle)) { onDismiss(); onPrint() }
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
            contentDescription = stringResource(R.string.add_tag),
            tint = PxColors.OnSurfaceDim,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(R.string.add_tag),
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
        title = { Text(stringResource(R.string.move_to_trash_confirm), color = PxColors.OnBackground) },
        text = {
            Text(
                stringResource(R.string.move_to_trash_message),
                color = PxColors.OnSurfaceDim
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.move_to_trash), color = PxColors.Error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = PxColors.OnSurfaceDim)
            }
        }
    )
}

private fun applyMarkdownAction(action: MarkdownAction, current: TextFieldValue, headerTemplate: String = "Header %d", cellLabel: String = "Cell"): Pair<String, Int> {
    val text = current.text
    val start = current.selection.start
    val end = current.selection.end
    val selected = runCatching { text.substring(start, end) }.getOrElse { return text to start }

    return when (action) {
        is MarkdownAction.InsertTable -> {
            val tableStr = buildTableMarkdown(action.rows, action.cols, action.hasHeader, headerTemplate, cellLabel)
            val newText = text.substring(0, start) + tableStr + text.substring(end)
            newText to (start + tableStr.length)
        }
        is MarkdownAction.HighlightColor -> {
            val hex = action.color.toArgbHex()
            val prefix = "==color:$hex:"
            val suffix = "=="
            val wrapped = "$prefix${selected.ifEmpty { "text" }}$suffix"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            val cursor = if (selected.isEmpty()) start + prefix.length + 4
            else start + wrapped.length
            newText to cursor
        }
        is MarkdownAction.TextColor -> {
            val hex = action.color.toArgbHex()
            val prefix = "{color:$hex}"
            val suffix = "{/color}"
            val wrapped = "$prefix${selected.ifEmpty { "text" }}$suffix"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            val cursor = if (selected.isEmpty()) start + prefix.length + 4
            else start + wrapped.length
            newText to cursor
        }
        is MarkdownAction.FontSize -> {
            val prefix = "{size:${action.size}}"
            val suffix = "{/size}"
            val wrapped = "$prefix${selected.ifEmpty { "text" }}$suffix"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            val cursor = if (selected.isEmpty()) start + prefix.length + 4
            else start + wrapped.length
            newText to cursor
        }
        else -> {
            when {
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
    }
}

private fun buildTableMarkdown(rows: Int, cols: Int, hasHeader: Boolean, headerTemplate: String = "Header %d", cellLabel: String = "Cell"): String {
    return buildString {
        val headerRow = "| " + (1..cols).joinToString(" | ") { String.format(headerTemplate, it) } + " |\n"
        val separatorRow = "|" + (1..cols).joinToString("|") { "---" } + "|\n"
        val dataRow = "| " + (1..cols).joinToString(" | ") { cellLabel } + " |\n"

        if (hasHeader) {
            append(headerRow)
            append(separatorRow)
        } else {
            append(separatorRow)
        }
        repeat(if (hasHeader) rows - 1 else rows) {
            append(dataRow)
        }
        append("\n")
    }
}

private fun Color.toArgbHex(): String {
    val r = (this.red * 255).toInt().coerceIn(0, 255)
    val g = (this.green * 255).toInt().coerceIn(0, 255)
    val b = (this.blue * 255).toInt().coerceIn(0, 255)
    return "#%02x%02x%02x".format(r, g, b)
}

private sealed class EditSegment {
    class Text(val text: String) : EditSegment()
    class Image(val uri: String, val alt: String) : EditSegment()
}
