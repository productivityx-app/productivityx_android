package com.oussama_chatri.productivityx.features.notes.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteLink
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun NoteLinkingGraph(
    centerTitle: String,
    links: List<NoteLink>,
    onNodeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PxColors.Surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Note Links",
                style = MaterialTheme.typography.labelMedium,
                color = PxColors.OnSurfaceDim
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (links.isEmpty()) {
                Text(
                    text = "No linked notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = PxColors.OnSurfaceDim,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val radius = size.minDimension / 2 - 40f

                        val nodeCount = links.size.coerceAtMost(8)

                        for (i in 0 until nodeCount) {
                            val angle = 2 * PI * i / nodeCount - PI / 2
                            val x = centerX + radius * cos(angle).toFloat()
                            val y = centerY + radius * sin(angle).toFloat()

                            drawLine(
                                color = PxColors.Primary.copy(alpha = 0.3f),
                                start = Offset(centerX, centerY),
                                end = Offset(x, y),
                                strokeWidth = 1.5f
                            )

                            drawCircle(
                                color = PxColors.Primary.copy(alpha = 0.15f),
                                radius = 20f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = PxColors.Primary,
                                radius = 16f,
                                center = Offset(x, y),
                                style = Stroke(width = 2f)
                            )
                            drawCircle(
                                color = PxColors.Primary.copy(alpha = 0.3f),
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }

                        drawCircle(
                            color = PxColors.Primary,
                            radius = 24f,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 2.5f)
                        )
                        drawCircle(
                            color = PxColors.Primary.copy(alpha = 0.3f),
                            radius = 6f,
                            center = Offset(centerX, centerY)
                        )
                    }

                    // Center label
                    Text(
                        text = centerTitle.take(2).ifBlank { "N" },
                        style = MaterialTheme.typography.labelSmall,
                        color = PxColors.Primary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clickable { },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
