package com.oussama_chatri.productivityx.features.notes.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

object PreviewModeRenderer {

    data class TableData(
        val headers: List<String>,
        val rows: List<List<String>>,
        val alignments: List<Char> = emptyList()
    )

    fun render(content: String): AnnotatedString = buildAnnotatedString {
        val lines = content.split("\n")
        var i = 0

        while (i < lines.size) {
            val rawLine = lines[i]
            val trimmed = rawLine.trimStart()

            when {
                // Code block
                trimmed.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    i++

                    val code = codeLines.joinToString("\n")
                    if (code.isNotEmpty()) {
                        append("\n")
                        val style = SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = PxColors.Primary,
                            background = PxColors.Outline.copy(alpha = 0.12f)
                        )
                        pushStyle(SpanStyle(background = PxColors.Outline.copy(alpha = 0.08f)))
                        append(" ")
                        pop()
                        for (line in codeLines) {
                            pushStyle(style)
                            append(line)
                            pop()
                            append("\n")
                        }
                        pushStyle(SpanStyle(background = PxColors.Outline.copy(alpha = 0.08f)))
                        append(" ")
                        pop()
                        append("\n")
                    }
                }

                // Heading
                trimmed.startsWith("#") -> {
                    val level = trimmed.takeWhile { it == '#' }.length
                    val text = trimmed.drop(level).trim()
                    val size = when (level) {
                        1 -> 24.sp; 2 -> 20.sp; 3 -> 17.sp; else -> 15.sp
                    }
                    if (text.isNotEmpty()) {
                        append("\n")
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = size, color = PxColors.OnBackground))
                        append(renderInline(text))
                        pop()
                        append("\n\n")
                    }
                    i++
                }

                // Blockquote
                trimmed.startsWith("> ") -> {
                    val quoteText = trimmed.removePrefix("> ").trim()
                    pushStyle(SpanStyle(color = PxColors.OnSurfaceDim, fontStyle = FontStyle.Italic))
                    append("  ")
                    append(renderInline(quoteText))
                    pop()
                    append("\n")
                    i++
                }

                // Bullet list
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val text = trimmed.removePrefix("- ").removePrefix("* ").trim()
                    append("  •  ")
                    append(renderInline(text))
                    append("\n")
                    i++
                }

                // Numbered list
                trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val dotIdx = trimmed.indexOf('.')
                    val num = trimmed.substring(0, dotIdx + 1)
                    val text = trimmed.substring(dotIdx + 1).trim()
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PxColors.OnSurface))
                    append("  $num ")
                    pop()
                    append(renderInline(text))
                    append("\n")
                    i++
                }

                // Divider
                trimmed.matches(Regex("^-{3,}$")) || trimmed.matches(Regex("^\\*{3,}$")) || trimmed.matches(Regex("^_{3,}$")) -> {
                    append("\n")
                    pushStyle(SpanStyle(color = PxColors.Outline.copy(alpha = 0.5f)))
                    append("\u2500".repeat(40))
                    pop()
                    append("\n\n")
                    i++
                }

                // Table
                trimmed.startsWith("|") && trimmed.endsWith("|") -> {
                    val tableRows = mutableListOf<String>()
                    while (i < lines.size && lines[i].trimStart().startsWith("|")) {
                        tableRows.add(lines[i].trimStart())
                        i++
                    }
                    renderTable(tableRows)
                }

                // Empty line
                trimmed.isEmpty() -> {
                    append("\n")
                    i++
                }

                // Paragraph
                else -> {
                    val paraLines = mutableListOf(rawLine)
                    i++
                    while (i < lines.size) {
                        val next = lines[i].trimStart()
                        if (next.isEmpty() || next.startsWith("#") || next.startsWith("```") ||
                            next.startsWith("> ") || next.startsWith("- ") || next.startsWith("* ") ||
                            next.matches(Regex("^\\d+\\.\\s.*")) || next.matches(Regex("^-{3,}$")) ||
                            next.matches(Regex("^\\*{3,}$")) || next.matches(Regex("^_{3,}$")) ||
                            (next.startsWith("|") && next.endsWith("|"))
                        ) break
                        paraLines.add(lines[i])
                        i++
                    }
                    append(renderInline(paraLines.joinToString(" ")))
                    append("\n\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.renderTable(tableRows: List<String>) {
        if (tableRows.size < 2) return

        val headers = parseTableRow(tableRows[0])
        val separator = if (tableRows.size > 1) parseTableRow(tableRows[1]) else emptyList()
        val alignments = separator.map { cell ->
            val t = cell.trim()
            when {
                t.startsWith(":") && t.endsWith(":") -> 'c'
                t.endsWith(":") -> 'r'
                t.startsWith(":") -> 'l'
                else -> 'l'
            }
        }

        val dataRows = tableRows.drop(2).map { parseTableRow(it) }

        val numCols = maxOf(headers.size, dataRows.maxOfOrNull { it.size } ?: 0)
        if (numCols == 0) return

        // Calculate column widths
        val colWidths = MutableList(numCols) { 12 }
        for ((ci, h) in headers.withIndex()) {
            colWidths[ci] = maxOf(colWidths[ci], h.length + 2)
        }
        for (row in dataRows) {
            for ((ci, cell) in row.withIndex()) {
                if (ci < numCols) colWidths[ci] = maxOf(colWidths[ci], cell.length + 2)
            }
        }

        // Render header
        append(" ")
        for ((ci, header) in headers.withIndex()) {
            val padded = header.padEnd(colWidths[ci])
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PxColors.OnBackground))
            append(padded)
            pop()
        }
        append("\n")

        // Render separator
        append(" ")
        pushStyle(SpanStyle(color = PxColors.Outline.copy(alpha = 0.4f)))
        for (ci in 0 until numCols) {
            append("\u2500".repeat(colWidths[ci]))
        }
        pop()
        append("\n")

        // Render data rows
        for (row in dataRows) {
            append(" ")
            for ((ci, cell) in row.withIndex()) {
                val padded = cell.padEnd(colWidths[ci])
                append(padded)
            }
            append("\n")
        }
        append("\n")
    }

    private fun parseTableRow(line: String): List<String> {
        val s = line.trim().removeSurrounding("|")
        return s.split("|").map { it.trim() }
    }

    private fun renderInline(text: String): AnnotatedString = buildAnnotatedString {
        val inline = PdfInlineRenderer()
        val segments = inline.parseInline(text)
        for (seg in segments) {
            val styles = mutableListOf<SpanStyle>()
            if (PdfInlineRenderer.Style.BOLD in seg.styles) {
                styles.add(SpanStyle(fontWeight = FontWeight.Bold))
            }
            if (PdfInlineRenderer.Style.ITALIC in seg.styles) {
                styles.add(SpanStyle(fontStyle = FontStyle.Italic))
            }
            if (PdfInlineRenderer.Style.STRIKETHROUGH in seg.styles) {
                styles.add(SpanStyle(textDecoration = TextDecoration.LineThrough))
            }
            if (PdfInlineRenderer.Style.UNDERLINE in seg.styles) {
                styles.add(SpanStyle(textDecoration = TextDecoration.Underline))
            }
            if (PdfInlineRenderer.Style.CODE in seg.styles) {
                styles.add(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = PxColors.Outline.copy(alpha = 0.12f),
                    color = PxColors.Primary
                ))
            }
            if (PdfInlineRenderer.Style.LINK in seg.styles) {
                styles.add(SpanStyle(
                    color = Color(0xFF569CD6),
                    textDecoration = TextDecoration.Underline
                ))
            }
            for (style in styles) pushStyle(style)
            append(seg.text)
            for (style in styles) pop()
        }
    }
}
