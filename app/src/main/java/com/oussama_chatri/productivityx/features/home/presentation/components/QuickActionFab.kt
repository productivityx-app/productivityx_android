package com.oussama_chatri.productivityx.features.home.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.home.presentation.state.QuickAction
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QuickActionFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onActionSelected: (QuickAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(200),
        label = "fab_rotation",
    )
    val density = LocalDensity.current

    Box(modifier = modifier) {
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .pointerInput(Unit) {
                        detectTapGestures { onToggle() }
                    },
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            if (isExpanded) {
                RadialMenuItems(onActionSelected = onActionSelected, density = density)
            }

            FloatingActionButton(
                onClick = onToggle,
                containerColor = PxColors.Primary,
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.home_quick_actions_cd),
                    tint = Color.White,
                    modifier = Modifier.rotate(rotation),
                )
            }
        }
    }
}

@Composable
private fun RadialMenuItems(
    onActionSelected: (QuickAction) -> Unit,
    density: androidx.compose.ui.unit.Density,
) {
    val items = listOf(
        RadialMenuItem(stringResource(R.string.home_quick_action_new_note), Icons.Filled.Edit, QuickAction.NEW_NOTE, PxColors.Info),
        RadialMenuItem(stringResource(R.string.home_quick_action_new_task), Icons.Filled.TaskAlt, QuickAction.NEW_TASK, PxColors.Success),
        RadialMenuItem(stringResource(R.string.home_quick_action_calculator), Icons.Filled.Calculate, QuickAction.CALCULATOR, PxColors.Primary),
        RadialMenuItem(stringResource(R.string.home_quick_action_start_timer), Icons.Filled.PlayArrow, QuickAction.START_TIMER, PxColors.Warning),
        RadialMenuItem(stringResource(R.string.home_quick_action_ai_chat), Icons.Filled.AutoAwesome, QuickAction.AI_CHAT, PxColors.Primary),
    )

    val radiusPx = with(density) { 120.dp.toPx() }
    val startAngle = -90f
    val angleStep = 360f / items.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, end = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        items.forEachIndexed { index, item ->
            val angle = Math.toRadians((startAngle + angleStep * index).toDouble())
            val xOffset = (radiusPx * cos(angle).toFloat()).roundToInt()
            val yOffset = (radiusPx * sin(angle).toFloat()).roundToInt()

            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = tween(300 + index * 50),
                    initialOffsetY = { it * 2 },
                ) + fadeIn(animationSpec = tween(300 + index * 50)),
                exit = slideOutVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                xOffset - with(density) { 40.dp.toPx().roundToInt() },
                                yOffset - with(density) { 40.dp.toPx().roundToInt() },
                            )
                        }
                        .size(80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(item.color.copy(alpha = 0.15f))
                            .clickable { onActionSelected(item.action) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = item.color,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

private data class RadialMenuItem(
    val label: String,
    val icon: ImageVector,
    val action: QuickAction,
    val color: Color,
)
