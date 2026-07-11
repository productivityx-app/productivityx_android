package com.oussama_chatri.productivityx.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.stringResource
import com.oussama_chatri.productivityx.R
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.enums.Priority
import com.oussama_chatri.productivityx.core.enums.SyncStatus
import com.oussama_chatri.productivityx.core.enums.TaskStatus
import com.oussama_chatri.productivityx.core.ui.theme.PriorityColors
import com.oussama_chatri.productivityx.core.ui.theme.ProductivityXTheme
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.PxIcons
import com.oussama_chatri.productivityx.core.ui.theme.PxIconSizes
import com.oussama_chatri.productivityx.core.ui.theme.Spacing

enum class PxChipVariant { Filter, Choice, Action, Input }

data class PxChipItem(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val color: Color? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PxChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PxChipVariant = PxChipVariant.Filter,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    selectedColor: Color = PxColors.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onDismiss: (() -> Unit)? = null,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "chipScale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color.Transparent,
        animationSpec = tween(150),
        label = "chipColor",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else PxColors.Outline,
        animationSpec = tween(150),
        label = "chipBorder",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (selectedColor.luminance() > 0.5f) Color.Black else Color.White
        } else PxColors.OnSurfaceDim,
        animationSpec = tween(150),
        label = "chipContent",
    )

    when (variant) {
        PxChipVariant.Input -> {
            InputChip(
                selected = isSelected,
                onClick = onClick,
                label = { Text(text, style = MaterialTheme.typography.labelMedium) },
                modifier = modifier.scale(scale),
                leadingIcon = leadingIcon?.let {
                    { Icon(it, contentDescription = null, modifier = Modifier.size(16.dp)) }
                },
                trailingIcon = if (onDismiss != null) {
                    {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_clear_input),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onDismiss() },
                        )
                    }
                } else trailingIcon?.let {
                    { Icon(it, contentDescription = null, modifier = Modifier.size(16.dp)) }
                },
                shape = MaterialTheme.shapes.large,
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = containerColor,
                    selectedLabelColor = contentColor,
                    labelColor = contentColor,
                    containerColor = containerColor,
                ),
                border = InputChipDefaults.inputChipBorder(
                    enabled = enabled,
                    selected = isSelected,
                    borderColor = borderColor,
                    selectedBorderColor = borderColor,
                ),
                enabled = enabled,
            )
        }
        else -> {
            FilterChip(
                selected = isSelected,
                onClick = onClick,
                label = { Text(text, style = MaterialTheme.typography.labelMedium) },
                modifier = modifier.scale(scale),
                leadingIcon = leadingIcon?.let {
                    { Icon(it, contentDescription = null, modifier = Modifier.size(16.dp)) }
                },
                trailingIcon = trailingIcon?.let {
                    { Icon(it, contentDescription = null, modifier = Modifier.size(16.dp)) }
                },
                shape = MaterialTheme.shapes.large,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = containerColor,
                    selectedLabelColor = contentColor,
                    labelColor = contentColor,
                    containerColor = containerColor,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,
                    selected = isSelected,
                    borderColor = borderColor,
                    selectedBorderColor = borderColor,
                ),
                enabled = enabled,
            )
        }
    }
}

@Composable
fun FilterBar(
    items: List<PxChipItem>,
    selectedIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    multiSelect: Boolean = false,
    showClearAll: Boolean = true,
) {
    Column(modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.lg),
        ) {
            items(items) { item ->
                PxChip(
                    text = item.label,
                    isSelected = item.id in selectedIds,
                    onClick = {
                        onSelectionChange(
                            if (multiSelect) {
                                if (item.id in selectedIds) selectedIds - item.id
                                else selectedIds + item.id
                            } else {
                                if (item.id in selectedIds) emptySet() else setOf(item.id)
                            }
                        )
                    },
                    leadingIcon = item.icon,
                    selectedColor = item.color ?: PxColors.Primary,
                )
            }
        }
        if (showClearAll && selectedIds.isNotEmpty()) {
            TextButton(
                onClick = { onSelectionChange(emptySet()) },
                modifier = Modifier.padding(start = Spacing.lg),
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("Clear all", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagInputField(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier,
    suggestions: List<String> = emptyList(),
) {
    var inputText by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            tags.forEach { tag ->
                PxChip(
                    text = tag,
                    isSelected = true,
                    onClick = {},
                    variant = PxChipVariant.Input,
                    onDismiss = { onRemoveTag(tag) },
                    selectedColor = PxColors.Primary.copy(alpha = 0.15f),
                )
            }
            PxTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.width(120.dp),
                placeholder = "Add tag",
                trailingIcon = Icons.Filled.Add,
                onTrailingIconClick = {
                    if (inputText.isNotBlank()) {
                        onAddTag(inputText.trim())
                        inputText = ""
                    }
                },
                supportingText = if (suggestions.isNotEmpty() && inputText.isNotEmpty()) {
                    suggestions.filter { it.contains(inputText, ignoreCase = true) }
                        .take(3).joinToString(", ")
                } else null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FacetedFilter(
    title: String,
    options: List<PxChipItem>,
    selectedIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = PxColors.OnBackground,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(Spacing.sm))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { option ->
                PxChip(
                    text = option.label,
                    isSelected = option.id in selectedIds,
                    onClick = {
                        onSelectionChange(
                            if (option.id in selectedIds) selectedIds - option.id
                            else selectedIds + option.id
                        )
                    },
                    variant = PxChipVariant.Filter,
                    selectedColor = option.color ?: PxColors.Primary,
                )
            }
        }
    }
}

// ==================== LEGACY CHIPS (unchanged) ====================

@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
) {
    val dotColor = when (syncStatus) {
        SyncStatus.PENDING  -> PxColors.Warning
        SyncStatus.SYNCING  -> PxColors.Info
        SyncStatus.SYNCED   -> PxColors.Success
        SyncStatus.CONFLICT -> PxColors.Error
    }
    val label = when (syncStatus) {
        SyncStatus.PENDING  -> "Pending"
        SyncStatus.SYNCING  -> "Syncing"
        SyncStatus.SYNCED   -> "Synced"
        SyncStatus.CONFLICT -> "Conflict"
    }
    val infiniteTransition = rememberInfiniteTransition(label = "syncAnim")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (syncStatus == SyncStatus.SYNCING || syncStatus == SyncStatus.CONFLICT) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "syncDotAlpha",
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .alpha(if (syncStatus == SyncStatus.SYNCED) 1f else alpha)
                .background(dotColor),
        )
        if (showLabel) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PxColors.OnSurfaceDim,
            )
        }
    }
}

@Composable
fun PriorityChip(priority: Priority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        Priority.LOW    -> PriorityColors.Low
        Priority.MEDIUM -> PriorityColors.Medium
        Priority.HIGH   -> PriorityColors.High
        Priority.URGENT -> PriorityColors.Urgent
    }
    val label = priority.name.lowercase().replaceFirstChar { it.uppercaseChar() }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
fun StatusChip(status: TaskStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        TaskStatus.TODO        -> PxColors.OnSurfaceDim to stringResource(R.string.status_todo)
        TaskStatus.IN_PROGRESS -> PxColors.Info         to stringResource(R.string.status_in_progress)
        TaskStatus.ON_HOLD     -> PxColors.Warning      to stringResource(R.string.status_on_hold)
        TaskStatus.DONE        -> PxColors.Success      to stringResource(R.string.status_done)
        TaskStatus.CANCELLED   -> PxColors.Error        to stringResource(R.string.status_cancelled)
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F14)
@Composable
private fun ChipsPreview() {
    ProductivityXTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriorityChip(Priority.LOW)
                PriorityChip(Priority.MEDIUM)
                PriorityChip(Priority.HIGH)
                PriorityChip(Priority.URGENT)
            }
            FilterBar(
                items = listOf(
                    PxChipItem("1", "All"),
                    PxChipItem("2", "Notes"),
                    PxChipItem("3", "Tasks"),
                ),
                selectedIds = setOf("1"),
                onSelectionChange = {},
            )
            TagInputField(tags = listOf("work", "personal"), onAddTag = {}, onRemoveTag = {})
        }
    }
}
