package com.oussama_chatri.productivityx.features.notes.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.PxRadius
import com.oussama_chatri.productivityx.core.ui.theme.Spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TableCreationDialog(
    onDismiss: () -> Unit,
    onConfirm: (rows: Int, cols: Int, hasHeader: Boolean) -> Unit
) {
    var selectedRows by remember { mutableStateOf(2) }
    var selectedCols by remember { mutableStateOf(2) }
    var hasHeader by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PxColors.Surface,
        title = {
            Text(
                text = stringResource(R.string.editor_insert_table_title),
                style = MaterialTheme.typography.titleMedium,
                color = PxColors.OnBackground
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.editor_table_dimensions, selectedRows, selectedCols),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PxColors.OnSurfaceDim
                )
                Spacer(modifier = Modifier.height(Spacing.md))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (row in 1..7) {
                        for (col in 1..7) {
                            val isSelected = row <= selectedRows && col <= selectedCols
                            val bgColor by animateColorAsState(
                                if (isSelected) PxColors.Primary.copy(alpha = 0.25f)
                                else PxColors.SurfaceVariant,
                                label = "cellBg"
                            )
                            val borderColor by animateColorAsState(
                                if (isSelected) PxColors.Primary
                                else PxColors.Outline.copy(alpha = 0.4f),
                                label = "cellBorder"
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(3.dp))
                                    .clickable {
                                        selectedRows = row
                                        selectedCols = col
                                    }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.editor_table_header_row),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PxColors.OnSurface
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Switch(
                        checked = hasHeader,
                        onCheckedChange = { hasHeader = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PxColors.Primary,
                            checkedTrackColor = PxColors.Primary.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onConfirm(selectedRows, selectedCols, hasHeader) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = PxColors.Primary,
                    contentColor = PxColors.OnPrimary
                )
            ) {
                Text(stringResource(R.string.editor_insert_table_btn), color = PxColors.OnPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = PxColors.OnSurfaceDim)
            }
        }
    )
}
