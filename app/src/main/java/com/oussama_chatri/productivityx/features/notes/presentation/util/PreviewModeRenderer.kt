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

sealed class PreviewBlock {
    data class Text(val annotatedString: AnnotatedString) : PreviewBlock()
    data class Image(val uri: String, val caption: String? = null) : PreviewBlock()
}

object PreviewModeRenderer {

    data class TableData(
        val headers: List<String>,
        val rows: List<List<String>>,
        val alignments: List<Char> = emptyList()
    )

    private val imageRegex = Regex("""!\[([^\]]*)\]\(([^)]+)\)""")

    fun renderToBlocks(content: String): List<PreviewBlock> {
        val blocks = mutableListOf<PreviewBlock>()
        val parts = imageRegex.split(content)
        val matches = imageRegex.findAll(content).toList()

        for ((idx, part) in parts.withIndex()) {
            if (part.isNotBlank()) {
                blocks.add(PreviewBlock.Text(renderText(part)))
            }
            if (idx < matches.size) {
                val m = matches[idx]
                blocks.add(PreviewBlock.Image(m.groupValues[2], m.groupValues[1].ifBlank { null }))
            }
        }
        return blocks
    }

    fun render(content: String): AnnotatedString = renderText(content)

    private fun renderText(content: String): AnnotatedString = buildAnnotatedString {
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

                // Task list
                trimmed.startsWith("- [") -> {
                    val checked = trimmed.length > 4 && trimmed[3] == 'x'
                    val text = trimmed.substring(trimmed.indexOf(']') + 1).trim()
                    val checkbox = if (checked) "☑" else "☐"
                    pushStyle(if (checked) SpanStyle(color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                        else SpanStyle(color = PxColors.OnSurfaceDim))
                    append("  $checkbox  ")
                    pop()
                    if (checked) {
                        pushStyle(SpanStyle(color = PxColors.OnSurfaceDim, textDecoration = TextDecoration.LineThrough))
                        append(renderInline(text))
                        pop()
                    } else {
                        append(renderInline(text))
                    }
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

    private val colorHighlightPreviewRegex = Regex("""==color:#?([0-9a-fA-F]{6}|[0-9a-fA-F]{3}):(.*?)==""")
    private val textColorOpenPreviewRegex = Regex("""\{color:#?([0-9a-fA-F]{6}|[0-9a-fA-F]{3})\}""")
    private val textColorClosePreviewRegex = Regex("""\{/color\}""")
    private val fontSizeOpenPreviewRegex = Regex("""\{size:(\d+)\}""")
    private val fontSizeClosePreviewRegex = Regex("""\{/size\}""")

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

        val colWidths = MutableList(numCols) { 12 }
        for ((ci, h) in headers.withIndex()) {
            colWidths[ci] = maxOf(colWidths[ci], h.length + 2)
        }
        for (row in dataRows) {
            for ((ci, cell) in row.withIndex()) {
                if (ci < numCols) colWidths[ci] = maxOf(colWidths[ci], cell.length + 2)
            }
        }

        pushStyle(SpanStyle(background = PxColors.Primary.copy(alpha = 0.10f), fontWeight = FontWeight.Bold, color = PxColors.OnBackground))
        append(" ")
        for ((ci, header) in headers.withIndex()) {
            val padded = when (alignments.getOrElse(ci) { 'l' }) {
                'c' -> header.padEnd(colWidths[ci]).padStart(colWidths[ci])
                'r' -> header.padStart(colWidths[ci])
                else -> header.padEnd(colWidths[ci])
            }
            append(padded)
        }
        pop()
        append("\n")

        pushStyle(SpanStyle(color = PxColors.Outline.copy(alpha = 0.4f)))
        append(" ")
        for (ci in 0 until numCols) {
            append("\u2500".repeat(colWidths[ci]))
        }
        pop()
        append("\n")

        for ((rowIdx, row) in dataRows.withIndex()) {
            if (rowIdx % 2 == 1) {
                pushStyle(SpanStyle(background = PxColors.Outline.copy(alpha = 0.06f)))
            }
            append(" ")
            for ((ci, cell) in row.withIndex()) {
                val padded = when (alignments.getOrElse(ci) { 'l' }) {
                    'c' -> cell.padEnd(colWidths[ci]).padStart(colWidths[ci])
                    'r' -> cell.padStart(colWidths[ci])
                    else -> cell.padEnd(colWidths[ci])
                }
                append(padded)
            }
            if (rowIdx % 2 == 1) pop()
            append("\n")
        }
    }

    private fun parseTableRow(line: String): List<String> {
        val s = line.trim().removeSurrounding("|")
        return s.split("|").map { it.trim() }
    }

    private fun AnnotatedString.Builder.renderInline(text: String): AnnotatedString = buildAnnotatedString {
        var remaining = text

        while (remaining.isNotEmpty()) {
            val chMatch = colorHighlightPreviewRegex.find(remaining)
            val tcOpenMatch = textColorOpenPreviewRegex.find(remaining)
            val fsOpenMatch = fontSizeOpenPreviewRegex.find(remaining)
            val tcCloseMatch = textColorClosePreviewRegex.find(remaining)
            val fsCloseMatch = fontSizeClosePreviewRegex.find(remaining)

            when {
                chMatch != null && chMatch.range.first == 0 -> {
                    val hex = chMatch.groupValues[1]
                    val content = chMatch.groupValues[2]
                    val bgColor = try { Color(hex.toLong(16) or 0xFF000000).copy(alpha = 0.3f) } catch (_: Exception) { PxColors.Primary.copy(alpha = 0.3f) }
                    remaining = remaining.substring(chMatch.range.last + 1)
                    pushStyle(SpanStyle(background = bgColor))
                    appendInlineParsed(content)
                    pop()
                }
                tcOpenMatch != null && tcOpenMatch.range.first == 0 -> {
                    val hex = tcOpenMatch.groupValues[1]
                    val fgColor = try { Color(hex.toLong(16) or 0xFF000000) } catch (_: Exception) { PxColors.OnSurface }
                    remaining = remaining.substring(tcOpenMatch.range.last + 1)
                    val closeIdx = textColorClosePreviewRegex.find(remaining)
                    val content = if (closeIdx != null) remaining.substring(0, closeIdx.range.first) else ""
                    remaining = if (closeIdx != null) remaining.substring(closeIdx.range.last + 1) else ""
                    pushStyle(SpanStyle(color = fgColor))
                    appendInlineParsed(content)
                    pop()
                }
                fsOpenMatch != null && fsOpenMatch.range.first == 0 -> {
                    val size = try { fsOpenMatch.groupValues[1].toInt().sp } catch (_: Exception) { 16.sp }
                    remaining = remaining.substring(fsOpenMatch.range.last + 1)
                    val closeIdx = fontSizeClosePreviewRegex.find(remaining)
                    val content = if (closeIdx != null) remaining.substring(0, closeIdx.range.first) else ""
                    remaining = if (closeIdx != null) remaining.substring(closeIdx.range.last + 1) else ""
                    pushStyle(SpanStyle(fontSize = size))
                    appendInlineParsed(content)
                    pop()
                }
                tcCloseMatch != null && tcCloseMatch.range.first == 0 -> {
                    remaining = remaining.substring(tcCloseMatch.range.last + 1)
                }
                fsCloseMatch != null && fsCloseMatch.range.first == 0 -> {
                    remaining = remaining.substring(fsCloseMatch.range.last + 1)
                }
                else -> {
                    val nextTag = Regex("""==color:|\Q{color:\E|\Q{size:\E|\Q{/color}\E|\Q{/size}\E""").find(remaining)
                    val segment = if (nextTag != null) remaining.substring(0, nextTag.range.first) else remaining
                    appendInlineParsed(segment)
                    remaining = if (nextTag != null) remaining.substring(nextTag.range.first) else ""
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendInlineParsed(text: String) {
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
            if (PdfInlineRenderer.Style.HIGHLIGHT in seg.styles) {
                styles.add(SpanStyle(
                    background = PxColors.Primary.copy(alpha = 0.3f)
                ))
            }
            if (seg.textColorHex != null) {
                val color = try { Color(seg.textColorHex.toLong(16) or 0xFF000000) } catch (_: Exception) { PxColors.OnSurface }
                styles.add(SpanStyle(color = color))
            }
            if (seg.fontSize != null) {
                styles.add(SpanStyle(fontSize = seg.fontSize.sp))
            }
            if (seg.highlightColorHex != null) {
                val color = try { Color(seg.highlightColorHex.toLong(16) or 0xFF000000).copy(alpha = 0.3f) } catch (_: Exception) { PxColors.Primary.copy(alpha = 0.3f) }
                styles.add(SpanStyle(background = color))
            }
            for (style in styles) pushStyle(style)
            append(seg.text)
            for (style in styles) pop()
        }
    }
}
