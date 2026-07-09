package com.oussama_chatri.productivityx.features.notes.presentation.util

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.RectF

class PdfBlockRenderer(
    private val engine: PdfEngine,
    private val inlineRenderer: PdfInlineRenderer
) {
    private val lineSpacing = 6f
    private val paragraphSpacing = 8f

    fun renderHeading(heading: PdfBlock.Heading) {
        val textSize = when (heading.level) {
            1 -> 24f
            2 -> 20f
            3 -> 17f
            4 -> 15f
            5 -> 14f
            else -> 13f
        }
        val spaceBefore = when (heading.level) {
            1 -> 8f
            2 -> 6f
            else -> 4f
        }

        engine.ensureSpace(textSize + lineSpacing + spaceBefore)
        engine.advance(spaceBefore)

        val paint = Paint().apply {
            this.textSize = textSize
            color = Color.BLACK
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val segments = inlineRenderer.parseInline(heading.text)
        val lines = inlineRenderer.wordWrapSegments(segments, paint, engine.contentWidth)

        for (line in lines) {
            engine.ensureSpace(textSize + lineSpacing)
            var x = engine.metrics.margin
            for ((idx, seg) in line.segments.withIndex()) {
                val p = Paint(paint)
                inlineRenderer.applyStyles(p, seg.styles)
                canvas.drawText(seg.text, x, engine.currentY, p)
                x += p.measureText(seg.text)
            }
            engine.advance(textSize + 2f)
        }

        engine.advance(2f)
    }

    fun renderParagraph(paragraph: PdfBlock.Paragraph) {
        val paint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
            isAntiAlias = true
        }

        val segments = inlineRenderer.parseInline(paragraph.text)
        if (segments.isEmpty()) {
            engine.advance(paragraphSpacing)
            return
        }

        val lines = inlineRenderer.wordWrapSegments(segments, paint, engine.contentWidth)
        if (lines.isEmpty()) {
            engine.advance(paragraphSpacing)
            return
        }

        for (line in lines) {
            engine.ensureSpace(paint.textSize + lineSpacing)
            var x = engine.metrics.margin
            for (seg in line.segments) {
                val p = Paint(paint)
                inlineRenderer.applyStyles(p, seg.styles)
                if (PdfInlineRenderer.Style.STRIKETHROUGH in seg.styles) {
                    val tw = p.measureText(seg.text)
                    val strikeY = engine.currentY - p.descent() + (p.ascent() + p.descent()) / 2f
                    canvas.drawText(seg.text, x, engine.currentY, p)
                    canvas.drawLine(x, strikeY, x + tw, strikeY, p)
                } else {
                    canvas.drawText(seg.text, x, engine.currentY, p)
                }
                x += p.measureText(seg.text)
            }
            engine.advance(paint.textSize + lineSpacing)
        }

        engine.advance(2f)
    }

    fun renderBulletItem(item: PdfBlock.BulletItem) {
        val paint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
            isAntiAlias = true
        }

        val indent = 12f + item.indent * 16f
        val bulletText = "•"
        val bulletWidth = paint.measureText(bulletText)
        val spaceAfterBullet = 8f
        val availableWidth = engine.contentWidth - indent - bulletWidth - spaceAfterBullet

        engine.ensureSpace(paint.textSize + lineSpacing)

        canvas.drawText(bulletText, engine.metrics.margin + indent, engine.currentY, paint)

        val segments = inlineRenderer.parseInline(item.text)
        if (segments.isNotEmpty()) {
            val linePaint = Paint(paint)
            val lines = inlineRenderer.wordWrapSegments(segments, linePaint, availableWidth)

            var x = engine.metrics.margin + indent + bulletWidth + spaceAfterBullet
            for (seg in lines.firstOrNull()?.segments ?: emptyList()) {
                val p = Paint(paint)
                inlineRenderer.applyStyles(p, seg.styles)
                canvas.drawText(seg.text, x, engine.currentY, p)
                x += p.measureText(seg.text)
            }
            engine.advance(paint.textSize + lineSpacing)

            // Continuation lines
            for (i in 1 until lines.size) {
                engine.ensureSpace(paint.textSize + lineSpacing)
                x = engine.metrics.margin + indent + bulletWidth + spaceAfterBullet
                for (seg in lines[i].segments) {
                    val p = Paint(paint)
                    inlineRenderer.applyStyles(p, seg.styles)
                    canvas.drawText(seg.text, x, engine.currentY, p)
                    x += p.measureText(seg.text)
                }
                engine.advance(paint.textSize + lineSpacing)
            }
        } else {
            engine.advance(paint.textSize + lineSpacing)
        }

        engine.advance(1f)
    }

    fun renderOrderedItem(item: PdfBlock.OrderedItem) {
        val paint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
            isAntiAlias = true
        }

        val indent = 12f + item.indent * 16f
        val numText = "${item.number}."
        val numWidth = paint.measureText(numText)
        val spaceAfterNum = 8f
        val availableWidth = engine.contentWidth - indent - numWidth - spaceAfterNum

        engine.ensureSpace(paint.textSize + lineSpacing)

        canvas.drawText(numText, engine.metrics.margin + indent, engine.currentY, paint)

        val segments = inlineRenderer.parseInline(item.text)
        if (segments.isNotEmpty()) {
            val linePaint = Paint(paint)
            val lines = inlineRenderer.wordWrapSegments(segments, linePaint, availableWidth)

            var x = engine.metrics.margin + indent + numWidth + spaceAfterNum
            for (seg in lines.firstOrNull()?.segments ?: emptyList()) {
                val p = Paint(paint)
                inlineRenderer.applyStyles(p, seg.styles)
                canvas.drawText(seg.text, x, engine.currentY, p)
                x += p.measureText(seg.text)
            }
            engine.advance(paint.textSize + lineSpacing)

            for (i in 1 until lines.size) {
                engine.ensureSpace(paint.textSize + lineSpacing)
                x = engine.metrics.margin + indent + numWidth + spaceAfterNum
                for (seg in lines[i].segments) {
                    val p = Paint(paint)
                    inlineRenderer.applyStyles(p, seg.styles)
                    canvas.drawText(seg.text, x, engine.currentY, p)
                    x += p.measureText(seg.text)
                }
                engine.advance(paint.textSize + lineSpacing)
            }
        } else {
            engine.advance(paint.textSize + lineSpacing)
        }

        engine.advance(1f)
    }

    fun renderBlockquote(blockquote: PdfBlock.Blockquote) {
        val textSize = 13f
        val paint = Paint().apply {
            this.textSize = textSize
            color = Color.GRAY
            isAntiAlias = true
            typeface = Typeface.defaultFromStyle(Typeface.ITALIC)
        }

        val barPaint = Paint().apply {
            color = Color.argb(80, 0, 0, 0)
            strokeWidth = 4f
        }

        val indent = 16f
        val barX = engine.metrics.margin
        val textX = engine.metrics.margin + indent + 8f
        val availableWidth = engine.contentWidth - indent - 8f

        engine.ensureSpace(textSize + lineSpacing + 4f)
        engine.advance(4f)

        val barTop = engine.currentY - paint.textSize + 2f
        val barBottom = engine.currentY + 4f
        canvas.drawLine(barX, barTop, barX, barBottom, barPaint)

        val segments = inlineRenderer.parseInline(blockquote.text)
        if (segments.isNotEmpty()) {
            val lines = inlineRenderer.wordWrapSegments(segments, paint, availableWidth)
            for (line in lines) {
                engine.ensureSpace(textSize + lineSpacing)
                var x = textX
                for (seg in line.segments) {
                    val p = Paint(paint)
                    inlineRenderer.applyStyles(p, seg.styles)
                    canvas.drawText(seg.text, x, engine.currentY, p)
                    x += p.measureText(seg.text)
                }
                engine.advance(textSize + lineSpacing)
            }
        } else {
            engine.advance(textSize + lineSpacing)
        }

        engine.advance(4f)
    }

    fun renderCodeBlock(codeBlock: PdfBlock.CodeBlock) {
        val textSize = 11f
        val codePaint = Paint().apply {
            this.textSize = textSize
            color = Color.DKGRAY
            isAntiAlias = true
            typeface = Typeface.MONOSPACE
        }

        val bgPaint = Paint().apply {
            color = Color.argb(24, 0, 0, 0)
        }

        val lineH = textSize + 6f
        val padding = 8f
        val codeLines = codeBlock.code.split("\n")
        val totalHeight = codeLines.size * lineH + padding * 2

        engine.ensureSpace(totalHeight + 4f)
        engine.advance(4f)

        val y0 = engine.currentY - padding
        canvas.drawRect(
            RectF(
                engine.metrics.margin, y0,
                engine.metrics.pageWidth - engine.metrics.margin, y0 + totalHeight
            ),
            bgPaint
        )

        var cy = engine.currentY
        for ((lineIdx, line) in codeLines.withIndex()) {
            engine.ensureSpace(lineH)
            val displayLine = if (codePaint.measureText(line) > engine.contentWidth - padding * 2) {
                truncateText(line, engine.contentWidth - padding * 2, codePaint)
            } else line
            canvas.drawText(displayLine, engine.metrics.margin + padding, cy, codePaint)
            cy += lineH
            engine.advance(lineH)
        }

        engine.advance(4f)
    }

    fun renderImageBitmap(
        loaded: PdfImageHandler.LoadedImage,
        caption: String?
    ) {
        val maxWidth = engine.contentWidth
        val maxHeight = 280f
        val displayWidth: Float
        val displayHeight: Float

        if (loaded.aspectRatio > maxWidth / maxHeight) {
            displayWidth = maxWidth
            displayHeight = maxWidth / loaded.aspectRatio
        } else {
            displayHeight = minOf(maxHeight, loaded.heightPx.toFloat())
            displayWidth = displayHeight * loaded.aspectRatio
        }

        engine.ensureSpace(displayHeight + 12f + (if (caption != null) 16f else 0f))
        engine.advance(4f)

        val x = if (displayWidth < maxWidth) {
            engine.metrics.margin + (maxWidth - displayWidth) / 2f
        } else engine.metrics.margin

        val bitmap = loaded.bitmap
        val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
        val dstRect = RectF(x, engine.currentY, x + displayWidth, engine.currentY + displayHeight)
        canvas.drawBitmap(bitmap, srcRect, dstRect, null)

        engine.advance(displayHeight + 4f)

        if (caption != null) {
            val capPaint = Paint().apply {
                textSize = 11f
                color = Color.GRAY
                isAntiAlias = true
            }
            val capWidth = capPaint.measureText(caption)
            val capX = engine.metrics.margin + (maxWidth - capWidth) / 2f
            canvas.drawText(caption, capX, engine.currentY, capPaint)
            engine.advance(14f)
        }
    }

    fun renderImageGallery(
        imageUrls: List<String>,
        imageHandler: PdfImageHandler
    ) {
        engine.ensureSpace(4f)
        engine.advance(4f)

        val labelPaint = Paint().apply {
            textSize = 16f
            color = Color.DKGRAY
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("Attachments", engine.metrics.margin, engine.currentY, labelPaint)
        engine.advance(22f)

        val dividerPaint = Paint().apply {
            color = Color.argb(40, 0, 0, 0)
            strokeWidth = 1f
        }
        canvas.drawLine(engine.metrics.margin, engine.currentY, engine.metrics.pageWidth - engine.metrics.margin, engine.currentY, dividerPaint)
        engine.advance(12f)

        val maxWidth = engine.contentWidth
        val galleryWidth = maxWidth
        val imageSize = minOf((galleryWidth - 8f * 2) / 2f, 160f)
        var col = 0
        var rowStartY = engine.currentY

        for ((idx, uriString) in imageUrls.withIndex()) {
            val loaded = imageHandler.loadImage(uriString)
            if (loaded != null) {
                val imgH: Float
                val imgW: Float
                if (loaded.aspectRatio > 1f) {
                    imgW = imageSize
                    imgH = imageSize / loaded.aspectRatio
                } else {
                    imgH = imageSize
                    imgW = imageSize * loaded.aspectRatio
                }

                if (col == 0) {
                    rowStartY = engine.currentY
                    engine.ensureSpace(imageSize + 4f)
                }

                val x = engine.metrics.margin + col * (imageSize + 8f)
                val y = rowStartY
                val src = android.graphics.Rect(0, 0, loaded.bitmap.width, loaded.bitmap.height)
                val dst = RectF(x, y, x + imgW, y + imgH)
                canvas.drawBitmap(loaded.bitmap, src, dst, null)

                loaded.bitmap.recycle()
                col++

                if (col >= 2) {
                    engine.advance(imageSize + 8f)
                    col = 0
                }
            }
        }

        if (col > 0) {
            engine.advance(imageSize + 8f)
        }
    }

    private fun truncateText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var result = text
        while (result.isNotEmpty() && paint.measureText(result + "…") > maxWidth) {
            result = result.dropLast(1)
        }
        return "$result…"
    }

    private val canvas get() = engine.canvas
}
