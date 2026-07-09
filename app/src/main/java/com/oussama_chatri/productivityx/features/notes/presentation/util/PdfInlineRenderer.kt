package com.oussama_chatri.productivityx.features.notes.presentation.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import kotlin.math.min

class PdfInlineRenderer {

    enum class Style { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, CODE, LINK }

    data class StyledSegment(val text: String, val styles: Set<Style>, val url: String? = null)

    fun parseInline(text: String): List<StyledSegment> {
        val result = mutableListOf<StyledSegment>()
        val buf = StringBuilder()
        val active = mutableSetOf<Style>()
        var i = 0

        fun flush() {
            if (buf.isNotEmpty()) {
                result.add(StyledSegment(buf.toString(), active.toSet()))
                buf.clear()
            }
        }

        while (i < text.length) {
            when {
                i + 1 < text.length && text[i] == '*' && text[i + 1] == '*' -> {
                    flush()
                    if (Style.BOLD in active) active.remove(Style.BOLD) else active.add(Style.BOLD)
                    i += 2
                }
                text[i] == '*' -> {
                    flush()
                    if (Style.ITALIC in active) active.remove(Style.ITALIC) else active.add(Style.ITALIC)
                    i += 1
                }
                i + 1 < text.length && text[i] == '~' && text[i + 1] == '~' -> {
                    flush()
                    if (Style.STRIKETHROUGH in active) active.remove(Style.STRIKETHROUGH) else active.add(Style.STRIKETHROUGH)
                    i += 2
                }
                text[i] == '<' && text.regionMatches(i, "<u>", 0, 3) -> {
                    flush()
                    if (Style.UNDERLINE in active) active.remove(Style.UNDERLINE) else active.add(Style.UNDERLINE)
                    i += 3
                }
                text[i] == '<' && text.regionMatches(i, "</u>", 0, 4) -> {
                    flush()
                    active.remove(Style.UNDERLINE)
                    i += 4
                }
                text[i] == '`' -> {
                    flush()
                    if (Style.CODE in active) active.remove(Style.CODE) else active.add(Style.CODE)
                    i += 1
                }
                text[i] == '[' && text.length > i + 1 -> {
                    val cb = text.indexOf(']', i)
                    if (cb != -1 && cb + 1 < text.length && text[cb + 1] == '(') {
                        val cp = text.indexOf(')', cb + 1)
                        if (cp != -1) {
                            flush()
                            val linkText = text.substring(i + 1, cb)
                            val url = text.substring(cb + 2, cp)
                            result.add(StyledSegment(linkText, setOf(Style.LINK), url))
                            i = cp + 1
                            continue
                        }
                    }
                    buf.append(text[i])
                    i++
                }
                else -> {
                    buf.append(text[i])
                    i++
                }
            }
        }
        flush()
        return result
    }

    fun applyStyles(paint: Paint, styles: Set<Style>) {
        val isBold = Style.BOLD in styles
        val isItalic = Style.ITALIC in styles
        val isCode = Style.CODE in styles
        paint.typeface = when {
            isCode -> Typeface.MONOSPACE
            isBold && isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            isBold -> Typeface.DEFAULT_BOLD
            isItalic -> Typeface.defaultFromStyle(Typeface.ITALIC)
            else -> Typeface.DEFAULT
        }
        if (Style.UNDERLINE in styles) paint.isUnderlineText = true
        if (Style.LINK in styles) paint.color = Color.BLUE
        if (Style.CODE in styles) paint.color = Color.rgb(180, 30, 30)
    }

    fun measureSegment(segment: StyledSegment, paint: Paint): Float {
        val p = Paint(paint)
        applyStyles(p, segment.styles)
        return p.measureText(segment.text)
    }

    fun drawSegment(canvas: Canvas, segment: StyledSegment, x: Float, y: Float, paint: Paint) {
        val p = Paint(paint)
        applyStyles(p, segment.styles)
        canvas.drawText(segment.text, x, y, p)
        if (Style.STRIKETHROUGH in segment.styles) {
            val tw = p.measureText(segment.text)
            val strikeY = y - p.descent() + (p.ascent() + p.descent()) / 2f
            canvas.drawLine(x, strikeY, x + tw, strikeY, p)
        }
    }

    data class LineContent(
        val segments: List<StyledSegment>,
        val widths: List<Float>,
        val totalWidth: Float
    )

    fun wordWrapSegments(
        segments: List<StyledSegment>,
        paint: Paint,
        maxWidth: Float
    ): List<LineContent> {
        val lines = mutableListOf<LineContent>()
        val currentSegs = mutableListOf<StyledSegment>()
        val currentWidths = mutableListOf<Float>()
        var currentWidth = 0f

        fun flushLine() {
            if (currentSegs.isNotEmpty()) {
                lines.add(LineContent(currentSegs.toList(), currentWidths.toList(), currentWidth))
                currentSegs.clear()
                currentWidths.clear()
                currentWidth = 0f
            }
        }

        for (seg in segments) {
            val words = seg.text.split(" ")
            var firstWord = true
            for (word in words) {
                val displayWord = if (firstWord) word else " $word"
                val wordStyles = seg.styles
                val p = Paint(paint)
                applyStyles(p, wordStyles)
                val ww = p.measureText(displayWord)

                if (!currentSegs.isEmpty() && currentWidth + ww > maxWidth) {
                    flushLine()
                }

                val actualWord = if (currentSegs.isEmpty()) word else displayWord
                val actualWidth = p.measureText(actualWord)
                currentSegs.add(StyledSegment(actualWord, wordStyles, seg.url))
                currentWidths.add(actualWidth)
                currentWidth += actualWidth
                firstWord = false
            }
        }
        flushLine()
        return lines
    }
}
