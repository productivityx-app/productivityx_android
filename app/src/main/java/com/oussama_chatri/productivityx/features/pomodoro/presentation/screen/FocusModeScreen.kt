package com.oussama_chatri.productivityx.features.pomodoro.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.home.domain.usecase.GetDailyQuoteUseCase
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.TimerState
import com.oussama_chatri.productivityx.features.pomodoro.presentation.event.PomodoroUiEvent
import com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel.PomodoroViewModel

@Composable
fun FocusModeScreen(
    viewModel: PomodoroViewModel,
    onClose: () -> Unit,
    getDailyQuoteUseCase: GetDailyQuoteUseCase // Injected or passed
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val quote = remember { getDailyQuoteUseCase.getQuote() }

    val bgColor by animateColorAsState(
        targetValue = if (state.selectedType == PomodoroType.FOCUS) Color(0xFF0F172A) else Color(0xFF1E1B4B),
        animationSpec = tween(1000),
        label = "focusBg"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgColor, Color.Black)
                )
            )
    ) {
        // Exit button
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Outlined.Close, contentDescription = "Exit Focus Mode", tint = Color.White.copy(alpha = 0.6f))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val remainingSeconds = when (val s = state.timerState) {
                is TimerState.Running -> s.remainingSeconds
                is TimerState.Paused -> s.remainingSeconds
                else -> state.totalSeconds
            }
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeStr = "%02d:%02d".format(minutes, seconds)

            AnimatedContent(
                targetState = timeStr,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                label = "focusTime"
            ) { time ->
                Text(
                    text = time,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Thin,
                        letterSpacing = (-4).sp
                    ),
                    color = Color.White
                )
            }

            Spacer(Modifier.height(48.dp))

            // Quote
            Text(
                text = "\"${quote.first}\"",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light,
                    lineHeight = 32.sp
                ),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "— ${quote.second}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(64.dp))

            // Minimal Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.onEvent(PomodoroUiEvent.ToggleDnd) }) {
                    Icon(
                        Icons.Outlined.DoNotDisturbOn,
                        contentDescription = "DND",
                        tint = if (state.isDndEnabled) PxColors.Primary else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Primary action in focus mode (Pause/Resume only)
                IconButton(
                    onClick = { 
                        if (state.isRunning) viewModel.onEvent(PomodoroUiEvent.PauseTimer)
                        else viewModel.onEvent(PomodoroUiEvent.ResumeTimer)
                    },
                    modifier = Modifier.size(64.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    val icon = if (state.isRunning) Icons.Outlined.Close else Icons.Outlined.PlayArrow
                    Icon(icon, contentDescription = "Toggle", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                IconButton(onClick = { /* Ambient sound picker or toggle */ }) {
                    Icon(
                        Icons.Outlined.VolumeUp,
                        contentDescription = "Ambient Sounds",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
