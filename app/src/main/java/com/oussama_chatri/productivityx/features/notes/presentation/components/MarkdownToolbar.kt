package com.oussama_chatri.productivityx.features.notes.presentation.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.oussama_chatri.productivityx.core.ui.theme.PxColors
import com.oussama_chatri.productivityx.core.ui.theme.PxRadius
import com.oussama_chatri.productivityx.R

sealed class MarkdownAction(val prefix: String = "", val suffix: String = "") {
    data object Bold : MarkdownAction("**", "**")
    data object Italic : MarkdownAction("*", "*")
    data object Underline : MarkdownAction("<u>", "</u>")
    data object Strikethrough : MarkdownAction("~~", "~~")
    data object Heading1 : MarkdownAction("# ")
    data object Heading2 : MarkdownAction("## ")
    data object Heading3 : MarkdownAction("### ")
    data object BulletList : MarkdownAction("- ")
    data object NumberedList : MarkdownAction("1. ")
    data object Code : MarkdownAction("`", "`")
    data object CodeBlock : MarkdownAction("```\n", "\n```")
    data object Quote : MarkdownAction("> ")
    data object Link : MarkdownAction("[", "](url)")
    data object Divider : MarkdownAction("\n---\n")
    data object Highlight : MarkdownAction("==", "==")
    data class InsertTable(val rows: Int, val cols: Int, val hasHeader: Boolean) : MarkdownAction()
    data class HighlightColor(val color: Color) : MarkdownAction()
    data class TextColor(val color: Color) : MarkdownAction()
    data class FontSize(val size: Int) : MarkdownAction()
}

object MarkdownPresetColors {
    val all = listOf(
        Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFF59E0B), Color(0xFFEAB308),
        Color(0xFF22C55E), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF3B82F6),
        Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFA855F7), Color(0xFFD946EF),
        Color(0xFFEC4899), Color(0xFFF43F5E), Color(0xFF78716C), Color(0xFFA8A29E),
        Color(0xFF64748B), Color(0xFF475569), Color(0xFF334155), Color(0xFF1E293B),
        Color(0xFFF87171), Color(0xFFFBBF24), Color(0xFF34D399), Color(0xFF60A5FA)
    )
}

object MarkdownFontSizes {
    val all = listOf(12, 14, 16, 18, 20, 22, 24, 28, 32, 36, 42, 48)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarkdownToolbar(
    onAction: (MarkdownAction) -> Unit,
    onAddImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorMenu by remember { mutableStateOf(false) }
    var showTextColorMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }

    Surface(
        color = PxColors.SurfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(color = PxColors.Outline.copy(alpha = 0.5f), thickness = 1.dp)
        Column {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .height(44.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolbarButton(Icons.Outlined.Image, stringResource(R.string.editor_format_image), onAddImageClick)

                SectionDivider()
                ToolbarButton(Icons.Outlined.FormatBold, stringResource(R.string.editor_format_bold)) { onAction(MarkdownAction.Bold) }
                ToolbarButton(Icons.Outlined.FormatItalic, stringResource(R.string.editor_format_italic)) { onAction(MarkdownAction.Italic) }
                ToolbarButton(Icons.Outlined.FormatUnderlined, stringResource(R.string.editor_format_underline)) { onAction(MarkdownAction.Underline) }
                ToolbarButton(Icons.Outlined.FormatStrikethrough, stringResource(R.string.editor_format_strikethrough)) { onAction(MarkdownAction.Strikethrough) }

                SectionDivider()
                ToolbarButton(Icons.Outlined.Title, stringResource(R.string.editor_format_heading1)) { onAction(MarkdownAction.Heading1) }
                ToolbarButton(Icons.Outlined.Title, stringResource(R.string.editor_format_heading2)) { onAction(MarkdownAction.Heading2) }
                ToolbarButton(Icons.Outlined.Title, stringResource(R.string.editor_format_heading3)) { onAction(MarkdownAction.Heading3) }
                ToolbarButton(Icons.Outlined.FormatListBulleted, stringResource(R.string.editor_format_list_bullet)) { onAction(MarkdownAction.BulletList) }
                ToolbarButton(Icons.Outlined.FormatListNumbered, stringResource(R.string.editor_format_list_numbered)) { onAction(MarkdownAction.NumberedList) }
                ToolbarButton(Icons.Outlined.FormatQuote, stringResource(R.string.editor_format_quote)) { onAction(MarkdownAction.Quote) }
                ToolbarButton(Icons.Outlined.Code, stringResource(R.string.editor_format_code)) { onAction(MarkdownAction.Code) }
                ToolbarButton(Icons.Outlined.DataObject, stringResource(R.string.editor_format_code_block)) { onAction(MarkdownAction.CodeBlock) }

                SectionDivider()
                ToolbarButton(Icons.Outlined.Link, stringResource(R.string.editor_format_link)) { onAction(MarkdownAction.Link) }
                ToolbarButton(Icons.Outlined.HorizontalRule, stringResource(R.string.editor_format_divider)) { onAction(MarkdownAction.Divider) }

                SectionDivider()
                Box {
                    ToolbarButton(Icons.Outlined.FormatColorFill, stringResource(R.string.editor_highlight_color)) { showColorMenu = true }
                    DropdownMenu(
                        expanded = showColorMenu,
                        onDismissRequest = { showColorMenu = false },
                        containerColor = PxColors.Surface
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editor_highlight_default), style = MaterialTheme.typography.bodySmall, color = PxColors.OnSurface) },
                            onClick = { showColorMenu = false; onAction(MarkdownAction.Highlight) }
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth()
                        ) {
                            MarkdownPresetColors.all.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(Modifier.size(24.dp))
                                        .clickable {
                                            showColorMenu = false
                                            onAction(MarkdownAction.HighlightColor(color))
                                        }
                                )
                            }
                        }
                    }
                }

                Box {
                    ToolbarButton(Icons.Outlined.Palette, stringResource(R.string.editor_text_color)) { showTextColorMenu = true }
                    DropdownMenu(
                        expanded = showTextColorMenu,
                        onDismissRequest = { showTextColorMenu = false },
                        containerColor = PxColors.Surface
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth()
                        ) {
                            MarkdownPresetColors.all.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(Modifier.size(24.dp))
                                        .clickable {
                                            showTextColorMenu = false
                                            onAction(MarkdownAction.TextColor(color))
                                        }
                                )
                            }
                        }
                    }
                }

                Box {
                    ToolbarButton(Icons.Outlined.FormatSize, stringResource(R.string.editor_font_size)) { showSizeMenu = true }
                    DropdownMenu(
                        expanded = showSizeMenu,
                        onDismissRequest = { showSizeMenu = false },
                        containerColor = PxColors.Surface
                    ) {
                        MarkdownFontSizes.all.forEach { size ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${size}sp",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = (size.coerceIn(8, 48)).sp
                                        ),
                                        color = PxColors.OnSurface
                                    )
                                },
                                onClick = { showSizeMenu = false; onAction(MarkdownAction.FontSize(size)) }
                            )
                        }
                    }
                }

                SectionDivider()
                ToolbarButton(Icons.Outlined.Edit, stringResource(R.string.editor_format_highlight)) { onAction(MarkdownAction.Highlight) }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .width(1.dp)
            .height(20.dp)
            .background(PxColors.Outline.copy(alpha = 0.3f))
    )
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = PxColors.OnSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}
