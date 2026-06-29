package com.oussama_chatri.productivityx.features.events.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.features.events.presentation.state.WeatherData

@Composable
fun WeatherWidget(
    weather: WeatherData?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A24))
            .padding(12.dp)
    ) {
        if (weather != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = weatherIcon(weather.icon),
                        contentDescription = weather.condition,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.padding(start = 8.dp))
                    Text(
                        text = "${weather.temperature}°",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = Color(0xFFEEEEF5)
                    )
                }
                Text(
                    text = weather.condition,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888899)
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFF888899),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Weather",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888899)
                )
            }
        }
    }
}

private fun weatherIcon(icon: String): ImageVector {
    return when {
        icon.contains("sun") || icon.contains("clear") -> Icons.Outlined.WbSunny
        icon.contains("cloud") || icon.contains("overcast") -> Icons.Outlined.Cloud
        icon.contains("rain") || icon.contains("drizzle") -> Icons.Outlined.WaterDrop
        icon.contains("snow") || icon.contains("ice") -> Icons.Outlined.AcUnit
        else -> Icons.Outlined.WbSunny
    }
}
