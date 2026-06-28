package com.oussama_chatri.productivityx.features.ai.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

@Composable
fun WelcomeState(
    onSuggestionClick : (String) -> Unit,
    modifier          : Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerOffset",
    )

    Column(
        modifier              = modifier.padding(24.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center,
    ) {
        // Gradient icon
        Box(
            modifier         = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(PxColors.Primary, PxColors.Secondary),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(36.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text      = stringResource(R.string.ai_welcome_title),
            style     = MaterialTheme.typography.headlineMedium,
            color     = PxColors.OnBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = stringResource(R.string.ai_welcome_body),
            style     = MaterialTheme.typography.bodyMedium,
            color     = PxColors.OnSurfaceDim,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        // 2×2 suggestion grid
        val suggestions = listOf(
            stringResource(R.string.ai_suggestion_due_today),
            stringResource(R.string.ai_suggestion_overdue),
            stringResource(R.string.ai_suggestion_start_focus),
            stringResource(R.string.ai_suggestion_meeting_agenda),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.chunked(2).forEach { row ->
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { suggestion ->
                        SuggestionCard(
                            text      = suggestion,
                            onClick   = { onSuggestionClick(suggestion) },
                            modifier  = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    text     : String,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PxColors.Surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.bodySmall,
            color = PxColors.OnSurface,
        )
    }
}
