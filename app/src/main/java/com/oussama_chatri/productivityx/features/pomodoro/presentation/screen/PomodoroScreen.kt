package com.oussama_chatri.productivityx.features.pomodoro.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.TimerState
import com.oussama_chatri.productivityx.features.pomodoro.presentation.event.PomodoroUiEvent
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.AmbientSound
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.PomodoroUiState
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.SessionHistoryItem
import com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel.PomodoroViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun PomodoroScreen(
    modifier: Modifier = Modifier,
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val onEvent = viewModel::onEvent
    var showSoundPicker by remember { mutableStateOf(false) }

    val bgPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) { }
                onEvent(PomodoroUiEvent.SelectBackground(uri.toString()))
            }
        }
    )

    LaunchedEffect(viewModel.snackbar) {
        viewModel.snackbar.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Phase-based background color shift
    val backgroundColor by animateColorAsState(
        targetValue = when {
            state.isFocusMode -> PxColors.Surface
            state.selectedType == PomodoroType.FOCUS -> PxColors.Background
            else -> PxColors.SurfaceVariant
        },
        animationSpec = tween(1000),
        label = "phaseBackground"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(backgroundColor)) {
        val isTablet = maxWidth >= 600.dp
        val timerSize = when {
            state.isFocusMode -> if (isTablet) 400.dp else 320.dp
            isTablet -> 380.dp
            else -> 300.dp
        }

        // Background Image & Scrim
        if (state.backgroundImageUri != null) {
            AsyncImage(
                model = state.backgroundImageUri,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )
        }

        if (isTablet && !state.isFocusMode) {
            // ── TABLET LAYOUT ──
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Timer area
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))

                    // Icons row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onEvent(PomodoroUiEvent.ToggleDnd) }) {
                            Icon(Icons.Outlined.DoNotDisturbOn, contentDescription = "DND",
                                tint = if (state.isDndEnabled) PxColors.Primary else PxColors.OnSurfaceDim)
                        }
                        IconButton(onClick = { bgPicker.launch("image/*") }) {
                            Icon(Icons.Outlined.Wallpaper, contentDescription = "Background", tint = PxColors.OnSurfaceDim)
                        }
                        IconButton(onClick = { showSoundPicker = true }) {
                            Icon(Icons.Outlined.MusicNote, contentDescription = "Sounds", tint = PxColors.OnSurfaceDim)
                        }
                        IconButton(onClick = { onEvent(PomodoroUiEvent.ToggleFocusMode) }) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = "Focus Mode",
                                tint = if (state.isFocusMode) PxColors.Primary else PxColors.OnSurfaceDim)
                        }
                    }

                    SessionTypeSelector(
                        selected = state.selectedType,
                        enabled = state.isIdle,
                        onSelect = { onEvent(PomodoroUiEvent.SelectType(it)) }
                    )

                    Spacer(Modifier.height(32.dp))

                    val isBreak = state.selectedType != PomodoroType.FOCUS
                    val breathingScale by if ((isBreak || state.isFocusMode) && (state.isRunning || state.isPaused)) {
                        val infiniteTransition = rememberInfiniteTransition(label = "breathing")
                        infiniteTransition.animateFloat(
                            initialValue = 1f, targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(animation = tween(4000, easing = EaseInOutCubic), repeatMode = RepeatMode.Reverse),
                            label = "breathingScale"
                        )
                    } else { remember { mutableStateOf(1f) } }

                    CircularTimer(
                        timerState = state.timerState, type = state.selectedType,
                        totalSecs = state.totalSeconds, isFocusMode = state.isFocusMode,
                        modifier = Modifier.size(timerSize).scale(breathingScale)
                    )

                    Spacer(Modifier.height(24.dp))
                    CycleDots(total = state.cyclesBeforeLongBreak, current = state.cycleIndex % state.cyclesBeforeLongBreak)
                    Spacer(Modifier.height(24.dp))
                    LinkedTaskCard(taskTitle = state.linkedTaskTitle, isRunning = state.isRunning || state.isPaused,
                        onLinkTap = { onEvent(PomodoroUiEvent.ShowTaskPicker) }, onUnlink = { onEvent(PomodoroUiEvent.UnlinkTask) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))

                    Spacer(Modifier.height(32.dp))
                    TimerControls(state = state, onEvent = onEvent)
                    Spacer(Modifier.height(48.dp))
                }

                // Right: Stats area
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(48.dp))
                    state.todayStats?.let { stats ->
                        TodayStatsStrip(stats = stats, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(32.dp))
                    HistoryTimelinePreview(items = state.sessionTimeline, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            // ── PHONE / FOCUS MODE LAYOUT ──
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!state.isFocusMode) {
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onEvent(PomodoroUiEvent.ToggleDnd) }) {
                            Icon(Icons.Outlined.DoNotDisturbOn, contentDescription = "DND",
                                tint = if (state.isDndEnabled) PxColors.Primary else PxColors.OnSurfaceDim)
                        }
                        IconButton(onClick = { bgPicker.launch("image/*") }) {
                            Icon(Icons.Outlined.Wallpaper, contentDescription = "Background", tint = PxColors.OnSurfaceDim)
                        }
                        IconButton(onClick = { showSoundPicker = true }) {
                            Icon(Icons.Outlined.MusicNote, contentDescription = "Sounds", tint = PxColors.OnSurfaceDim)
                        }
                        IconButton(onClick = { onEvent(PomodoroUiEvent.ToggleFocusMode) }) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = "Focus Mode",
                                tint = if (state.isFocusMode) PxColors.Primary else PxColors.OnSurfaceDim)
                        }
                    }

                    SessionTypeSelector(
                        selected = state.selectedType,
                        enabled = state.isIdle,
                        onSelect = { onEvent(PomodoroUiEvent.SelectType(it)) }
                    )
                } else {
                    Spacer(Modifier.height(48.dp))
                }

                Spacer(Modifier.height(32.dp))

                val isBreak = state.selectedType != PomodoroType.FOCUS
                val breathingScale by if ((isBreak || state.isFocusMode) && (state.isRunning || state.isPaused)) {
                    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
                    infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(animation = tween(4000, easing = EaseInOutCubic), repeatMode = RepeatMode.Reverse),
                        label = "breathingScale"
                    )
                } else { remember { mutableStateOf(1f) } }

                CircularTimer(
                    timerState = state.timerState, type = state.selectedType,
                    totalSecs = state.totalSeconds, isFocusMode = state.isFocusMode,
                    modifier = Modifier.size(timerSize).scale(breathingScale)
                )

                if (state.isFocusMode) {
                    Spacer(Modifier.height(48.dp))
                    state.motivationalQuote?.let { quote ->
                        Text(text = "\"$quote\"", style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                            color = PxColors.OnBackground.copy(alpha = 0.8f), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
                    }
                } else {
                    Spacer(Modifier.height(24.dp))
                    CycleDots(total = state.cyclesBeforeLongBreak, current = state.cycleIndex % state.cyclesBeforeLongBreak)
                    Spacer(Modifier.height(24.dp))
                    LinkedTaskCard(taskTitle = state.linkedTaskTitle, isRunning = state.isRunning || state.isPaused,
                        onLinkTap = { onEvent(PomodoroUiEvent.ShowTaskPicker) }, onUnlink = { onEvent(PomodoroUiEvent.UnlinkTask) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                }

                Spacer(Modifier.height(32.dp))
                TimerControls(state = state, onEvent = onEvent)

                if (!state.isFocusMode) {
                    Spacer(Modifier.height(32.dp))
                    state.todayStats?.let { stats ->
                        TodayStatsStrip(stats = stats, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    HistoryTimelinePreview(items = state.sessionTimeline, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                }

                Spacer(Modifier.height(48.dp))
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    // Task picker sheet
    if (state.showTaskPickerSheet) {
        AlertDialog(
            onDismissRequest = { onEvent(PomodoroUiEvent.SelectTask("", "")) },
            containerColor = PxColors.Surface,
            title = { Text("Link a Task", color = PxColors.OnBackground, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (tasks.isEmpty()) {
                        Text("No active tasks available.", color = PxColors.OnSurfaceDim)
                    } else {
                        tasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEvent(PomodoroUiEvent.SelectTask(task.id, task.title)) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Outlined.CheckCircle, null, tint = PxColors.Primary, modifier = Modifier.size(18.dp))
                                Text(task.title, color = PxColors.OnSurface, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onEvent(PomodoroUiEvent.SelectTask("", "")) }) {
                    Text("Cancel", color = PxColors.OnSurfaceDim)
                }
            }
        )
    }

    // Sound picker sheet
    if (showSoundPicker) {
        AlertDialog(
            onDismissRequest = { showSoundPicker = false },
            containerColor = PxColors.Surface,
            title = { Text("Ambient Sound", color = PxColors.OnBackground, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    AmbientSound.entries.forEach { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(PomodoroUiEvent.SelectAmbientSound(sound))
                                    showSoundPicker = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isSelected = state.selectedAmbientSound == sound
                            Icon(
                                if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.MusicNote,
                                null,
                                tint = if (isSelected) PxColors.Primary else PxColors.OnSurfaceDim,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when (sound) {
                                    AmbientSound.NONE -> "None"
                                    AmbientSound.RAIN -> "Rain"
                                    AmbientSound.CAFE -> "Cafe"
                                    AmbientSound.WHITE_NOISE -> "White Noise"
                                    AmbientSound.NATURE -> "Nature"
                                },
                                color = if (isSelected) PxColors.Primary else PxColors.OnSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSoundPicker = false }) {
                    Text("Close", color = PxColors.OnSurfaceDim)
                }
            }
        )
    }

    // Interrupt reason dialog
    if (state.showInterruptDialog) {
        InterruptDialog(
            reason   = state.interruptReason,
            onConfirm = { reason -> onEvent(PomodoroUiEvent.ConfirmInterrupt(reason)) },
            onDismiss = { onEvent(PomodoroUiEvent.DismissInterruptDialog) }
        )
    }
}

// ── Session type chips ───────────────────────────────────────────────────────

@Composable
private fun SessionTypeSelector(
    selected: PomodoroType,
    enabled: Boolean,
    onSelect: (PomodoroType) -> Unit
) {
    Row(
        modifier              = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(PxColors.Surface.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val types = listOf(
            PomodoroType.FOCUS,
            PomodoroType.SHORT_BREAK,
            PomodoroType.LONG_BREAK
        )
        types.forEach { type ->
            val isSelected = selected == type
            val bgColor by animateColorAsState(
                targetValue   = if (isSelected) typeColor(type) else Color.Transparent,
                animationSpec = tween(200),
                label         = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue   = if (isSelected) PxColors.OnBackground else PxColors.OnSurfaceDim,
                animationSpec = tween(200),
                label         = "chipText"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(bgColor)
                    .then(if (enabled) Modifier.clickable { onSelect(type) } else Modifier)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text     = when (type) {
                        PomodoroType.FOCUS -> "Focus"
                        PomodoroType.SHORT_BREAK -> "Short Break"
                        PomodoroType.LONG_BREAK -> "Long Break"
                    },
                    style    = MaterialTheme.typography.labelMedium,
                    color    = textColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ── Circular timer canvas ────────────────────────────────────────────────────

@Composable
private fun CircularTimer(
    timerState: TimerState,
    type: PomodoroType,
    totalSecs: Int,
    isFocusMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val progress = when (timerState) {
        is TimerState.Running  -> timerState.progressFraction
        is TimerState.Paused   -> timerState.progressFraction
        is TimerState.Completed -> 1f
        TimerState.Idle         -> 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1000, easing = EaseInOutCubic),
        label         = "timerProgress"
    )

    val remainingSeconds = when (timerState) {
        is TimerState.Running -> timerState.remainingSeconds
        is TimerState.Paused  -> timerState.remainingSeconds
        else                  -> totalSecs
    }

    val trackColor   = PxColors.SurfaceVariant.copy(alpha = if (isFocusMode) 0.1f else 0.3f)
    val mainColor    = typeColor(type)
    val secondaryColor = when(type) {
        PomodoroType.FOCUS -> PxColors.Warning 
        else -> PxColors.Success 
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = (if (isFocusMode) 10.dp else 14.dp).toPx()
            val radius      = (size.minDimension / 2f) - strokeWidth / 2f
            val topLeft     = Offset(center.x - radius, center.y - radius)
            val arcSize     = Size(radius * 2f, radius * 2f)

            // Background circle glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(mainColor.copy(alpha = 0.15f), Color.Transparent),
                    center = center,
                    radius = radius + strokeWidth * 2
                ),
                radius = radius + strokeWidth * 2
            )

            // Track arc
            drawArc(
                color        = trackColor,
                startAngle   = -90f,
                sweepAngle   = 360f,
                useCenter    = false,
                topLeft      = topLeft,
                size         = arcSize,
                style        = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc with gradient
            if (animatedProgress > 0f) {
                drawArc(
                    brush        = Brush.sweepGradient(
                        0f to mainColor,
                        0.5f to secondaryColor,
                        1f to mainColor,
                        center = center
                    ),
                    startAngle   = -90f,
                    sweepAngle   = 360f * animatedProgress,
                    useCenter    = false,
                    topLeft      = topLeft,
                    size         = arcSize,
                    style        = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Animated end point glow
                val angle = (360f * animatedProgress - 90f) * (Math.PI / 180f).toFloat()
                val x = center.x + radius * Math.cos(angle.toDouble()).toFloat()
                val y = center.y + radius * Math.sin(angle.toDouble()).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth / 2.5f,
                    center = Offset(x, y)
                )
            }
        }

        // Center time display 
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60

            Row(verticalAlignment = Alignment.CenterVertically) {
                FlipCountdownUnit(value = minutes)
                Text(":", style = MaterialTheme.typography.displayLarge.copy(fontSize = if (isFocusMode) 54.sp else 44.sp, fontWeight = FontWeight.Bold), color = Color.White)
                FlipCountdownUnit(value = seconds, isFocusMode = isFocusMode)
            }

            Spacer(Modifier.height(if (isFocusMode) 4.dp else 8.dp))

            val sessionLabel = when (timerState) {
                is TimerState.Running  -> typeLabel(timerState.type)
                is TimerState.Paused   -> "Paused"
                is TimerState.Completed -> "Done! 🎉"
                TimerState.Idle         -> typeLabel(type)
            }

            Text(
                text      = sessionLabel.uppercase(),
                style     = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                color     = PxColors.OnSurfaceDim,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FlipCountdownUnit(value: Int, isFocusMode: Boolean = false) {
    val text = "%02d".format(value)
    Crossfade(
        targetState = text,
        animationSpec = tween(600),
        label = "flipUnit"
    ) { target ->
        Text(
            text       = target,
            style      = MaterialTheme.typography.displayLarge.copy(
                fontSize   = if (isFocusMode) 78.sp else 64.sp,
                fontWeight = FontWeight.Bold
            ),
            color      = Color.White,
            textAlign  = TextAlign.Center
        )
    }
}

@Composable
private fun HistoryTimelinePreview(
    items: List<SessionHistoryItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Today's Sessions", style = MaterialTheme.typography.titleSmall, color = PxColors.OnBackground)
            Icon(Icons.Outlined.History, contentDescription = null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(12.dp))
        
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PxColors.Surface.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text("No sessions yet today", style = MaterialTheme.typography.bodySmall, color = PxColors.OnBackground.copy(alpha = 0.4f))
            }
        } else {
            items.take(3).forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(typeColor(item.type)))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        item.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurfaceDim
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        item.taskTitle ?: "General Focus",
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${item.durationMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = PxColors.OnSurfaceDim
                    )
                }
                if (index < items.size - 1 && index < 2) {
                    HorizontalDivider(color = PxColors.Outline.copy(alpha = 0.2f), modifier = Modifier.padding(start = 20.dp))
                }
            }
        }
    }
}

// ── Cycle indicator dots ─────────────────────────────────────────────────────

@Composable
private fun CycleDots(
    total: Int,
    current: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(total) { index ->
            val isActive    = index == current
            val isCompleted = index < current

            val color by animateColorAsState(
                targetValue   = when {
                    isCompleted -> PxColors.Primary
                    isActive    -> PxColors.Primary
                    else        -> PxColors.SurfaceVariant
                },
                label         = "dotColor"
            )

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(if (isActive) Modifier.border(2.dp, PxColors.OnBackground, CircleShape) else Modifier)
            )
        }
    }
}

// ── Linked task card ─────────────────────────────────────────────────────────

@Composable
private fun LinkedTaskCard(
    taskTitle: String?,
    isRunning: Boolean,
    onLinkTap: () -> Unit,
    onUnlink: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasTask = taskTitle != null

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PxColors.Surface.copy(alpha = 0.5f))
            .then(
                if (!hasTask && !isRunning) Modifier
                    .border(1.dp, PxColors.Outline, RoundedCornerShape(16.dp))
                    .clickable(onClick = onLinkTap)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (hasTask) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint               = PxColors.Primary,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text     = taskTitle!!,
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = PxColors.OnSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isRunning) {
                    IconButton(onClick = onUnlink) {
                        Icon(Icons.Outlined.Close, contentDescription = "Unlink", tint = PxColors.OnSurfaceDim)
                    }
                }
            }
        } else {
            Row(
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = PxColors.OnSurfaceDim)
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "Link a Task",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurfaceDim
                )
            }
        }
    }
}

// ── Timer controls ───────────────────────────────────────────────────────────

@Composable
private fun TimerControls(
    state: PomodoroUiState,
    onEvent: (PomodoroUiEvent) -> Unit
) {
    val density = LocalDensity.current
    val width = 300.dp
    val sizePx = with(density) { width.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Stop / interrupt
            AnimatedVisibility(visible = !state.isIdle) {
                OutlinedCircleButton(size = 56.dp, onClick = { onEvent(PomodoroUiEvent.StopAndInterrupt) }) {
                    Icon(Icons.Outlined.Stop, contentDescription = "Stop", tint = PxColors.Error)
                }
            }

            // Primary play / pause 
            FilledCircleButton(
                size      = 88.dp,
                color     = typeColor(state.selectedType),
                isLoading = state.isLoadingStart || state.isLoadingEnd,
                onClick   = {
                    when {
                        state.isIdle    -> onEvent(PomodoroUiEvent.StartSession)
                        state.isRunning -> onEvent(PomodoroUiEvent.PauseTimer)
                        state.isPaused  -> onEvent(PomodoroUiEvent.ResumeTimer)
                    }
                }
            ) {
                AnimatedContent(
                    targetState = state.isRunning,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "playPause"
                ) { isRunning ->
                    val icon = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow
                    Icon(icon, contentDescription = "Play/Pause", tint = PxColors.OnBackground, modifier = Modifier.size(40.dp))
                }
            }

            // Add 1 minute extension
            AnimatedVisibility(visible = !state.isIdle) {
                OutlinedCircleButton(size = 56.dp, onClick = { onEvent(PomodoroUiEvent.Extend1Min) }) {
                    Icon(Icons.Outlined.MoreTime, contentDescription = "+1 Min", tint = PxColors.OnSurface)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Swipe to skip 
        if (!state.isIdle) {
            Box(
                modifier = Modifier
                    .width(width)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(PxColors.Surface.copy(alpha = 0.3f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    "SWIPE TO SKIP",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = PxColors.OnSurfaceDim
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PxColors.OnBackground.copy(alpha = 0.15f))
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                offsetX = (offsetX + delta).coerceIn(0f, sizePx - with(density) { 56.dp.toPx() })
                            },
                            onDragStopped = {
                                if (offsetX > sizePx * 0.6f) {
                                    onEvent(PomodoroUiEvent.SkipTimer)
                                }
                                offsetX = 0f
                            }
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.SkipNext, contentDescription = null, tint = PxColors.OnBackground)
                }
            }
        }
    }
}

@Composable
private fun FilledCircleButton(
    size: Dp,
    color: Color,
    isLoading: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .clickable(enabled = !isLoading, onClick = onClick)
    ) {
        content()
    }
}

@Composable
private fun OutlinedCircleButton(
    size: Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, PxColors.Outline, CircleShape)
            .background(PxColors.Surface.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
    ) {
        content()
    }
}

// ── Today's stats ─────────────────────────────────────────────────────────────

@Composable
private fun TodayStatsStrip(
    stats: PomodoroStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PxColors.Surface.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val hours   = stats.totalFocusMinutesToday / 60
        val minutes = stats.totalFocusMinutesToday % 60
        val focusLabel = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

        StatColumn(value = "${stats.completedFocusSessionsToday}",  label = "SESSIONS")
        Box(modifier = Modifier.height(30.dp).width(1.dp).background(PxColors.Outline))
        StatColumn(value = focusLabel,                               label = "FOCUS TIME")
        Box(modifier = Modifier.height(30.dp).width(1.dp).background(PxColors.Outline))
        StatColumn(value = "${stats.currentStreak}",                 label = "STREAK")
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = PxColors.OnBackground
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = PxColors.OnSurfaceDim
        )
    }
}

// ── Interrupt dialog ──────────────────────────────────────────────────────────

@Composable
private fun InterruptDialog(
    reason: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var localReason by remember { mutableStateOf(reason) }

    AlertDialog(
        onDismissRequest  = onDismiss,
        containerColor    = PxColors.Surface,
        icon              = { Icon(Icons.Outlined.Warning, contentDescription = null, tint = PxColors.Warning) },
        title             = { Text("Interrupt session?") },
        text              = {
            Column {
                Text("Your partial focus time will still be credited to the linked task.", style = MaterialTheme.typography.bodySmall, color = PxColors.OnSurfaceDim)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = localReason,
                    onValueChange = { localReason = it },
                    placeholder   = { Text("Reason (optional)") },
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = PxColors.SurfaceVariant,
                        unfocusedContainerColor = PxColors.SurfaceVariant
                    ),
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton     = {
            TextButton(onClick = { onConfirm(localReason) }) {
                Text("Interrupt", color = PxColors.Error)
            }
        },
        dismissButton     = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PxColors.OnSurfaceDim)
            }
        }
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun typeColor(type: PomodoroType): Color = when (type) {
    PomodoroType.FOCUS       -> PxColors.Primary 
    PomodoroType.SHORT_BREAK -> PxColors.Success 
    PomodoroType.LONG_BREAK  -> PxColors.Info
}

@Composable
fun typeLabel(type: PomodoroType): String = when (type) {
    PomodoroType.FOCUS       -> "Focus"
    PomodoroType.SHORT_BREAK -> "Short Break"
    PomodoroType.LONG_BREAK  -> "Long Break"
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun PomodoroScreenPreview() {
    ProductivityXTheme {
        PomodoroScreen(modifier = Modifier)
    }
}
