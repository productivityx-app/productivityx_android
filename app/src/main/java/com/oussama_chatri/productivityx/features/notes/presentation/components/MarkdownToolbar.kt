package com.oussama_chatri.productivityx.features.notes.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

sealed class MarkdownAction(val prefix: String, val suffix: String = "") {
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
}

@Composable
fun MarkdownToolbar(
    onAction: (MarkdownAction) -> Unit,
    onAddImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = PxColors.SurfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(color = PxColors.Outline.copy(alpha = 0.5f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .height(48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(Icons.Outlined.Image, "Add image", onAddImageClick)
            ToolbarButton(Icons.Outlined.FormatBold, "Bold") { onAction(MarkdownAction.Bold) }
            ToolbarButton(Icons.Outlined.FormatItalic, "Italic") { onAction(MarkdownAction.Italic) }
            ToolbarButton(Icons.Outlined.FormatUnderlined, "Underline") { onAction(MarkdownAction.Underline) }
            ToolbarButton(Icons.Outlined.FormatStrikethrough, "Strikethrough") { onAction(MarkdownAction.Strikethrough) }
            ToolbarButton(Icons.Outlined.Title, "H1") { onAction(MarkdownAction.Heading1) }
            ToolbarButton(Icons.Outlined.Title, "H2") { onAction(MarkdownAction.Heading2) }
            ToolbarButton(Icons.Outlined.Title, "H3") { onAction(MarkdownAction.Heading3) }
            ToolbarButton(Icons.Outlined.FormatListBulleted, "Bullet list") { onAction(MarkdownAction.BulletList) }
            ToolbarButton(Icons.Outlined.FormatListNumbered, "Numbered list") { onAction(MarkdownAction.NumberedList) }
            ToolbarButton(Icons.Outlined.Code, "Code") { onAction(MarkdownAction.Code) }
            ToolbarButton(Icons.Outlined.DataObject, "Code block") { onAction(MarkdownAction.CodeBlock) }
            ToolbarButton(Icons.Outlined.FormatQuote, "Quote") { onAction(MarkdownAction.Quote) }
            ToolbarButton(Icons.Outlined.Link, "Link") { onAction(MarkdownAction.Link) }
            ToolbarButton(Icons.Outlined.HorizontalRule, "Divider") { onAction(MarkdownAction.Divider) }
        }
    }
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
