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
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Divider
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

sealed class MarkdownAction(val prefix: String, val suffix: String = "") {
    data object Bold         : MarkdownAction("**", "**")
    data object Italic       : MarkdownAction("*", "*")
    data object Heading      : MarkdownAction("## ")
    data object BulletList   : MarkdownAction("- ")
    data object NumberedList : MarkdownAction("1. ")
    data object Code         : MarkdownAction("`", "`")
    data object Quote        : MarkdownAction("> ")
    data object Link         : MarkdownAction("[", "](url)")
}

@Composable
fun MarkdownToolbar(
    onAction: (MarkdownAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color    = Color(0xFF252533),
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(color = Color(0xFF252533).copy(alpha = 0.5f), thickness = 1.dp)
        Row(
            modifier          = Modifier
                .horizontalScroll(rememberScrollState())
                .height(48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(Icons.Outlined.FormatBold,        "Bold")         { onAction(MarkdownAction.Bold) }
            ToolbarButton(Icons.Outlined.FormatItalic,      "Italic")       { onAction(MarkdownAction.Italic) }
            ToolbarButton(Icons.Outlined.Title,             "Heading")      { onAction(MarkdownAction.Heading) }
            ToolbarButton(Icons.Outlined.FormatListBulleted,"Bullet list")  { onAction(MarkdownAction.BulletList) }
            ToolbarButton(Icons.Outlined.FormatListNumbered,"Numbered list"){ onAction(MarkdownAction.NumberedList) }
            ToolbarButton(Icons.Outlined.Code,              "Code")         { onAction(MarkdownAction.Code) }
            ToolbarButton(Icons.Outlined.FormatQuote,       "Quote")        { onAction(MarkdownAction.Quote) }
            ToolbarButton(Icons.Outlined.Link,              "Link")         { onAction(MarkdownAction.Link) }
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
            imageVector        = icon,
            contentDescription = description,
            tint               = Color(0xFFCCCCD8),
            modifier           = Modifier.size(20.dp)
        )
    }
}
