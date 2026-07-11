package com.oussama_chatri.productivityx.features.pomodoro.presentation.screen

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oussama_chatri.productivityx.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.enums.PomodoroType
import com.oussama_chatri.productivityx.core.ui.components.PxEmptyState
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroSession
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel.SessionHistoryViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SessionHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: SessionHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SessionHistoryContent(
        sessions       = state.sessions,
        stats          = state.stats,
        isLoading      = state.isLoading,
        onNavigateBack = onNavigateBack,
        onLoadMore     = viewModel::loadNextPage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionHistoryContent(
    sessions: List<PomodoroSession>,
    stats: PomodoroStats?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onLoadMore: () -> Unit
) {
    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.pomodoro_history_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = PxColors.OnBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = PxColors.OnSurface
                    )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PxColors.Background
                )
            )
        }
    ) { innerPadding ->

        if (sessions.isEmpty() && !isLoading) {
            PxEmptyState(
                icon     = Icons.Outlined.History,
                title    = stringResource(R.string.pomodoro_history_empty_title),
                subtitle = stringResource(R.string.pomodoro_history_empty_body),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            return@Scaffold
        }

        val grouped = sessions
            .groupBy { it.startedAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .toSortedMap(compareByDescending { it })

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stats?.let {
                item {
                    StatsSummaryCard(stats = it)
                    Spacer(Modifier.height(8.dp))
                }
            }

            grouped.forEach { (date, daySessions) ->
                item(key = date.toString()) {
                    DateGroupHeader(date = date)
                    Spacer(Modifier.height(6.dp))
                }
                items(daySessions, key = { it.id }) { session ->
                    SessionHistoryItem(session = session)
                }
                item { Spacer(Modifier.height(4.dp)) }
            }
        }
    }
}

@Composable
private fun StatsSummaryCard(stats: PomodoroStats) {
    val hours   = stats.totalFocusMinutesToday / 60
    val minutes = stats.totalFocusMinutesToday % 60
    val label = when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0                 -> "${hours}h"
        else                      -> "${minutes}m"
    }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "${stats.completedFocusSessionsToday}",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = PxColors.Primary
            )
            Text(stringResource(R.string.pomodoro_today_sessions), style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
        }

        Box(modifier = Modifier.height(32.dp).width(1.dp).background(PxColors.Outline))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = PxColors.Warning
            )
            Text(stringResource(R.string.pomodoro_focus_time_label), style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
        }
    }
}

@Composable
private fun DateGroupHeader(date: LocalDate) {
    val today     = LocalDate.now()
    val yesterday = today.minusDays(1)
    val label = when (date) {
        today     -> stringResource(R.string.today)
        yesterday -> stringResource(R.string.yesterday)
        else      -> runCatching { DateTimeFormatter.ofPattern("EEEE, MMM d").format(date) }.getOrElse { "—" }
    }
    Text(
        text  = label,
        style = MaterialTheme.typography.labelMedium,
        color = PxColors.OnSurfaceDim
    )
}

@Composable
fun SessionHistoryItem(session: PomodoroSession) {
    val barColor = when (session.type) {
        PomodoroType.FOCUS       -> PxColors.Primary
        PomodoroType.SHORT_BREAK -> PxColors.Success
        PomodoroType.LONG_BREAK  -> PxColors.Info
    }

    val formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
    val startStr  = runCatching { formatter.format(session.startedAt) }.getOrElse { "—" }
    val endStr    = session.endedAt?.let { runCatching { formatter.format(it) }.getOrElse { "—" } } ?: stringResource(R.string.pomodoro_ongoing)
    val duration  = session.actualMinutes ?: session.plannedMinutes

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PxColors.Surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(64.dp)
                .background(barColor, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = sessionTypeLabel(session.type),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = PxColors.OnBackground
                )
                if (session.interrupted) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector        = Icons.Outlined.Warning,
                        contentDescription = stringResource(R.string.pomodoro_session_interrupted),
                        tint               = PxColors.Warning,
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }

            session.taskTitle?.let { title ->
                Text(
                    text     = title,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = PxColors.OnSurfaceDim,
                    maxLines = 1
                )
            }

            Text(
                text  = "$startStr — $endStr",
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurfaceDim
            )
        }

        Box(
            modifier = Modifier
                .padding(end = 12.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(barColor.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text       = stringResource(R.string.pomodoro_duration_min, duration),
                style      = MaterialTheme.typography.labelSmall,
                color      = barColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Scoped to this file only — avoids conflict with the public typeLabel in PomodoroScreen
@Composable
private fun sessionTypeLabel(type: PomodoroType) = when (type) {
    PomodoroType.FOCUS       -> stringResource(R.string.pomodoro_focus)
    PomodoroType.SHORT_BREAK -> stringResource(R.string.pomodoro_short_break)
    PomodoroType.LONG_BREAK  -> stringResource(R.string.pomodoro_long_break)
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun SessionHistoryPreview() {
    ProductivityXTheme {
        SessionHistoryContent(
            sessions       = emptyList(),
            stats          = PomodoroStats(3, 75, 4500),
            isLoading      = false,
            onNavigateBack = {},
            onLoadMore     = {}
        )
    }
}