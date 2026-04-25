package com.oussama_chatri.productivityx.features.notes.presentation.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.features.notes.domain.model.Tag

private val tagColorPalette = listOf(
    "#6366F1", "#8B5CF6", "#EC4899", "#EF4444",
    "#F59E0B", "#22C55E", "#3B82F6", "#14B8A6"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagPickerSheet(
    allTags: List<Tag>,
    selectedTagIds: Set<String>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onTagToggle: (String) -> Unit,
    onCreateTag: (name: String, color: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newTagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(tagColorPalette.first()) }
    var showCreate by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color(0xFF1A1A24),
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier         = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text  = "Tags",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFEEEEF5)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (allTags.isEmpty() && !showCreate) {
                Text(
                    text  = "No tags yet. Create your first one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888899)
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    allTags.forEach { tag ->
                        val isSelected = tag.id in selectedTagIds
                        val tagColor   = runCatching { Color(android.graphics.Color.parseColor(tag.color)) }
                            .getOrDefault(Color(0xFF6366F1))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) tagColor.copy(0.2f) else Color(0xFF252533))
                                .border(1.dp, if (isSelected) tagColor else Color.Transparent, RoundedCornerShape(50.dp))
                                .clickable { onTagToggle(tag.id) }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector        = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint               = tagColor,
                                    modifier           = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text  = tag.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) tagColor else Color(0xFFCCCCD8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showCreate) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF252533))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value          = newTagName,
                        onValueChange  = { newTagName = it },
                        textStyle      = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFEEEEF5)),
                        cursorBrush    = SolidColor(Color(0xFF6366F1)),
                        singleLine     = true,
                        decorationBox  = { inner ->
                            if (newTagName.isEmpty()) {
                                Text("Tag name", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888899))
                            }
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        tagColorPalette.forEach { hex ->
                            val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .then(
                                        if (hex == selectedColor) Modifier.border(2.dp, Color.White, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showCreate = false; newTagName = "" }) {
                            Text("Cancel", color = Color(0xFF888899))
                        }
                        TextButton(
                            onClick = {
                                if (newTagName.isNotBlank()) {
                                    onCreateTag(newTagName.trim(), selectedColor)
                                    newTagName  = ""
                                    showCreate  = false
                                }
                            },
                            enabled = newTagName.isNotBlank()
                        ) {
                            Text("Create", color = Color(0xFF6366F1))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCreate = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment  = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Add,
                        contentDescription = "New tag",
                        tint               = Color(0xFF6366F1),
                        modifier           = Modifier.size(16.dp)
                    )
                    Text(
                        text  = "New tag",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF6366F1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
