package com.oussama_chatri.productivityx.features.tasks.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.enums.Badge
import com.oussama_chatri.productivityx.core.enums.BadgeType
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.tasks.presentation.state.TaskStatsUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskStatsScreen(
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit = {},
    uiState: TaskStatsUiState = TaskStatsUiState()
) {
    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.tasks_statistics),
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
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PxColors.Primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Productivity Score + Streak
                    ProductivityHeader(
                        productivityScore = uiState.productivityScore,
                        productivityTrend = uiState.productivityTrend,
                        currentStreak = uiState.currentStreak,
                        longestStreak = uiState.longestStreak
                    )

                    // Completion Rate Charts
                    CompletionRateSection(
                        weeklyRate = uiState.weeklyCompletionRate,
                        monthlyRate = uiState.monthlyCompletionRate,
                        weeklyCompleted = uiState.weeklyCompletedCount,
                        weeklyTotal = uiState.weeklyTotalCount
                    )

                    // Time per Category
                    TimePerCategoryChart(timePerPriority = uiState.timePerPriority)

                    // Badges / Achievements
                    AchievementsSection(badges = uiState.badges)

                    // Weekly Review
                    WeeklyReviewCard(
                        createdCount = uiState.weekCreatedCount,
                        completedCount = uiState.weekCompletedCount,
                        overdueCount = uiState.weekOverdueCount,
                        suggestedFocus = uiState.weekSuggestedFocus
                    )

                    // Productivity Patterns
                    ProductivityPatternsSection(
                        hourlyHeatmap = uiState.hourlyHeatmap,
                        categoryBreakdown = uiState.categoryBreakdown,
                        completionVelocity = uiState.completionVelocity
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ─── Productivity Header ──────────────────────────────────────────────────────

@Composable
private fun ProductivityHeader(
    productivityScore: Float,
    productivityTrend: Float,
    currentStreak: Int,
    longestStreak: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Productivity Score
        StatsCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.AutoGraph,
            title = stringResource(R.string.tasks_stats_score),
            value = "${(productivityScore * 100).toInt()}",
            subValue = "${if (productivityTrend >= 0) "+" else ""}${(productivityTrend * 100).toInt()}%",
            subValueColor = if (productivityTrend >= 0) PxColors.Success else PxColors.Error
        )

        // Current Streak
        StatsCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.LocalFireDepartment,
            title = stringResource(R.string.tasks_stats_streak),
            value = "$currentStreak",
            subValue = stringResource(R.string.tasks_stats_days),
            valueColor = Color(0xFFF97316)
        )

        // Longest Streak
        StatsCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.EmojiEvents,
            title = stringResource(R.string.tasks_stats_best),
            value = "$longestStreak",
            subValue = stringResource(R.string.tasks_stats_days),
            valueColor = Color(0xFFF59E0B)
        )
    }
}

// ─── Completion Rate Section ──────────────────────────────────────────────────

@Composable
private fun CompletionRateSection(
    weeklyRate: Float,
    monthlyRate: Float,
    weeklyCompleted: Int,
    weeklyTotal: Int
) {
    SectionCard(title = stringResource(R.string.tasks_stats_completion_rate)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Weekly
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { weeklyRate },
                        modifier = Modifier.size(80.dp),
                        color = PxColors.Primary,
                        trackColor = PxColors.SurfaceVariant,
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "${(weeklyRate * 100).toInt()}%",
                        color = PxColors.OnBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(stringResource(R.string.tasks_stats_this_week), color = PxColors.OnSurfaceDim, fontSize = 12.sp)
                Text(
                    text = stringResource(R.string.tasks_stats_done_count, weeklyCompleted, weeklyTotal),
                    color = PxColors.OnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Monthly
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { monthlyRate },
                        modifier = Modifier.size(80.dp),
                        color = PxColors.Secondary,
                        trackColor = PxColors.SurfaceVariant,
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "${(monthlyRate * 100).toInt()}%",
                        color = PxColors.OnBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(stringResource(R.string.tasks_stats_this_month), color = PxColors.OnSurfaceDim, fontSize = 12.sp)
                Text(
                    text = stringResource(R.string.tasks_stats_monthly_progress),
                    color = PxColors.OnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── Time Per Category Chart ──────────────────────────────────────────────────

@Composable
private fun TimePerCategoryChart(
    timePerPriority: Map<Priority, Int>
) {
    SectionCard(title = stringResource(R.string.tasks_stats_time_by_priority)) {
        if (timePerPriority.isEmpty()) {
            Text(
                stringResource(R.string.tasks_stats_no_data),
                color = PxColors.OnSurfaceDim,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            val totalTime = timePerPriority.values.sum().coerceAtLeast(1)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timePerPriority.forEach { (priority, minutes) ->
                    val fraction = minutes.toFloat() / totalTime
                    val color = when (priority) {
                        Priority.LOW -> Color(0xFF6B7280)
                        Priority.MEDIUM -> Color(0xFF3B82F6)
                        Priority.HIGH -> Color(0xFFF59E0B)
                        Priority.URGENT -> Color(0xFFEF4444)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = PxColors.OnSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(60.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(PxColors.SurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                        Text(
                            text = stringResource(R.string.tasks_minutes_abbrev, minutes),
                            color = PxColors.OnSurfaceDim,
                            fontSize = 12.sp,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

// ─── Achievements / Badges Section ────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AchievementsSection(
    badges: List<Badge>
) {
    SectionCard(title = stringResource(R.string.tasks_stats_achievements)) {
        if (badges.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Outlined.Stars,
                    null,
                    tint = PxColors.OnSurfaceDim,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    stringResource(R.string.tasks_stats_unlock_badges),
                    color = PxColors.OnSurfaceDim,
                    fontSize = 13.sp
                )
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                badges.forEach { badge ->
                    BadgeItem(badge = badge)
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(
    badge: Badge
) {
    val isUnlocked = badge.unlockedAt != null
    val alpha = if (isUnlocked) 1f else 0.4f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.SurfaceVariant.copy(alpha = alpha))
            .padding(8.dp)
    ) {
        Icon(
            Icons.Outlined.EmojiEvents,
            null,
            tint = if (isUnlocked) Color(0xFFF59E0B) else PxColors.OnSurfaceDim,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = badge.title,
            color = if (isUnlocked) PxColors.OnBackground else PxColors.OnSurfaceDim,
            fontSize = 10.sp,
            fontWeight = if (isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// ─── Weekly Review Card ───────────────────────────────────────────────────────

@Composable
private fun WeeklyReviewCard(
    createdCount: Int,
    completedCount: Int,
    overdueCount: Int,
    suggestedFocus: String
) {
    SectionCard(title = stringResource(R.string.tasks_stats_weekly_review)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStat(label = stringResource(R.string.tasks_stats_created), value = "$createdCount", icon = Icons.Outlined.BarChart)
                MiniStat(label = stringResource(R.string.tasks_stats_completed), value = "$completedCount", icon = Icons.Outlined.CheckCircle)
                MiniStat(label = stringResource(R.string.tasks_stats_overdue), value = "$overdueCount", icon = Icons.Outlined.AccessTime, valueColor = PxColors.Error)
            }

            if (suggestedFocus.isNotBlank()) {
                HorizontalDivider(color = PxColors.Outline)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.TrendingUp, null, tint = PxColors.Primary, modifier = Modifier.size(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.tasks_stats_suggested_focus),
                            color = PxColors.OnSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            suggestedFocus,
                            color = PxColors.OnSurfaceDim,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Productivity Patterns Section ────────────────────────────────────────────

@Composable
private fun ProductivityPatternsSection(
    hourlyHeatmap: Map<Int, Map<Int, Int>>,
    categoryBreakdown: Map<Priority, Float>,
    completionVelocity: List<Pair<LocalDate, Int>>
) {
    SectionCard(title = stringResource(R.string.tasks_stats_patterns)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Completion velocity trend (simple chart)
            if (completionVelocity.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.tasks_stats_velocity),
                        color = PxColors.OnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    SimpleLineChart(
                        data = completionVelocity.map { it.second },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }
            }

            // Hourly heatmap summary
            if (hourlyHeatmap.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.tasks_stats_productive_hours),
                        color = PxColors.OnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val days = listOf(
                        stringResource(R.string.day_monday_short),
                        stringResource(R.string.day_tuesday_short),
                        stringResource(R.string.day_wednesday_short),
                        stringResource(R.string.day_thursday_short),
                        stringResource(R.string.day_friday_short),
                        stringResource(R.string.day_saturday_short),
                        stringResource(R.string.day_sunday_short)
                    )
                    val maxCount = hourlyHeatmap.values.flatMap { it.values }.maxOrNull() ?: 1

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (dayIdx in 0..6) {
                            val hourMap = hourlyHeatmap[dayIdx] ?: emptyMap()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    days.getOrElse(dayIdx) { "?" },
                                    color = PxColors.OnSurfaceDim,
                                    fontSize = 9.sp,
                                    modifier = Modifier.width(24.dp)
                                )
                                for (hour in 6..22 step 2) {
                                    val count = hourMap[hour] ?: 0
                                    val intensity = if (maxCount > 0) count.toFloat() / maxCount else 0f
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                when {
                                                    intensity > 0.7f -> PxColors.Primary
                                                    intensity > 0.4f -> PxColors.Primary.copy(alpha = 0.6f)
                                                    intensity > 0.1f -> PxColors.Primary.copy(alpha = 0.3f)
                                                    else -> PxColors.SurfaceVariant
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category breakdown pie chart
            if (categoryBreakdown.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.tasks_stats_category_breakdown),
                        color = PxColors.OnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    SimplePieChart(
                        data = categoryBreakdown,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }
            }

            if (hourlyHeatmap.isEmpty() && categoryBreakdown.isEmpty() && completionVelocity.isEmpty()) {
                Text(
                    stringResource(R.string.tasks_stats_complete_more),
                    color = PxColors.OnSurfaceDim,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

// ─── Simple Line Chart ────────────────────────────────────────────────────────

@Composable
private fun SimpleLineChart(
    data: List<Int>,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return

    Canvas(modifier = modifier) {
        val maxVal = data.max().toFloat().coerceAtLeast(1f)
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - (value / maxVal) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = PxColors.Primary,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Fill under the curve
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo((data.size - 1) * stepX, size.height)
        fillPath.lineTo(0f, size.height)
        fillPath.close()
        drawPath(
            path = fillPath,
            color = PxColors.Primary.copy(alpha = 0.1f)
        )
    }
}

// ─── Simple Pie Chart ─────────────────────────────────────────────────────────

@Composable
private fun SimplePieChart(
    data: Map<Priority, Float>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum().coerceAtLeast(1f)
    val colors = listOf(
        Color(0xFF6B7280),
        Color(0xFF3B82F6),
        Color(0xFFF59E0B),
        Color(0xFFEF4444)
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(64.dp)) {
            var startAngle = -90f
            data.entries.forEachIndexed { index, (_, value) ->
                val sweepAngle = (value / total) * 360f
                drawArc(
                    color = colors.getOrElse(index) { PxColors.Primary },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            data.entries.forEachIndexed { index, (priority, value) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.getOrElse(index) { PxColors.Primary })
                    )
                    Text(
                        text = "${priority.name.lowercase().replaceFirstChar { it.uppercase() }}: ${(value * 100).toInt()}%",
                        color = PxColors.OnSurface,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ─── Helper composables ───────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PxColors.Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = PxColors.OnBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun StatsCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subValue: String = "",
    subValueColor: Color = PxColors.OnSurfaceDim,
    valueColor: Color = PxColors.OnBackground
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PxColors.Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = PxColors.Primary, modifier = Modifier.size(20.dp))
            Text(
                text = title,
                color = PxColors.OnSurfaceDim,
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            if (subValue.isNotBlank()) {
                Text(
                    text = subValue,
                    color = subValueColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = PxColors.OnBackground
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = PxColors.OnSurfaceDim, modifier = Modifier.size(16.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = PxColors.OnSurfaceDim,
            fontSize = 11.sp
        )
    }
}
