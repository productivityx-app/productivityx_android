package com.oussama_chatri.productivityx.features.notes.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.core.ui.theme.PxColors

class MarkdownVisualTransformation : VisualTransformation {

    private val colorHighlightRegex = Regex("""==color:#?([0-9a-fA-F]{6}|[0-9a-fA-F]{3}):(.*?)==""")
    private val textColorOpenRegex = Regex("""\{color:#?([0-9a-fA-F]{6}|[0-9a-fA-F]{3})\}""")
    private val textColorCloseRegex = Regex("""\{/color\}""")
    private val fontSizeOpenRegex = Regex("""\{size:(\d+)\}""")
    private val fontSizeCloseRegex = Regex("""\{/size\}""")

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        var transformedText = ""
        val mappingToOriginal = mutableListOf<Int>()
        val mappingToTransformed = IntArray(originalText.length + 1)
        val styles = mutableListOf<Pair<SpanStyle, IntRange>>()

        var i = 0
        while (i < originalText.length) {
            val remaining = originalText.substring(i)

            // Check for `{color:#...}` / `{/color}` / `{size:N}` / `{/size}` closures
            // These are consumed silently without showing in transformed output
            val tcMatch = textColorCloseRegex.find(remaining)
            if (tcMatch != null && tcMatch.range.first == 0) {
                for (j in tcMatch.range) {
                    mappingToTransformed[i + j] = transformedText.length
                }
                i += tcMatch.range.last + 1
                continue
            }
            val fsMatch = fontSizeCloseRegex.find(remaining)
            if (fsMatch != null && fsMatch.range.first == 0) {
                for (j in fsMatch.range) {
                    mappingToTransformed[i + j] = transformedText.length
                }
                i += fsMatch.range.last + 1
                continue
            }

            // ---- LINE-LEVEL (start of line) ----

            // Divider ---
            if ((i == 0 || i + 3 <= originalText.length && originalText[i - 1] == '\n') && remaining.startsWith("---") && (i + 3 >= originalText.length || originalText[i + 3] == '\n')) {
                for (j in 0 until 3) {
                    mappingToTransformed[i + j] = transformedText.length
                }
                val startIdx = transformedText.length
                for (k in 0 until 40) transformedText += '\u2500'
                styles.add(SpanStyle(color = PxColors.Outline.copy(alpha = 0.5f)) to (startIdx..transformedText.length))
                i += 3
                continue
            }

            // Headings
            if ((i == 0 || originalText[i - 1] == '\n') && remaining.startsWith("#")) {
                var hashes = 0
                while (i + hashes < originalText.length && originalText[i + hashes] == '#') {
                    hashes++
                }
                if (hashes in 1..6 && i + hashes < originalText.length && originalText[i + hashes] == ' ') {
                    val scale = when (hashes) {
                        1 -> 1.5f; 2 -> 1.4f; 3 -> 1.3f; 4 -> 1.2f; 5 -> 1.1f; else -> 1.0f
                    }
                    val style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = (16f * scale).sp)

                    val consumeLen = hashes + 1
                    for (j in 0 until consumeLen) {
                        mappingToTransformed[i + j] = transformedText.length
                    }
                    i += consumeLen

                    val startIdx = transformedText.length
                    var endIdx = startIdx
                    while (i < originalText.length && originalText[i] != '\n') {
                        mappingToTransformed[i] = transformedText.length
                        mappingToOriginal.add(i)
                        transformedText += originalText[i]
                        endIdx++
                        i++
                    }
                    styles.add(style to startIdx..endIdx)
                    continue
                }
            }

            // Blockquote
            if ((i == 0 || originalText[i - 1] == '\n') && remaining.startsWith("> ")) {
                val style = SpanStyle(color = PxColors.OnSurfaceDim, fontStyle = FontStyle.Italic)

                mappingToTransformed[i] = transformedText.length
                mappingToTransformed[i + 1] = transformedText.length
                i += 2

                val startIdx = transformedText.length
                var endIdx = startIdx
                while (i < originalText.length && originalText[i] != '\n') {
                    mappingToTransformed[i] = transformedText.length
                    mappingToOriginal.add(i)
                    transformedText += originalText[i]
                    endIdx++
                    i++
                }
                styles.add(style to startIdx..endIdx)
                continue
            }

            // Task list - [ ] / - [x]
            if ((i == 0 || originalText[i - 1] == '\n') && remaining.startsWith("- [") && remaining.length > 4) {
                val checked = remaining[3] == 'x'
                val consumedLen = remaining.indexOf(']') + 1 + 1
                for (j in 0 until consumedLen) {
                    mappingToTransformed[i + j] = transformedText.length
                }
                i += consumedLen

                val startIdx = transformedText.length
                transformedText += if (checked) "☑ " else "☐ "
                while (i < originalText.length && originalText[i] != '\n') {
                    mappingToTransformed[i] = transformedText.length
                    mappingToOriginal.add(i)
                    transformedText += originalText[i]
                    i++
                }
                val checkStyle = if (checked) SpanStyle(color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                    else SpanStyle(color = PxColors.OnSurfaceDim)
                styles.add(checkStyle to (startIdx..startIdx + 1))
                if (checked) {
                    styles.add(SpanStyle(color = PxColors.OnSurfaceDim, textDecoration = TextDecoration.LineThrough) to (startIdx + 2 until transformedText.length))
                }
                continue
            }

            // Bullet list
            if ((i == 0 || originalText[i - 1] == '\n') && remaining.startsWith("- ") && i + 2 < originalText.length) {
                mappingToTransformed[i] = transformedText.length
                mappingToTransformed[i + 1] = transformedText.length
                i += 2

                val startIdx = transformedText.length
                transformedText += "• "
                var endIdx = startIdx + 2
                while (i < originalText.length && originalText[i] != '\n') {
                    mappingToTransformed[i] = transformedText.length
                    mappingToOriginal.add(i)
                    transformedText += originalText[i]
                    endIdx++
                    i++
                }
                styles.add(SpanStyle(fontWeight = FontWeight.Bold) to (startIdx..startIdx + 1))
                continue
            }

            // Numbered list
            if ((i == 0 || originalText[i - 1] == '\n')) {
                val numMatch = Regex("^(\\d+)\\. ").find(remaining)
                if (numMatch != null) {
                    val numStr = numMatch.value
                    val prefixLen = numStr.length

                    for (j in 0 until prefixLen) {
                        mappingToTransformed[i + j] = transformedText.length
                    }
                    i += prefixLen

                    val startIdx = transformedText.length
                    transformedText += "$numStr"
                    var endIdx = startIdx + prefixLen
                    while (i < originalText.length && originalText[i] != '\n') {
                        mappingToTransformed[i] = transformedText.length
                        mappingToOriginal.add(i)
                        transformedText += originalText[i]
                        endIdx++
                        i++
                    }
                    styles.add(SpanStyle(fontWeight = FontWeight.Bold) to (startIdx..startIdx + numStr.length - 1))
                    continue
                }
            }

            // Table row — enhanced with header bg and alignment
            if ((i == 0 || originalText[i - 1] == '\n') && remaining.startsWith("|") && remaining.length > 3) {
                val tableLines = mutableListOf<Int>()
                var scanIdx = i
                while (scanIdx < originalText.length) {
                    val lineEnd = originalText.indexOf('\n', scanIdx)
                    val lineLen = if (lineEnd == -1) originalText.length - scanIdx else lineEnd - scanIdx
                    val lineContent = originalText.substring(scanIdx, scanIdx + lineLen).trimStart()
                    if (lineContent.startsWith("|") && lineContent.endsWith("|")) {
                        tableLines.add(scanIdx)
                        scanIdx = if (lineEnd == -1) originalText.length else lineEnd + 1
                    } else break
                }

                if (tableLines.size >= 2) {
                    // Determine alignment from separator row (row 1)
                    val sepLineStart = tableLines.getOrNull(1) ?: (tableLines.last() + 1)
                    val sepLineEnd = originalText.indexOf('\n', sepLineStart).let { if (it == -1) originalText.length else it }
                    val sepRaw = originalText.substring(sepLineStart, sepLineEnd).trim().removeSurrounding("|")
                    val sepCells = sepRaw.split("|").map { it.trim() }
                    val alignments = sepCells.map { cell ->
                        when {
                            cell.startsWith(":") && cell.endsWith(":") -> 'c'
                            cell.endsWith(":") -> 'r'
                            else -> 'l'
                        }
                    }

                    for ((rowIdx, lineStart) in tableLines.withIndex()) {
                        val lineEnd = originalText.indexOf('\n', lineStart).let { if (it == -1) originalText.length else it }
                        val rawLine = originalText.substring(lineStart, lineEnd)
                        val cells = rawLine.trim().removeSurrounding("|").split("|").map { it.trim() }

                        // Skip separator line
                        if (rowIdx == 1 && cells.all { it.all { c -> c == '-' || c == ':' } }) {
                            for (j in lineStart until lineEnd) {
                                mappingToTransformed[j] = transformedText.length
                            }
                            mappingToTransformed[lineEnd] = transformedText.length
                            continue
                        }
                        if (rowIdx > 0 && cells.all { it.all { c -> c == '-' || c == ':' } }) break

                        val isHeader = rowIdx == 0

                        // Header background
                        if (isHeader) {
                            val hdrStart = transformedText.length
                            transformedText += " "
                            val hdrEnd = transformedText.length
                            styles.add(SpanStyle(background = PxColors.Primary.copy(alpha = 0.12f)) to (hdrStart until hdrEnd))
                        }

                        for ((colIdx, cell) in cells.withIndex()) {
                            if (colIdx > 0) {
                                transformedText += " "
                                val divIdx = transformedText.length
                                transformedText += "\u2502"
                                styles.add(SpanStyle(color = PxColors.Outline.copy(alpha = 0.4f)) to (divIdx until divIdx + 1))
                                transformedText += " "
                            }
                            val cellStart = transformedText.length
                            val align = alignments.getOrElse(colIdx) { 'l' }
                            val displayCell = when (align) {
                                'r' -> cell.padStart(cell.length + 1).take(16)
                                'c' -> cell.padStart(cell.length + 1).padEnd(cell.length + 2).take(16)
                                else -> cell.padEnd(cell.length + 1).take(16)
                            }
                            transformedText += displayCell
                            val cellEnd = transformedText.length
                            if (isHeader && cell.isNotEmpty()) {
                                styles.add(SpanStyle(fontWeight = FontWeight.Bold) to (cellStart until cellEnd))
                            }
                        }

                        // Map original positions
                        for (j in lineStart until lineEnd) {
                            mappingToTransformed[j] = transformedText.length
                        }
                        if (rowIdx < tableLines.size - 1) {
                            mappingToTransformed[lineEnd] = transformedText.length
                        }
                        transformedText += '\n'
                    }

                    i = if (tableLines.last() + 1 < originalText.length) {
                        val lastLineEnd = originalText.indexOf('\n', tableLines.last())
                        if (lastLineEnd == -1) originalText.length else lastLineEnd + 1
                    } else originalText.length
                    continue
                }
            }

            // ---- INLINE ----

            // Colored highlight ==color:hex:text==
            val chMatch = colorHighlightRegex.find(remaining)
            if (chMatch != null && chMatch.range.first == 0) {
                val hex = chMatch.groupValues[1]
                val content = chMatch.groupValues[2]
                val hexColor = try { Color(hex.toLong(16) or 0xFF000000) } catch (_: Exception) { PxColors.Primary.copy(alpha = 0.3f) }
                val style = SpanStyle(background = hexColor.copy(alpha = 0.3f))

                for (j in chMatch.range) {
                    mappingToTransformed[i + j] = transformedText.length
                }
                val fullLen = chMatch.range.last + 1
                val startIdx = transformedText.length
                for (ch in content) {
                    transformedText += ch
                }
                styles.add(style to (startIdx until transformedText.length))
                i += fullLen
                continue
            }

            // Text color opener {color:#...}
            val tcOpenMatch = textColorOpenRegex.find(remaining)
            if (tcOpenMatch != null && tcOpenMatch.range.first == 0) {
                val hex = tcOpenMatch.groupValues[1]
                val hexColor = try { Color(hex.toLong(16) or 0xFF000000) } catch (_: Exception) { PxColors.OnSurface }
                val style = SpanStyle(color = hexColor)

                for (j in tcOpenMatch.range) {
                    mappingToTransformed[i + j] = transformedText.length
                }
                val fullLen = tcOpenMatch.range.last + 1

                // Find the closing {/color} and apply color to content between
                val restAfterOpen = remaining.substring(fullLen)
                val closeMatch = textColorCloseRegex.find(restAfterOpen)
                val contentEnd = if (closeMatch != null) closeMatch.range.first else restAfterOpen.length
                val content = restAfterOpen.substring(0, contentEnd)
                val closeLen = if (closeMatch != null) closeMatch.range.last + 1 else 0
                val closeActualLen = if (closeMatch != null) 8 else 0 // {/color} = 8 chars

                val startIdx = transformedText.length
                for (ch in content) {
                    mappingToTransformed[i + fullLen + content.indexOf(ch)] = transformedText.length
                    transformedText += ch
                }
                styles.add(style to (startIdx until transformedText.length))

                // Map {/color}
                for (j in 0 until closeActualLen) {
                    val idx = i + fullLen + contentEnd + j
                    if (idx < mappingToTransformed.size) {
                        mappingToTransformed[idx] = transformedText.length
                    }
                }

                i += fullLen + contentEnd + closeActualLen
                continue
            }

            // Font size opener {size:N}
            val fsOpenMatch = fontSizeOpenRegex.find(remaining)
            if (fsOpenMatch != null && fsOpenMatch.range.first == 0) {
                val sizeStr = fsOpenMatch.groupValues[1]
                val fontSize = try { sizeStr.toInt().sp } catch (_: Exception) { 16.sp }
                val style = SpanStyle(fontSize = fontSize)

                for (j in fsOpenMatch.range) {
                    mappingToTransformed[i + j] = transformedText.length
                }
                val fullLen = fsOpenMatch.range.last + 1

                val restAfterOpen = remaining.substring(fullLen)
                val closeMatch = fontSizeCloseRegex.find(restAfterOpen)
                val contentEnd = if (closeMatch != null) closeMatch.range.first else restAfterOpen.length
                val content = restAfterOpen.substring(0, contentEnd)
                val closeActualLen = if (closeMatch != null) 7 else 0 // {/size} = 7 chars

                val startIdx = transformedText.length
                for (ch in content) {
                    transformedText += ch
                }
                styles.add(style to (startIdx until transformedText.length))

                for (j in 0 until closeActualLen) {
                    val idx = i + fullLen + contentEnd + j
                    if (idx < mappingToTransformed.size) {
                        mappingToTransformed[idx] = transformedText.length
                    }
                }

                i += fullLen + contentEnd + closeActualLen
                continue
            }

            // Code block ```...```
            if (remaining.startsWith("```")) {
                val end = remaining.indexOf("```", 3)
                if (end != -1) {
                    for (j in 0 until 3) {
                        mappingToTransformed[i + j] = transformedText.length
                    }
                    val content = remaining.substring(3, end)
                    val startIdx = transformedText.length
                    for (ch in content) {
                        mappingToTransformed[i + 3 + (content.indexOf(ch))] = transformedText.length
                        transformedText += ch
                    }
                    for (j in 0 until 3) {
                        mappingToTransformed[i + end + j] = transformedText.length
                    }
                    styles.add(SpanStyle(fontFamily = FontFamily.Monospace, background = PxColors.Outline.copy(alpha = 0.15f), color = PxColors.Primary) to (startIdx..startIdx + content.length))
                    i += end + 6
                    continue
                }
            }

            // Bold **...**
            if (remaining.startsWith("**")) {
                val end = remaining.indexOf("**", 2)
                if (end != -1) {
                    val content = remaining.substring(2, end)
                    val style = SpanStyle(fontWeight = FontWeight.Bold)

                    mappingToTransformed[i] = transformedText.length
                    mappingToTransformed[i + 1] = transformedText.length

                    val startIdx = transformedText.length
                    for (j in content.indices) {
                        mappingToTransformed[i + 2 + j] = transformedText.length
                        mappingToOriginal.add(i + 2 + j)
                        transformedText += content[j]
                    }

                    mappingToTransformed[i + end] = transformedText.length
                    mappingToTransformed[i + end + 1] = transformedText.length

                    styles.add(style to startIdx..transformedText.length)
                    i += end + 2
                    continue
                }
            }

            // Highlight ==...== (default, non-colored)
            if (remaining.startsWith("==")) {
                val end = remaining.indexOf("==", 2)
                if (end != -1) {
                    val content = remaining.substring(2, end)
                    val style = SpanStyle(background = PxColors.Primary.copy(alpha = 0.3f))

                    mappingToTransformed[i] = transformedText.length
                    mappingToTransformed[i + 1] = transformedText.length

                    val startIdx = transformedText.length
                    for (j in content.indices) {
                        mappingToTransformed[i + 2 + j] = transformedText.length
                        mappingToOriginal.add(i + 2 + j)
                        transformedText += content[j]
                    }

                    mappingToTransformed[i + end] = transformedText.length
                    mappingToTransformed[i + end + 1] = transformedText.length

                    styles.add(style to startIdx..transformedText.length)
                    i += end + 2
                    continue
                }
            }

            // Strikethrough ~~...~~
            if (remaining.startsWith("~~")) {
                val end = remaining.indexOf("~~", 2)
                if (end != -1) {
                    val content = remaining.substring(2, end)
                    val style = SpanStyle(textDecoration = TextDecoration.LineThrough)

                    mappingToTransformed[i] = transformedText.length
                    mappingToTransformed[i + 1] = transformedText.length

                    val startIdx = transformedText.length
                    for (j in content.indices) {
                        mappingToTransformed[i + 2 + j] = transformedText.length
                        mappingToOriginal.add(i + 2 + j)
                        transformedText += content[j]
                    }

                    mappingToTransformed[i + end] = transformedText.length
                    mappingToTransformed[i + end + 1] = transformedText.length

                    styles.add(style to startIdx..transformedText.length)
                    i += end + 2
                    continue
                }
            }

            // Underline <u>...</u>
            if (remaining.startsWith("<u>")) {
                val end = remaining.indexOf("</u>", 3)
                if (end != -1) {
                    val content = remaining.substring(3, end)
                    val style = SpanStyle(textDecoration = TextDecoration.Underline)

                    mappingToTransformed[i] = transformedText.length
                    mappingToTransformed[i + 1] = transformedText.length
                    mappingToTransformed[i + 2] = transformedText.length

                    val startIdx = transformedText.length
                    for (j in content.indices) {
                        mappingToTransformed[i + 3 + j] = transformedText.length
                        mappingToOriginal.add(i + 3 + j)
                        transformedText += content[j]
                    }

                    mappingToTransformed[i + end] = transformedText.length
                    mappingToTransformed[i + end + 1] = transformedText.length
                    mappingToTransformed[i + end + 2] = transformedText.length
                    mappingToTransformed[i + end + 3] = transformedText.length

                    styles.add(style to startIdx..transformedText.length)
                    i += end + 4
                    continue
                }
            }

            // Inline code `...`
            if (remaining.startsWith("`")) {
                val end = remaining.indexOf("`", 1)
                if (end != -1) {
                    val content = remaining.substring(1, end)

                    mappingToTransformed[i] = transformedText.length

                    val startIdx = transformedText.length
                    for (j in content.indices) {
                        mappingToTransformed[i + 1 + j] = transformedText.length
                        mappingToOriginal.add(i + 1 + j)
                        transformedText += content[j]
                    }

                    mappingToTransformed[i + end] = transformedText.length

                    styles.add(SpanStyle(fontFamily = FontFamily.Monospace, background = PxColors.Outline.copy(alpha = 0.15f), color = PxColors.Primary) to (startIdx..transformedText.length))
                    i += end + 1
                    continue
                }
            }

            // Image ![alt](url)
            if (remaining.startsWith("![")) {
                val closeBracket = remaining.indexOf("](", 1)
                val closeParen = if (closeBracket != -1) remaining.indexOf(")", closeBracket + 2) else -1
                if (closeBracket != -1 && closeParen != -1) {
                    val alt = remaining.substring(2, closeBracket)
                    val style = SpanStyle(
                        background = PxColors.Primary.copy(alpha = 0.12f),
                        color = PxColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    for (j in 0 until closeParen + 1) {
                        mappingToTransformed[i + j] = transformedText.length
                    }
                    val startIdx = transformedText.length
                    val placeholder = if (alt.isNotBlank()) "🖼 $alt" else "🖼"
                    for (ch in placeholder) {
                        transformedText += ch
                    }
                    styles.add(style to (startIdx until transformedText.length))
                    i += closeParen + 1
                    continue
                }
            }

            // Link [text](url)
            if (remaining.startsWith("[")) {
                val closeBracket = remaining.indexOf("](")
                val closeParen = if (closeBracket != -1) remaining.indexOf(")", closeBracket + 2) else -1
                if (closeBracket != -1 && closeParen != -1) {
                    val linkText = remaining.substring(1, closeBracket)
                    val url = remaining.substring(closeBracket + 2, closeParen)

                    val style = SpanStyle(color = Color(0xFF569CD6), textDecoration = TextDecoration.Underline)

                    mappingToTransformed[i] = transformedText.length
                    val startIdx = transformedText.length
                    for (j in linkText.indices) {
                        mappingToTransformed[i + 1 + j] = transformedText.length
                        mappingToOriginal.add(i + 1 + j)
                        transformedText += linkText[j]
                    }

                    for (k in linkText.length + 2 until closeParen) {
                        mappingToTransformed[i + 1 + k] = transformedText.length
                    }
                    mappingToTransformed[i + closeBracket] = transformedText.length
                    mappingToTransformed[i + closeBracket + 1] = transformedText.length

                    for (k in closeBracket + 2 until closeParen + 1) {
                        mappingToTransformed[i + k] = transformedText.length
                    }

                    styles.add(style to (startIdx..transformedText.length))
                    i += closeParen + 1
                    continue
                }
            }

            // Italic *...*
            if (remaining.startsWith("*")) {
                val end = remaining.indexOf("*", 1)
                if (end != -1 && remaining.getOrNull(1) != '*') {
                    val content = remaining.substring(1, end)
                    val style = SpanStyle(fontStyle = FontStyle.Italic)

                    mappingToTransformed[i] = transformedText.length

                    val startIdx = transformedText.length
                    for (j in content.indices) {
                        mappingToTransformed[i + 1 + j] = transformedText.length
                        mappingToOriginal.add(i + 1 + j)
                        transformedText += content[j]
                    }

                    mappingToTransformed[i + end] = transformedText.length

                    styles.add(style to startIdx..transformedText.length)
                    i += end + 1
                    continue
                }
            }

            // Normal char
            mappingToTransformed[i] = transformedText.length
            mappingToOriginal.add(i)
            transformedText += originalText[i]
            i++
        }

        mappingToTransformed[originalText.length] = transformedText.length
        mappingToOriginal.add(originalText.length)

        val annotatedString = buildAnnotatedString {
            append(transformedText)
            for ((style, range) in styles) {
                if (range.first < range.last) {
                    addStyle(style, range.first, range.last)
                }
            }
        }

        return TransformedText(
            text = annotatedString,
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset < 0) return 0
                    if (offset >= mappingToTransformed.size) return transformedText.length
                    return mappingToTransformed[offset]
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset < 0) return 0
                    if (offset >= mappingToOriginal.size) return originalText.length
                    return mappingToOriginal[offset]
                }
            }
        )
    }
}
