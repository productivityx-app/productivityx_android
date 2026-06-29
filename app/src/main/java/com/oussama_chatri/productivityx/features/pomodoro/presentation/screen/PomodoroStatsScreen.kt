package com.oussama_chatri.productivityx.features.pomodoro.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.pomodoro.domain.model.PomodoroStats
import com.oussama_chatri.productivityx.features.pomodoro.presentation.viewmodel.PomodoroStatsViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PomodoroStatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PxColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Focus Analytics", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PxColors.Background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            state.stats?.let { stats ->
                StreakCard(stats)
                WeeklyHeatMap(stats.weeklyHeatMap)
                CategoryDistribution(stats.categoryDistribution)
                SummaryStats(stats)
            }
        }
    }
}

@Composable
private fun StreakCard(stats: PomodoroStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PxColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(PxColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${stats.currentStreak} Day Streak",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PxColors.OnSurface
                )
                Text(
                    text = "Best: ${stats.longestStreak} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = PxColors.OnSurfaceDim
                )
            }
            Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = Color(0xFFFACC15))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeeklyHeatMap(heatMap: Map<LocalDate, Int>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Weekly Intensity",
                style = MaterialTheme.typography.titleMedium,
                color = PxColors.OnBackground
            )
            Icon(Icons.Outlined.TrendingUp, contentDescription = null, tint = PxColors.Primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PxColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = 7
                ) {
                    val now = LocalDate.now()
                    // Show last 21 days
                    for (i in 20 downTo 0) {
                        val date = now.minusDays(i.toLong())
                        val intensity = heatMap[date] ?: 0
                        HeatMapBox(intensity)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Less", style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
                    HeatMapBox(0)
                    HeatMapBox(25)
                    HeatMapBox(50)
                    HeatMapBox(100)
                    Text("More", style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
                }
            }
        }
    }
}

@Composable
private fun HeatMapBox(minutes: Int) {
    val color = when {
        minutes == 0 -> PxColors.SurfaceVariant
        minutes < 25 -> PxColors.Primary.copy(alpha = 0.3f)
        minutes < 60 -> PxColors.Primary.copy(alpha = 0.6f)
        else -> PxColors.Primary
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}

@Composable
private fun CategoryDistribution(dist: Map<String, Int>) {
    Column {
        Text(
            "Focus by Category",
            style = MaterialTheme.typography.titleMedium,
            color = PxColors.OnBackground
        )
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PxColors.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val total = dist.values.sum().toFloat()
                dist.forEach { (cat, mins) ->
                    val fraction = if (total > 0) mins / total else 0f
                    CategoryRow(cat, mins, fraction)
                }
                if (dist.isEmpty()) {
                    Text("No category data available", color = PxColors.OnSurfaceDim, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(name: String, minutes: Int, fraction: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = PxColors.OnSurface)
            Text("${minutes}m", style = MaterialTheme.typography.bodySmall, color = PxColors.OnSurfaceDim)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(PxColors.SurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(PxColors.Primary, PxColors.PrimaryVariant)
                        )
                    )
            )
        }
    }
}

@Composable
private fun SummaryStats(stats: PomodoroStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatMiniCard(
            modifier = Modifier.weight(1f),
            label = "Quality Score",
            value = "${(stats.focusQualityScore * 100).roundToInt()}%",
            icon = Icons.Outlined.TrendingUp,
            color = Color(0xFF22C55E)
        )
        StatMiniCard(
            modifier = Modifier.weight(1f),
            label = "Total Focus",
            value = "${stats.totalFocusTimeAllTime / 3600}h",
            icon = Icons.Outlined.PieChart,
            color = PxColors.Primary
        )
    }
}

@Composable
private fun StatMiniCard(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PxColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = PxColors.OnSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = PxColors.OnSurfaceDim)
        }
    }
}

fun Float.roundToInt(): Int = Math.round(this)
