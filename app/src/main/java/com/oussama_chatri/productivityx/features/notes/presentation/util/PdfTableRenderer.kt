package com.oussama_chatri.productivityx.features.notes.presentation.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import kotlin.math.max
import kotlin.math.min

class PdfTableRenderer(
    private val engine: PdfEngine,
    private val inlineRenderer: PdfInlineRenderer
) {
    private val cellPadding = 6f
    private val lineHeight = 18f
    private val minColWidth = 40f
    private val maxColWidth = 180f

    fun renderTable(table: PdfBlock.Table) {
        val metrics = engine.metrics
        val totalWidth = metrics.contentWidth
        val numCols = max(table.headers.size, table.rows.maxOfOrNull { it.size } ?: 0)
        if (numCols == 0) return

        val colWidths = calculateColumnWidths(table, totalWidth, numCols)
        val alignments = table.alignments.let { a ->
            if (a.size >= numCols) a else a + List(numCols - a.size) { 'l' }
        }

        engine.ensureSpace(8f)
        engine.advance(4f)

        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 13f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        val cellPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            isAntiAlias = true
        }

        val gridPaint = Paint().apply {
            color = Color.argb(60, 0, 0, 0)
            strokeWidth = 0.75f
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.argb(24, 0, 0, 0)
        }

        val altRowPaint = Paint().apply {
            color = Color.argb(12, 0, 0, 0)
        }

        fun drawGridLine(y: Float) {
            engine.canvas.drawLine(metrics.margin, y, metrics.margin + totalWidth, y, gridPaint)
        }

        fun drawVerticalLines(rowY: Float, rowHeight: Float) {
            var x = metrics.margin
            for (w in colWidths) {
                engine.canvas.drawLine(x, rowY, x, rowY + rowHeight, gridPaint)
                x += w
            }
            engine.canvas.drawLine(x, rowY, x, rowY + rowHeight, gridPaint)
        }

        fun renderCellContent(cell: String, colW: Float, paint: Paint, y: Float, align: Char) {
            val segments = inlineRenderer.parseInline(cell)
            val availableW = colW - cellPadding * 2
            val lines = inlineRenderer.wordWrapSegments(segments, paint, availableW)
            for ((li, line) in lines.withIndex()) {
                val lineH = paint.textSize + 2f
                engine.ensureSpace(lineH)
                var x = metrics.margin + cellPadding
                for (seg in line.segments) {
                    val segW = inlineRenderer.measureSegment(seg, paint)
                    val drawX = when (align) {
                        'c' -> metrics.margin + cellPadding + (availableW - segW) / 2f
                        'r' -> metrics.margin + colW - cellPadding - segW
                        else -> x
                    }
                    inlineRenderer.drawSegment(engine.canvas, seg, drawX, engine.currentY, paint)
                    x += segW
                }
                if (li < lines.size - 1) engine.advance(lineH)
            }
        }

        fun measureCellHeight(cell: String, paint: Paint, colW: Float): Float {
            val segments = inlineRenderer.parseInline(cell)
            val availableW = colW - cellPadding * 2
            val lines = inlineRenderer.wordWrapSegments(segments, paint, availableW)
            val lineH = paint.textSize + 2f
            return max(lines.size * lineH + cellPadding * 2, lineHeight + cellPadding * 2)
        }

        // Header row
        val headerY = engine.currentY
        val headerHeight = lineHeight + cellPadding * 2

        engine.ensureSpace(headerHeight + 4f)
        engine.setY(headerY)
        val adjustedHeaderY = engine.currentY

        engine.canvas.drawRect(metrics.margin, adjustedHeaderY - cellPadding, metrics.margin + totalWidth, adjustedHeaderY - cellPadding + headerHeight, headerBgPaint)
        drawGridLine(adjustedHeaderY - cellPadding)
        drawGridLine(adjustedHeaderY - cellPadding + headerHeight)

        var cx = metrics.margin + cellPadding
        for ((colIdx, header) in table.headers.withIndex()) {
            val colW = colWidths.getOrElse(colIdx) { minColWidth }
            val segs = inlineRenderer.parseInline(header)
            val availableW = colW - cellPadding * 2
            val lines = inlineRenderer.wordWrapSegments(segs, headerPaint, availableW)
            var drawX = cx
            for (seg in lines.firstOrNull()?.segments ?: emptyList()) {
                inlineRenderer.drawSegment(engine.canvas, seg, drawX, adjustedHeaderY, headerPaint)
                drawX += inlineRenderer.measureSegment(seg, headerPaint)
            }
            cx += colW
        }

        engine.advance(headerHeight)

        drawVerticalLines(adjustedHeaderY - cellPadding, headerHeight)

        // Data rows
        for ((rowIdx, row) in table.rows.withIndex()) {
            val rowHeight = maxOf(
                row.mapIndexed { colIdx, cell ->
                    measureCellHeight(cell, cellPaint, colWidths.getOrElse(colIdx) { minColWidth })
                }.maxOrNull() ?: (lineHeight + cellPadding * 2),
                lineHeight + cellPadding * 2
            )
            engine.ensureSpace(rowHeight)
            val rowY = engine.currentY

            if (rowIdx % 2 == 1) {
                engine.canvas.drawRect(metrics.margin, rowY - cellPadding, metrics.margin + totalWidth, rowY - cellPadding + rowHeight, altRowPaint)
            }

            drawGridLine(rowY - cellPadding)
            drawGridLine(rowY - cellPadding + rowHeight)

            val rowStartY = engine.currentY
            for ((colIdx, cell) in row.withIndex()) {
                val colW = colWidths.getOrElse(colIdx) { minColWidth }
                engine.setY(rowStartY)
                renderCellContent(cell, colW, cellPaint, rowStartY, alignments.getOrElse(colIdx) { 'l' })
            }

            drawVerticalLines(rowY - cellPadding, rowHeight)

            engine.advance(rowHeight - (engine.currentY - rowY))
        }

        engine.advance(2f)
    }

    private fun calculateColumnWidths(table: PdfBlock.Table, totalWidth: Float, numCols: Int): List<Float> {
        val paint = Paint().apply { textSize = 12f; isAntiAlias = true }
        val headerPaint = Paint(Paint(paint)).apply { typeface = Typeface.DEFAULT_BOLD }

        val maxWidths = MutableList(numCols) { minColWidth }

        for ((colIdx, header) in table.headers.withIndex()) {
            val w = headerPaint.measureText(header) + cellPadding * 2
            maxWidths[colIdx] = max(maxWidths[colIdx], w)
        }

        for (row in table.rows) {
            for ((colIdx, cell) in row.withIndex()) {
                if (colIdx < numCols) {
                    val w = paint.measureText(cell) + cellPadding * 2
                    maxWidths[colIdx] = max(maxWidths[colIdx], w)
                }
            }
        }

        val totalNatural = maxWidths.sum()
        if (totalNatural <= totalWidth) {
            val remaining = totalWidth - totalNatural
            val extraPerCol = remaining / numCols
            return maxWidths.map { it + extraPerCol }
        }

        val clamped = maxWidths.map { min(it, maxColWidth) }
        val clampedSum = clamped.sum()
        if (clampedSum <= totalWidth) {
            val remaining = totalWidth - clampedSum
            val unclampedCount = maxWidths.indices.count { maxWidths[it] <= maxColWidth }
            val extraPerCol = if (unclampedCount > 0) remaining / unclampedCount else 0f
            return maxWidths.indices.map { idx ->
                if (maxWidths[idx] > maxColWidth) maxColWidth
                else min(maxWidths[idx] + extraPerCol, maxColWidth)
            }
        }

        val scale = totalWidth / clampedSum
        return clamped.map { it * scale }
    }

    private fun truncateText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var result = text
        while (result.isNotEmpty() && paint.measureText(result + "…") > maxWidth) {
            result = result.dropLast(1)
        }
        return "$result…"
    }
}
