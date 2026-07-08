package com.oussama_chatri.productivityx.features.home.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import java.time.LocalTime

@Composable
fun GreetingSection(
    firstName: String,
    weatherTemp: String?,
    weatherCondition: String?,
    modifier: Modifier = Modifier,
    onAvatarClick: () -> Unit = {},
) {
    val greeting = timeAwareGreeting(firstName)
    val dateText = java.time.format.DateTimeFormatter
        .ofPattern("EEEE, MMMM d")
        .format(java.time.LocalDate.now())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PxColors.Surface)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn(),
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = PxColors.OnBackground,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurfaceDim,
                )
                if (weatherTemp != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = PxColors.Warning,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$weatherTemp${weatherCondition?.let { " - $it" } ?: ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PxColors.OnSurfaceDim,
                        )
                    }
                }
            }

            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PxColors.Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = firstName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = PxColors.Primary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(PxColors.Success)
                        .align(Alignment.BottomEnd),
                )
            }
        }
    }
}

private fun timeAwareGreeting(firstName: String): String {
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }
    return "$greeting, ${firstName.ifBlank { "there" }}"
}
