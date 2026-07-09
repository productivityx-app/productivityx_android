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
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        var transformedText = ""
        val mappingToOriginal = mutableListOf<Int>()
        val mappingToTransformed = IntArray(originalText.length + 1)
        val styles = mutableListOf<Pair<SpanStyle, IntRange>>()

        var i = 0
        while (i < originalText.length) {
            val remaining = originalText.substring(i)

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

            // Table row
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
                    // Consume all table lines
                    val tableStartIdx = transformedText.length
                    val tableStartI = i
                    var renderedChars = 0

                    for ((rowIdx, lineStart) in tableLines.withIndex()) {
                        val lineEnd = originalText.indexOf('\n', lineStart).let { if (it == -1) originalText.length else it }
                        val rawLine = originalText.substring(lineStart, lineEnd)

                        val cells = rawLine.trim().removeSurrounding("|").split("|").map { it.trim() }

                        // Skip separator line (|---|)
                        if (rowIdx == 1 && cells.all { it.all { c -> c == '-' || c == ':' } }) {
                            // Skip separator - consume it without rendering
                            for (j in lineStart until lineEnd) {
                                mappingToTransformed[j] = transformedText.length
                            }
                            mappingToTransformed[lineEnd] = transformedText.length
                            continue
                        }

                        if (rowIdx > 0 && cells.all { it.all { c -> c == '-' || c == ':' } }) break

                        // Render cells
                        for ((colIdx, cell) in cells.withIndex()) {
                            if (colIdx > 0) transformedText += " │ "
                            val isHeader = rowIdx == 0
                            val cellStart = transformedText.length
                            for (ch in cell) {
                                val charIdx = lineStart + rawLine.indexOf(ch, if (colIdx == 0) rawLine.indexOf('|') + 1 else rawLine.indexOf('|', rawLine.indexOf("|$cell") + 1))
                                // Approximate mapping
                                transformedText += ch
                            }
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

            // Highlight ==...==
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
