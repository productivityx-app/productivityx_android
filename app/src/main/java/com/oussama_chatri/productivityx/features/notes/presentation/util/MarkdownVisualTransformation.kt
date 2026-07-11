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

    private class TransformState(
        val originalText: String,
        val mappingToTransformed: IntArray,
        val mappingToOriginal: MutableList<Int>,
        var transformedText: String = "",
        val styles: MutableList<Pair<SpanStyle, IntRange>> = mutableListOf()
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val mappingToOriginal = mutableListOf<Int>()
        val mappingToTransformed = IntArray(originalText.length + 1)
        val styles = mutableListOf<Pair<SpanStyle, IntRange>>()

        val state = TransformState(originalText, mappingToTransformed, mappingToOriginal, "", styles)

        var i = 0
        while (i < originalText.length) {
            val remaining = originalText.substring(i)

            val tcMatch = textColorCloseRegex.find(remaining)
            if (tcMatch != null && tcMatch.range.first == 0) {
                for (j in tcMatch.range) {
                    mappingToTransformed[i + j] = state.transformedText.length
                }
                i += tcMatch.range.last + 1
                continue
            }
            val fsMatch = fontSizeCloseRegex.find(remaining)
            if (fsMatch != null && fsMatch.range.first == 0) {
                for (j in fsMatch.range) {
                    mappingToTransformed[i + j] = state.transformedText.length
                }
                i += fsMatch.range.last + 1
                continue
            }

            // ---- LINE-LEVEL ----

            if ((i == 0 || i + 3 <= originalText.length && originalText[i - 1] == '\n') && remaining.startsWith("---") && (i + 3 >= originalText.length || originalText[i + 3] == '\n')) {
                for (j in 0 until 3) {
                    mappingToTransformed[i + j] = state.transformedText.length
                }
                val startIdx = state.transformedText.length
                for (k in 0 until 40) state.transformedText += '\u2500'
                styles.add(SpanStyle(color = PxColors.Outline.copy(alpha = 0.5f)) to (startIdx..state.transformedText.length))
                i += 3
                continue
            }

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
                        mappingToTransformed[i + j] = state.transformedText.length
                    }
                    i += consumeLen
                    val startIdx = state.transformedText.length
                    var endIdx = startIdx
                    while (i < originalText.length && originalText[i] != '\n') {
                        mappingToTransformed[i] = state.transformedText.length
                        mappingToOriginal.add(i)
                        state.transformedText += originalText[i]
                        endIdx++
                        i++
                    }
                    styles.add(style to startIdx..endIdx)
                    continue
                }
            }

            if ((i == 0 || originalText[i - 1] == '\n') && remaining.startsWith("> ")) {
                val style = SpanStyle(color = PxColors.OnSurfaceDim, fontStyle = FontStyle.Italic)
                mappingToTransformed[i] = state.transformedText.length
                mappingToTransformed[i + 1] = state.transformedText.length
                i += 2
                val startIdx = state.transformedText.length
                var endIdx = startIdx
                while (i < originalText.length && originalText[i] != '\n') {
                    mappingToTransformed[i] = state.transformedText.length
                    mappingToOriginal.add(i)
                    state.transformedText += originalText[i]
                    endIdx++
                    i++
                }
                styles.add(style to startIdx..endIdx)
                continue
            }

            if ((i == 0 || originalText[i - 1] == '\n') && remaining.startsWith("- [") && remaining.length > 4) {
                val checked = remaining[3] == 'x'
                val consumedLen = remaining.indexOf(']') + 1 + 1
                for (j in 0 until consumedLen) {
                    mappingToTransformed[i + j] = state.transformedText.length
                }
                i += consumedLen
                val startIdx = state.transformedText.length
                state.transformedText += if (checked) "☑ " else "☐ "
                while (i < originalText.length && originalText[i] != '\n') {
                    mappingToTransformed[i] = state.transformedText.length
                    mappingToOriginal.add(i)
                    state.transformedText += originalText[i]
                    i++
                }
                val checkStyle = if (checked) SpanStyle(color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                else SpanStyle(color = PxColors.OnSurfaceDim)
                styles.add(checkStyle to (startIdx..startIdx + 1))
                if (checked) {
                    styles.add(SpanStyle(color = PxColors.OnSurfaceDim, textDecoration = TextDecoration.LineThrough) to (startIdx + 2 until state.transformedText.length))
                }
                continue
            }

            if ((i == 0 || originalText[i - 1] == '\n') && remaining.startsWith("- ") && i + 2 < originalText.length) {
                mappingToTransformed[i] = state.transformedText.length
                mappingToTransformed[i + 1] = state.transformedText.length
                i += 2
                val startIdx = state.transformedText.length
                state.transformedText += "• "
                var endIdx = startIdx + 2
                while (i < originalText.length && originalText[i] != '\n') {
                    mappingToTransformed[i] = state.transformedText.length
                    mappingToOriginal.add(i)
                    state.transformedText += originalText[i]
                    endIdx++
                    i++
                }
                styles.add(SpanStyle(fontWeight = FontWeight.Bold) to (startIdx..startIdx + 1))
                continue
            }

            if ((i == 0 || originalText[i - 1] == '\n')) {
                val numMatch = Regex("^(\\d+)\\. ").find(remaining)
                if (numMatch != null) {
                    val numStr = numMatch.value
                    val prefixLen = numStr.length
                    for (j in 0 until prefixLen) {
                        mappingToTransformed[i + j] = state.transformedText.length
                    }
                    i += prefixLen
                    val startIdx = state.transformedText.length
                    state.transformedText += "$numStr"
                    var endIdx = startIdx + prefixLen
                    while (i < originalText.length && originalText[i] != '\n') {
                        mappingToTransformed[i] = state.transformedText.length
                        mappingToOriginal.add(i)
                        state.transformedText += originalText[i]
                        endIdx++
                        i++
                    }
                    styles.add(SpanStyle(fontWeight = FontWeight.Bold) to (startIdx..startIdx + numStr.length - 1))
                    continue
                }
            }

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

                        if (rowIdx == 1 && cells.all { it.all { c -> c == '-' || c == ':' } }) {
                            for (j in lineStart until lineEnd) {
                                mappingToTransformed[j] = state.transformedText.length
                            }
                            mappingToTransformed[lineEnd] = state.transformedText.length
                            continue
                        }
                        if (rowIdx > 0 && cells.all { it.all { c -> c == '-' || c == ':' } }) break

                        val isHeader = rowIdx == 0

                        if (isHeader) {
                            val hdrStart = state.transformedText.length
                            state.transformedText += " "
                            val hdrEnd = state.transformedText.length
                            styles.add(SpanStyle(background = PxColors.Primary.copy(alpha = 0.12f)) to (hdrStart until hdrEnd))
                        }

                        for ((colIdx, cell) in cells.withIndex()) {
                            if (colIdx > 0) {
                                state.transformedText += " "
                                val divIdx = state.transformedText.length
                                state.transformedText += "\u2502"
                                styles.add(SpanStyle(color = PxColors.Outline.copy(alpha = 0.4f)) to (divIdx until divIdx + 1))
                                state.transformedText += " "
                            }
                            val cellStart = state.transformedText.length
                            val align = alignments.getOrElse(colIdx) { 'l' }
                            val displayCell = when (align) {
                                'r' -> cell.padStart(cell.length + 1).take(16)
                                'c' -> cell.padStart(cell.length + 1).padEnd(cell.length + 2).take(16)
                                else -> cell.padEnd(cell.length + 1).take(16)
                            }
                            state.transformedText += displayCell
                            val cellEnd = state.transformedText.length
                            if (isHeader && cell.isNotEmpty()) {
                                styles.add(SpanStyle(fontWeight = FontWeight.Bold) to (cellStart until cellEnd))
                            }
                        }

                        for (j in lineStart until lineEnd) {
                            mappingToTransformed[j] = state.transformedText.length
                        }
                        if (rowIdx < tableLines.size - 1) {
                            mappingToTransformed[lineEnd] = state.transformedText.length
                        }
                        state.transformedText += '\n'
                    }

                    i = if (tableLines.last() + 1 < originalText.length) {
                        val lastLineEnd = originalText.indexOf('\n', tableLines.last())
                        if (lastLineEnd == -1) originalText.length else lastLineEnd + 1
                    } else originalText.length
                    continue
                }
            }

            if (remaining.startsWith("```")) {
                val end = remaining.indexOf("```", 3)
                if (end != -1) {
                    for (j in 0 until 3) {
                        mappingToTransformed[i + j] = state.transformedText.length
                    }
                    val content = remaining.substring(3, end)
                    val startIdx = state.transformedText.length
                    for (ch in content.indices) {
                        mappingToTransformed[i + 3 + ch] = state.transformedText.length
                        state.transformedText += content[ch]
                    }
                    for (j in 0 until 3) {
                        mappingToTransformed[i + end + j] = state.transformedText.length
                    }
                    styles.add(SpanStyle(fontFamily = FontFamily.Monospace, background = PxColors.Outline.copy(alpha = 0.15f), color = PxColors.Primary) to (startIdx..startIdx + content.length))
                    i += end + 6
                    continue
                }
            }

            // ---- INLINE (recursive) ----
            val lineEnd = originalText.indexOf('\n', i).let { if (it == -1) originalText.length else it }
            val prevI = i
            i = processInlineContent(state, i, lineEnd)
            if (i == prevI) {
                if (i < state.mappingToTransformed.size) {
                    state.mappingToTransformed[i] = state.transformedText.length
                }
                state.mappingToOriginal.add(i)
                state.transformedText += originalText[i]
                i++
            }
        }

        mappingToTransformed[originalText.length] = state.transformedText.length
        mappingToOriginal.add(originalText.length)

        val annotatedString = buildAnnotatedString {
            append(state.transformedText)
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
                    if (offset >= mappingToTransformed.size) return state.transformedText.length
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

    private fun processInlineContent(
        state: TransformState,
        startIdx: Int,
        endIdx: Int = state.originalText.length
    ): Int {
        var i = startIdx
        val ot = state.originalText

        while (i < endIdx && ot[i] != '\n') {
            val remaining = ot.substring(i, endIdx)

            // Stray {/color}
            val tcCloseMatch = textColorCloseRegex.find(remaining)
            if (tcCloseMatch != null && tcCloseMatch.range.first == 0) {
                for (j in tcCloseMatch.range) {
                    if (i + j < state.mappingToTransformed.size) {
                        state.mappingToTransformed[i + j] = state.transformedText.length
                    }
                }
                i += tcCloseMatch.range.last + 1
                continue
            }

            // Stray {/size}
            val fsCloseMatch = fontSizeCloseRegex.find(remaining)
            if (fsCloseMatch != null && fsCloseMatch.range.first == 0) {
                for (j in fsCloseMatch.range) {
                    if (i + j < state.mappingToTransformed.size) {
                        state.mappingToTransformed[i + j] = state.transformedText.length
                    }
                }
                i += fsCloseMatch.range.last + 1
                continue
            }

            // Colored highlight ==color:hex:text==
            val chMatch = colorHighlightRegex.find(remaining)
            if (chMatch != null && chMatch.range.first == 0) {
                val hex = chMatch.groupValues[1]
                val content = chMatch.groupValues[2]
                val hexColor = try { Color(hex.toLong(16) or 0xFF000000) } catch (_: Exception) { PxColors.Primary.copy(alpha = 0.3f) }
                val style = SpanStyle(background = hexColor.copy(alpha = 0.3f))

                val fullMatchLen = chMatch.range.last + 1
                for (j in 0 until fullMatchLen) {
                    if (i + j < state.mappingToTransformed.size) {
                        state.mappingToTransformed[i + j] = state.transformedText.length
                    }
                }

                val prefixLen = fullMatchLen - content.length - 2
                val contentStart = i + prefixLen
                val contentEnd = contentStart + content.length

                val styleStart = state.transformedText.length
                processInlineContent(state, contentStart, contentEnd)
                state.styles.add(style to (styleStart until state.transformedText.length))

                i += fullMatchLen
                continue
            }

            // Text color {color:#hex}...{/color}
            val tcOpenMatch = textColorOpenRegex.find(remaining)
            if (tcOpenMatch != null && tcOpenMatch.range.first == 0) {
                val hex = tcOpenMatch.groupValues[1]
                val hexColor = try { Color(hex.toLong(16) or 0xFF000000) } catch (_: Exception) { PxColors.OnSurface }
                val style = SpanStyle(color = hexColor)

                val openLen = tcOpenMatch.range.last + 1
                for (j in 0 until openLen) {
                    if (i + j < state.mappingToTransformed.size) {
                        state.mappingToTransformed[i + j] = state.transformedText.length
                    }
                }

                val restAfterOpen = if (i + openLen < endIdx) ot.substring(i + openLen, endIdx) else ""
                val closeMatch = textColorCloseRegex.find(restAfterOpen)
                val contentLen = if (closeMatch != null) closeMatch.range.first else restAfterOpen.length
                val closeLen = if (closeMatch != null) 8 else 0

                val contentStart = i + openLen
                val contentEndIdx = contentStart + contentLen
                val styleStart = state.transformedText.length
                processInlineContent(state, contentStart, contentEndIdx)
                state.styles.add(style to (styleStart until state.transformedText.length))

                for (j in 0 until closeLen) {
                    val idx = contentEndIdx + j
                    if (idx < state.mappingToTransformed.size) {
                        state.mappingToTransformed[idx] = state.transformedText.length
                    }
                }

                i = contentEndIdx + closeLen
                continue
            }

            // Font size {size:N}...{/size}
            val fsOpenMatch = fontSizeOpenRegex.find(remaining)
            if (fsOpenMatch != null && fsOpenMatch.range.first == 0) {
                val sizeStr = fsOpenMatch.groupValues[1]
                val fontSize = try { sizeStr.toInt().sp } catch (_: Exception) { 16.sp }
                val style = SpanStyle(fontSize = fontSize)

                val openLen = fsOpenMatch.range.last + 1
                for (j in 0 until openLen) {
                    if (i + j < state.mappingToTransformed.size) {
                        state.mappingToTransformed[i + j] = state.transformedText.length
                    }
                }

                val restAfterOpen = if (i + openLen < endIdx) ot.substring(i + openLen, endIdx) else ""
                val closeMatch = fontSizeCloseRegex.find(restAfterOpen)
                val contentLen = if (closeMatch != null) closeMatch.range.first else restAfterOpen.length
                val closeLen = if (closeMatch != null) 7 else 0

                val contentStart = i + openLen
                val contentEndIdx = contentStart + contentLen
                val styleStart = state.transformedText.length
                processInlineContent(state, contentStart, contentEndIdx)
                state.styles.add(style to (styleStart until state.transformedText.length))

                for (j in 0 until closeLen) {
                    val idx = contentEndIdx + j
                    if (idx < state.mappingToTransformed.size) {
                        state.mappingToTransformed[idx] = state.transformedText.length
                    }
                }

                i = contentEndIdx + closeLen
                continue
            }

            // Inline code `...` (no recursive processing)
            if (remaining.startsWith("`")) {
                val end = remaining.indexOf("`", 1)
                if (end != -1) {
                    val content = remaining.substring(1, end)

                    if (i < state.mappingToTransformed.size) {
                        state.mappingToTransformed[i] = state.transformedText.length
                    }

                    val startT = state.transformedText.length
                    for (j in content.indices) {
                        if (i + 1 + j < state.mappingToTransformed.size) {
                            state.mappingToTransformed[i + 1 + j] = state.transformedText.length
                        }
                        state.mappingToOriginal.add(i + 1 + j)
                        state.transformedText += content[j]
                    }

                    if (i + end < state.mappingToTransformed.size) {
                        state.mappingToTransformed[i + end] = state.transformedText.length
                    }

                    state.styles.add(SpanStyle(fontFamily = FontFamily.Monospace, background = PxColors.Outline.copy(alpha = 0.15f), color = PxColors.Primary) to (startT..state.transformedText.length))
                    i += end + 1
                    continue
                }
            }

            // Bold **...** (recursive content)
            if (remaining.startsWith("**")) {
                val end = remaining.indexOf("**", 2)
                if (end != -1) {
                    val style = SpanStyle(fontWeight = FontWeight.Bold)

                    state.mappingToTransformed[i] = state.transformedText.length
                    state.mappingToTransformed[i + 1] = state.transformedText.length

                    val styleStart = state.transformedText.length
                    processInlineContent(state, i + 2, i + end)
                    state.styles.add(style to (styleStart..state.transformedText.length))

                    state.mappingToTransformed[i + end] = state.transformedText.length
                    state.mappingToTransformed[i + end + 1] = state.transformedText.length

                    i += end + 2
                    continue
                }
            }

            // Highlight ==...== (recursive content)
            if (remaining.startsWith("==")) {
                val end = remaining.indexOf("==", 2)
                if (end != -1) {
                    val style = SpanStyle(background = PxColors.Primary.copy(alpha = 0.3f))

                    state.mappingToTransformed[i] = state.transformedText.length
                    state.mappingToTransformed[i + 1] = state.transformedText.length

                    val styleStart = state.transformedText.length
                    processInlineContent(state, i + 2, i + end)
                    state.styles.add(style to (styleStart..state.transformedText.length))

                    state.mappingToTransformed[i + end] = state.transformedText.length
                    state.mappingToTransformed[i + end + 1] = state.transformedText.length

                    i += end + 2
                    continue
                }
            }

            // Strikethrough ~~...~~ (recursive content)
            if (remaining.startsWith("~~")) {
                val end = remaining.indexOf("~~", 2)
                if (end != -1) {
                    val style = SpanStyle(textDecoration = TextDecoration.LineThrough)

                    state.mappingToTransformed[i] = state.transformedText.length
                    state.mappingToTransformed[i + 1] = state.transformedText.length

                    val styleStart = state.transformedText.length
                    processInlineContent(state, i + 2, i + end)
                    state.styles.add(style to (styleStart..state.transformedText.length))

                    state.mappingToTransformed[i + end] = state.transformedText.length
                    state.mappingToTransformed[i + end + 1] = state.transformedText.length

                    i += end + 2
                    continue
                }
            }

            // Underline <u>...</u> (recursive content)
            if (remaining.startsWith("<u>")) {
                val end = remaining.indexOf("</u>", 3)
                if (end != -1) {
                    val style = SpanStyle(textDecoration = TextDecoration.Underline)

                    for (j in 0 until 3) {
                        state.mappingToTransformed[i + j] = state.transformedText.length
                    }

                    val styleStart = state.transformedText.length
                    processInlineContent(state, i + 3, i + end)
                    state.styles.add(style to (styleStart..state.transformedText.length))

                    for (j in 0 until 4) {
                        state.mappingToTransformed[i + end + j] = state.transformedText.length
                    }

                    i += end + 4
                    continue
                }
            }

            // Image ![alt](url) (no recursive processing)
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
                        if (i + j < state.mappingToTransformed.size) {
                            state.mappingToTransformed[i + j] = state.transformedText.length
                        }
                    }
                    val startT = state.transformedText.length
                    val placeholder = if (alt.isNotBlank()) "\uD83D\uDDBC $alt" else "\uD83D\uDDBC"
                    for (ch in placeholder) {
                        state.transformedText += ch
                    }
                    state.styles.add(style to (startT until state.transformedText.length))
                    i += closeParen + 1
                    continue
                }
            }

            // Link [text](url) (no recursive processing on text)
            if (remaining.startsWith("[")) {
                val closeBracket = remaining.indexOf("](")
                val closeParen = if (closeBracket != -1) remaining.indexOf(")", closeBracket + 2) else -1
                if (closeBracket != -1 && closeParen != -1) {
                    val linkText = remaining.substring(1, closeBracket)

                    val style = SpanStyle(color = Color(0xFF569CD6), textDecoration = TextDecoration.Underline)

                    state.mappingToTransformed[i] = state.transformedText.length
                    val startT = state.transformedText.length
                    for (j in linkText.indices) {
                        if (i + 1 + j < state.mappingToTransformed.size) {
                            state.mappingToTransformed[i + 1 + j] = state.transformedText.length
                        }
                        state.mappingToOriginal.add(i + 1 + j)
                        state.transformedText += linkText[j]
                    }

                    for (k in i + 1 + linkText.length until i + closeParen + 1) {
                        if (k < state.mappingToTransformed.size) {
                            state.mappingToTransformed[k] = state.transformedText.length
                        }
                    }

                    state.styles.add(style to (startT..state.transformedText.length))
                    i += closeParen + 1
                    continue
                }
            }

            // Italic *...* (recursive content)
            if (remaining.startsWith("*")) {
                val end = remaining.indexOf("*", 1)
                if (end != -1 && remaining.getOrNull(1) != '*') {
                    val style = SpanStyle(fontStyle = FontStyle.Italic)

                    state.mappingToTransformed[i] = state.transformedText.length

                    val styleStart = state.transformedText.length
                    processInlineContent(state, i + 1, i + end)
                    state.styles.add(style to (styleStart..state.transformedText.length))

                    state.mappingToTransformed[i + end] = state.transformedText.length

                    i += end + 1
                    continue
                }
            }

            // Normal char
            if (i < state.mappingToTransformed.size) {
                state.mappingToTransformed[i] = state.transformedText.length
            }
            state.mappingToOriginal.add(i)
            state.transformedText += ot[i]
            i++
        }
        return i
    }
}
