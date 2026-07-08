package com.oussama_chatri.productivityx.features.settings.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
    ) {
        content()
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
        color = PxColors.OnSurface.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconTint: Color = PxColors.OnSurface.copy(alpha = 0.6f),
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
    trailing: @Composable (() -> Unit)? = null
) {
    val baseModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Column {
        Row(
            modifier = baseModifier
                .defaultMinSize(minHeight = 56.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = PxColors.OnSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailing()
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 52.dp),
                color = PxColors.OnSurface.copy(alpha = 0.06f)
            )
        }
    }
}

@Composable
fun SettingRowSwitch(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showDivider: Boolean = true
) {
    SettingRow(
        icon = icon,
        label = label,
        subtitle = subtitle,
        modifier = modifier,
        showDivider = showDivider,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PxColors.Primary,
                    uncheckedThumbColor = PxColors.OnSurface.copy(alpha = 0.4f),
                    uncheckedTrackColor = PxColors.OnSurface.copy(alpha = 0.12f)
                )
            )
        }
    )
}

@Composable
fun MinuteStepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    suffix: String = "min",
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(32.dp)
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleLarge,
                color = PxColors.Primary
            )
        }
        Text(
            text = "$value $suffix",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PxColors.OnSurface,
            modifier = Modifier.width(52.dp),
        )
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(32.dp)
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge,
                color = PxColors.Primary
            )
        }
    }
}

@Composable
fun <T> SelectionChipRow(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        options.forEach { (value, label) ->
            SelectionChip(
                label = label,
                selected = value == selected,
                onClick = { onSelect(value) }
            )
        }
    }
}

@Composable
fun SelectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) PxColors.Primary
        else PxColors.SurfaceVariant,
        animationSpec = spring(),
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White
        else PxColors.OnSurface.copy(alpha = 0.7f),
        animationSpec = spring(),
        label = "chip_text"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
fun AvatarInitials(
    initials: String,
    modifier: Modifier = Modifier,
    size: Int = 80
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(PxColors.Primary)
    ) {
        Text(
            text = initials.take(2).uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (size / 3).sp
            ),
            color = Color.White
        )
    }
}

@Composable
fun ThemeSelector(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    data class ThemeOption(val key: String, val label: String, val primary: Color, val surface: Color)

    val themes = listOf(
        ThemeOption("DARK", "Dark", Color(0xFF6366F1), Color(0xFF1A1A24)),
        ThemeOption("LIGHT", "Light", Color(0xFF4F46E5), Color(0xFFFFFFFF)),
        ThemeOption("SYSTEM", "System", Color(0xFF6366F1), Color(0xFF1A1A24)),
        ThemeOption("OCEAN", "Ocean", Color(0xFF06B6D4), Color(0xFF0C1A26)),
        ThemeOption("AMBER", "Amber", Color(0xFFF59E0B), Color(0xFF1A1400)),
        ThemeOption("FOREST", "Forest", Color(0xFF22C55E), Color(0xFF0D1A10)),
        ThemeOption("ROSE", "Rose", Color(0xFFF43F5E), Color(0xFF1C0A10)),
        ThemeOption("MIDNIGHT", "Midnight", Color(0xFF6366F1), Color(0xFF10101F)),
        ThemeOption("DYNAMIC", "Dynamic", Color(0xFF6366F1), Color(0xFF1A1A24)),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        themes.forEach { theme ->
            val isSelected = theme.key == currentTheme
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) theme.primary else Color.Transparent,
                animationSpec = spring(),
                label = "themeBorder",
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onThemeSelected(theme.key) }
                    .border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(theme.primary),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = theme.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) PxColors.Primary
                    else PxColors.OnSurface.copy(alpha = 0.7f),
                )
                if (theme.key == "DYNAMIC") {
                    Text(
                        text = "Android 12+",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = PxColors.OnSurface.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: Int, modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFFEF4444),
        Color(0xFFF59E0B),
        Color(0xFF3B82F6),
        Color(0xFF22C55E)
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        repeat(4) { index ->
            val filled = index < strength
            val color by animateColorAsState(
                targetValue = if (filled) colors[strength - 1]
                else PxColors.OnSurface.copy(alpha = 0.15f),
                animationSpec = spring(),
                label = "strength_$index"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
    val label = when (strength) {
        1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Strong"
        else -> ""
    }
    if (label.isNotEmpty()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.getOrElse(strength - 1) { Color.Transparent },
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
