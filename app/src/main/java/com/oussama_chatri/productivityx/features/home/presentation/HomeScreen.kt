package com.oussama_chatri.productivityx.features.home.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.ui.components.PxLoadingState
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.util.UiState
import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import com.oussama_chatri.productivityx.features.home.domain.model.WidgetType
import com.oussama_chatri.productivityx.features.home.presentation.components.AiQuickActionWidget
import com.oussama_chatri.productivityx.features.home.presentation.components.DailyQuoteCard
import com.oussama_chatri.productivityx.features.home.presentation.components.EventsWidget
import com.oussama_chatri.productivityx.features.home.presentation.components.FocusModeToggle
import com.oussama_chatri.productivityx.features.home.presentation.components.FocusTimeWidget
import com.oussama_chatri.productivityx.features.home.presentation.components.GreetingSection
import com.oussama_chatri.productivityx.features.home.presentation.components.QuickActionFab
import com.oussama_chatri.productivityx.features.home.presentation.components.RecentNotesWidget
import com.oussama_chatri.productivityx.features.home.presentation.components.TasksWidget
import com.oussama_chatri.productivityx.features.home.presentation.event.HomeEvent
import com.oussama_chatri.productivityx.features.home.presentation.state.HomeUiState
import com.oussama_chatri.productivityx.features.home.presentation.state.QuickAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToPomodoro: () -> Unit,
    onNavigateToAi: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.dashboardState,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            modifier = Modifier.fillMaxSize(),
            label = "home_content",
        ) { state ->
            when (state) {
                is UiState.Loading -> PxLoadingState()
                is UiState.Error -> HomeErrorState(
                    message = state.message,
                    onRefresh = { viewModel.onEvent(HomeEvent.Refresh) },
                )
                is UiState.Success -> HomeContent(
                    uiState = uiState,
                    summary = state.data,
                    onEvent = viewModel::onEvent,
                    onAvatarClick = onNavigateToProfile,
                    onOpenTasks = onNavigateToTasks,
                    onOpenCalendar = onNavigateToCalendar,
                    onOpenNotes = onNavigateToNotes,
                    onOpenPomodoro = onNavigateToPomodoro,
                    onOpenAi = onNavigateToAi,
                )
            }
        }

        QuickActionFab(
            isExpanded = uiState.showRadialMenu,
            onToggle = { viewModel.onEvent(HomeEvent.ToggleRadialMenu) },
            onActionSelected = { action ->
                viewModel.onEvent(HomeEvent.QuickActionSelected(action))
                when (action) {
                    QuickAction.NEW_NOTE -> onNavigateToNotes()
                    QuickAction.NEW_TASK -> onNavigateToTasks()
                    QuickAction.START_TIMER -> onNavigateToPomodoro()
                    QuickAction.AI_CHAT -> onNavigateToAi()
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }

    QuickActionSnackbar(
        selectedAction = uiState.selectedQuickAction,
        onDismiss = { viewModel.onEvent(HomeEvent.DismissQuickAction) },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    summary: DashboardSummary,
    onEvent: (HomeEvent) -> Unit,
    onAvatarClick: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenPomodoro: () -> Unit,
    onOpenAi: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { onEvent(HomeEvent.Refresh) },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val widgets = uiState.widgetOrder
                .filter { summary.widgetVisibility[it] != false }

            itemsIndexed(widgets, key = { _, w -> w.name }) { _, widgetType ->
                val isExpanded = uiState.expandedWidget == widgetType
                val span = widgetType.defaultSpan

                WidgetContent(
                    widgetType = widgetType,
                    summary = summary,
                    isExpanded = isExpanded,
                    onToggleExpand = { onEvent(HomeEvent.ToggleWidgetExpanded(widgetType)) },
                    onSeeAll = when (widgetType) {
                        WidgetType.TODAYS_TASKS -> onOpenTasks
                        WidgetType.UPCOMING_EVENTS -> onOpenCalendar
                        WidgetType.RECENT_NOTES -> onOpenNotes
                        WidgetType.FOCUS_TIME -> onOpenPomodoro
                        WidgetType.FOCUS_MODE_TOGGLE -> null
                        WidgetType.DAILY_QUOTE -> null
                        WidgetType.AI_QUICK_ACTION -> null
                        WidgetType.GREETING -> null
                    },
                    onAvatarClick = onAvatarClick,
                    onStartFocus = onOpenPomodoro,
                    onOpenAi = onOpenAi,
                    onToggleFocusMode = { onEvent(HomeEvent.ToggleFocusMode) },
                    modifier = Modifier.animateItem(),
                )
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun WidgetContent(
    widgetType: WidgetType,
    summary: DashboardSummary,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSeeAll: (() -> Unit)?,
    onAvatarClick: () -> Unit,
    onStartFocus: () -> Unit,
    onOpenAi: () -> Unit,
    onToggleFocusMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (widgetType) {
        WidgetType.GREETING -> GreetingSection(
            firstName = summary.firstName,
            weatherTemp = summary.weatherTemp,
            weatherCondition = summary.weatherCondition,
            onAvatarClick = onAvatarClick,
            modifier = modifier,
        )
        WidgetType.TODAYS_TASKS -> TasksWidget(
            tasks = summary.dueTodayTasks,
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand,
            onSeeAll = onSeeAll ?: {},
            modifier = modifier,
        )
        WidgetType.UPCOMING_EVENTS -> EventsWidget(
            events = summary.upcomingEvents,
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand,
            onSeeAll = onSeeAll ?: {},
            modifier = modifier,
        )
        WidgetType.FOCUS_TIME -> FocusTimeWidget(
            focusMinutes = summary.todayFocusMinutes,
            totalMinutes = summary.totalEstimatedFocusMinutes,
            completedSessions = summary.completedSessionsToday,
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand,
            onStartFocus = onStartFocus,
            modifier = modifier,
        )
        WidgetType.RECENT_NOTES -> RecentNotesWidget(
            notes = summary.recentNotes,
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand,
            onSeeAll = onSeeAll ?: {},
            modifier = modifier,
        )
        WidgetType.DAILY_QUOTE -> DailyQuoteCard(
            quote = summary.dailyQuote,
            author = summary.dailyQuoteAuthor,
            modifier = modifier,
        )
        WidgetType.AI_QUICK_ACTION -> AiQuickActionWidget(
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand,
            onOpenAi = onOpenAi,
            modifier = modifier,
        )
        WidgetType.FOCUS_MODE_TOGGLE -> FocusModeToggle(
            isFocusMode = summary.isFocusMode,
            onToggle = onToggleFocusMode,
            modifier = modifier,
        )
    }
}

@Composable
private fun HomeErrorState(
    message: String,
    onRefresh: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(bottom = 16.dp),
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun QuickActionSnackbar(
    selectedAction: QuickAction?,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = selectedAction != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
    ) {
        val message = when (selectedAction) {
            QuickAction.NEW_NOTE -> "Opening new note..."
            QuickAction.NEW_TASK -> "Opening new task..."
            QuickAction.START_TIMER -> "Starting timer..."
            QuickAction.AI_CHAT -> "Opening AI chat..."
            null -> ""
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Text(
                text = message,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PxColors.SurfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
