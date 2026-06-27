package com.oussama_chatri.productivityx.features.pomodoro.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.TimerState
import com.oussama_chatri.productivityx.features.pomodoro.presentation.event.PomodoroUiEvent
import com.oussama_chatri.productivityx.features.pomodoro.presentation.state.PomodoroUiState
import com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel.PomodoroViewModel

@Composable
fun PomodoroScreen(
    modifier: Modifier = Modifier,
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val onEvent = viewModel::onEvent

    LaunchedEffect(viewModel.snackbar) {
        viewModel.snackbar.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Session type selector chips
            SessionTypeSelector(
                selected = state.selectedType,
                enabled  = state.isIdle,
                onSelect = { onEvent(PomodoroUiEvent.SelectType(it)) }
            )

            Spacer(Modifier.height(32.dp))

            // Circular timer
            CircularTimer(
                timerState = state.timerState,
                type       = state.selectedType,
                totalSecs  = state.totalSeconds,
                modifier   = Modifier.size(280.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Cycle dots
            CycleDots(
                total   = state.cyclesBeforeLongBreak,
                current = state.cycleIndex % state.cyclesBeforeLongBreak
            )

            Spacer(Modifier.height(24.dp))

            // Linked task card
            LinkedTaskCard(
                taskTitle     = state.linkedTaskTitle,
                isRunning     = state.isRunning || state.isPaused,
                onLinkTap     = { onEvent(PomodoroUiEvent.ShowTaskPicker) },
                onUnlink      = { onEvent(PomodoroUiEvent.UnlinkTask) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Timer controls
            TimerControls(
                state   = state,
                onEvent = onEvent
            )

            Spacer(Modifier.height(32.dp))

            // Today's stats strip
            state.todayStats?.let { stats ->
                TodayStatsStrip(
                    stats    = stats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter),
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
            .background(PxColors.Surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val types = listOf(
            PomodoroType.FOCUS       to "Focus",
            PomodoroType.SHORT_BREAK to "Short Break",
            PomodoroType.LONG_BREAK  to "Long Break"
        )
        types.forEach { (type, label) ->
            val isSelected = selected == type
            val bgColor by animateColorAsState(
                targetValue   = if (isSelected) typeColor(type) else Color.Transparent,
                animationSpec = tween(200),
                label         = "chipBg_$label"
            )
            val textColor by animateColorAsState(
                targetValue   = if (isSelected) Color.White else PxColors.OnSurfaceDim,
                animationSpec = tween(200),
                label         = "chipText_$label"
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
                    text     = label,
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
        animationSpec = tween(800, easing = EaseInOutCubic),
        label         = "timerProgress"
    )

    val remainingSeconds = when (timerState) {
        is TimerState.Running -> timerState.remainingSeconds
        is TimerState.Paused  -> timerState.remainingSeconds
        else                  -> totalSecs
    }

    val trackColor   = PxColors.SurfaceVariant
    val progressColor = typeColor(type)
    val bgArcColor   = PxColors.Surface

    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius      = (size.minDimension / 2f) - strokeWidth / 2f
            val topLeft     = Offset(center.x - radius, center.y - radius)
            val arcSize     = Size(radius * 2f, radius * 2f)

            // Background circle fill
            drawCircle(color = bgArcColor, radius = radius - strokeWidth / 2f)

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

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color        = progressColor,
                    startAngle   = -90f,
                    sweepAngle   = 360f * animatedProgress,
                    useCenter    = false,
                    topLeft      = topLeft,
                    size         = arcSize,
                    style        = Stroke(width = strokeWidth, cap = StrokeCap.Round)
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
            val timeStr = "%02d:%02d".format(minutes, seconds)

            AnimatedContent(
                targetState = timeStr,
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                },
                label = "timerText"
            ) { time ->
                Text(
                    text       = time,
                    style      = MaterialTheme.typography.displayLarge.copy(
                        fontSize   = 44.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color      = PxColors.OnBackground,
                    textAlign  = TextAlign.Center
                )
            }

            Spacer(Modifier.height(4.dp))

            val sessionLabel = when (timerState) {
                is TimerState.Running  -> typeLabel(timerState.type)
                is TimerState.Paused   -> "${typeLabel(timerState.type)} · Paused"
                is TimerState.Completed -> "Complete! 🎉"
                TimerState.Idle         -> typeLabel(type)
            }

            Text(
                text      = sessionLabel,
                style     = MaterialTheme.typography.bodySmall,
                color     = PxColors.OnSurfaceDim,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Cycle indicator dots ─────────────────────────────────────────────────────

@Composable
private fun CycleDots(
    total: Int,
    current: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.35f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { index ->
            val isActive    = index == current
            val isCompleted = index < current

            val color by animateColorAsState(
                targetValue   = when {
                    isCompleted -> PxColors.Primary
                    isActive    -> PxColors.Primary
                    else        -> PxColors.SurfaceVariant
                },
                animationSpec = tween(200),
                label         = "dotColor_$index"
            )

            Box(
                modifier = Modifier
                    .then(if (isActive) Modifier.scale(pulseScale) else Modifier)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
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
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
            .then(
                if (!hasTask && !isRunning) Modifier
                    .border(1.dp, PxColors.Outline, RoundedCornerShape(12.dp))
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
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text     = taskTitle!!,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = PxColors.OnSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isRunning) {
                    IconButton(
                        onClick  = onUnlink,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Close,
                            contentDescription = "Unlink task",
                            tint               = PxColors.OnSurfaceDim,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            Row(
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Add,
                    contentDescription = null,
                    tint               = PxColors.OnSurfaceDim,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "Link a task to track focus time",
                    style = MaterialTheme.typography.bodySmall,
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
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // Stop / interrupt — only shown when active or paused
        AnimatedVisibility(visible = !state.isIdle) {
            OutlinedCircleButton(
                size    = 56.dp,
                onClick = { onEvent(PomodoroUiEvent.StopAndInterrupt) }
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Stop,
                    contentDescription = "Stop",
                    tint               = PxColors.Error,
                    modifier           = Modifier.size(22.dp)
                )
            }
        }

        // Primary play / pause
        FilledCircleButton(
            size      = 80.dp,
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
            val icon = when {
                state.isRunning -> Icons.Outlined.Pause
                else            -> Icons.Outlined.PlayArrow
            }
            Icon(
                imageVector        = icon,
                contentDescription = if (state.isRunning) "Pause" else "Play",
                tint               = Color.White,
                modifier           = Modifier.size(36.dp)
            )
        }

        // Skip
        AnimatedVisibility(visible = !state.isIdle) {
            OutlinedCircleButton(
                size    = 56.dp,
                onClick = { onEvent(PomodoroUiEvent.SkipTimer) }
            ) {
                Icon(
                    imageVector        = Icons.Outlined.SkipNext,
                    contentDescription = "Skip",
                    tint               = PxColors.OnSurface,
                    modifier           = Modifier.size(22.dp)
                )
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
            .background(PxColors.Surface)
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
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val hours   = stats.totalFocusMinutesToday / 60
        val minutes = stats.totalFocusMinutesToday % 60
        val focusLabel = when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0                 -> "${hours}h"
            else                      -> "${minutes}m"
        }

        StatColumn(value = "${stats.completedFocusSessionsToday}",  label = "Sessions")
        StatDivider()
        StatColumn(value = focusLabel,                               label = "Focus time")
        StatDivider()
        StatColumn(value = "${stats.completedFocusSessionsToday}",   label = "Completed")
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
        Spacer(Modifier.height(2.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = PxColors.OnSurfaceDim
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(1.dp)
            .background(PxColors.Outline)
    )
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
        icon              = {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = PxColors.Warning
            )
        },
        title             = {
            Text(
                text  = "Interrupt session?",
                style = MaterialTheme.typography.titleMedium,
                color = PxColors.OnBackground
            )
        },
        text              = {
            Column {
                Text(
                    text  = "Your partial focus time will still be credited to the linked task.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PxColors.OnSurfaceDim
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = localReason,
                    onValueChange = { localReason = it },
                    placeholder   = { Text("Reason (optional)", color = PxColors.OnSurfaceDim) },
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = PxColors.SurfaceVariant,
                        unfocusedContainerColor = PxColors.SurfaceVariant,
                        focusedBorderColor      = PxColors.Primary,
                        unfocusedBorderColor    = PxColors.Outline,
                        focusedTextColor        = PxColors.OnBackground,
                        unfocusedTextColor      = PxColors.OnBackground
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

fun typeLabel(type: PomodoroType): String = when (type) {
    PomodoroType.FOCUS       -> "Focus Session"
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
