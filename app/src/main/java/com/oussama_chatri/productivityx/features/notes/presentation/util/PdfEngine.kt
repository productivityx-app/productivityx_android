package com.oussama_chatri.productivityx.features.notes.presentation.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument

class PdfEngine(
    private val document: PdfDocument,
    private val pageInfo: PdfDocument.PageInfo,
    private val margin: Float = 50f,
    private val headerHeight: Float = 30f,
    private val footerHeight: Float = 25f
) {
    private var page: PdfDocument.Page = document.startPage(pageInfo)
    var canvas: Canvas = page.canvas
        private set
    var currentY: Float = margin + headerHeight
        private set

    val contentWidth: Float get() = pageInfo.pageWidth - margin * 2
    val pageWidth: Float get() = pageInfo.pageWidth.toFloat()
    val pageHeight: Float get() = pageInfo.pageHeight.toFloat()
    val bottomLimit: Float get() = pageHeight - margin - footerHeight

    private var pageNumber = 1
    private var totalPages = 0

    data class PageMetrics(
        val margin: Float,
        val contentWidth: Float,
        val pageWidth: Float,
        val pageHeight: Float,
        val bottomLimit: Float,
        val currentY: Float
    )

    val metrics: PageMetrics get() = PageMetrics(
        margin = margin,
        contentWidth = contentWidth,
        pageWidth = pageWidth,
        pageHeight = pageHeight,
        bottomLimit = bottomLimit,
        currentY = currentY
    )

    fun ensureSpace(neededHeight: Float) {
        if (currentY + neededHeight > bottomLimit) {
            addFooter()
            document.finishPage(page)
            pageNumber++
            page = document.startPage(pageInfo)
            canvas = page.canvas
            currentY = margin + headerHeight
            addHeader()
        }
    }

    fun advance(dy: Float) {
        currentY += dy
    }

    fun setY(y: Float) {
        currentY = y
    }

    fun addTitle(title: String) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        ensureSpace(30f)
        canvas.drawText(title, margin, currentY, paint)
        currentY += 36f
    }

    fun addHorizontalRule(spaceBefore: Float, thickness: Float = 2f, color: Int = Color.LTGRAY) {
        ensureSpace(spaceBefore + 4f)
        currentY += spaceBefore
        val paint = Paint().apply {
            this.color = color
            strokeWidth = thickness
            isAntiAlias = true
        }
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 8f
    }

    fun addVerticalSpace(height: Float) {
        ensureSpace(height)
        currentY += height
    }

    fun addHeader() {
        val paint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.5f
        }
        val y = margin + 12f
        canvas.drawText("ProductivityX", margin, y, paint)
        canvas.drawText("${java.time.LocalDate.now()}", pageWidth - margin - paint.measureText("${java.time.LocalDate.now()}"), y, paint)
        canvas.drawLine(margin, y + 6f, pageWidth - margin, y + 6f, linePaint)
    }

    fun addFooter() {
        val paint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.5f
        }
        val y = pageHeight - margin - 4f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        val pageText = "Page $pageNumber"
        canvas.drawText(pageText, pageWidth / 2f - paint.measureText(pageText) / 2f, y + 14f, paint)
    }

    fun makePaint(
        textSize: Float = 14f,
        color: Int = Color.DKGRAY,
        bold: Boolean = false,
        italic: Boolean = false,
        monospace: Boolean = false
    ): Paint = Paint().apply {
        this.textSize = textSize
        this.color = color
        this.isAntiAlias = true
        typeface = when {
            monospace -> Typeface.MONOSPACE
            bold && italic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            bold -> Typeface.DEFAULT_BOLD
            italic -> Typeface.defaultFromStyle(Typeface.ITALIC)
            else -> Typeface.DEFAULT
        }
    }

    fun measureTextBlock(lines: List<String>, paint: Paint, maxW: Float = contentWidth): Float {
        var totalHeight = 0f
        for (line in lines) {
            val wrapped = wrapLine(line, paint, maxW)
            totalHeight += wrapped.size * (paint.textSize + 6f)
        }
        return totalHeight
    }

    fun wrapLine(text: String, paint: Paint, maxW: Float): List<String> {
        if (paint.measureText(text) <= maxW) return listOf(text)
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(test) > maxW && current.isNotEmpty()) {
                lines.add(current.toString())
                current = StringBuilder(word)
            } else {
                if (current.isNotEmpty()) current.append(" ")
                current.append(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    fun drawTextBlock(
        text: String,
        paint: Paint,
        maxW: Float = contentWidth,
        lineSpacing: Float = 6f
    ) {
        val segments = text.split("\n")
        for (segment in segments) {
            val wrapped = wrapLine(segment, paint, maxW)
            for (line in wrapped) {
                ensureSpace(paint.textSize + lineSpacing)
                canvas.drawText(line, margin, currentY, paint)
                currentY += paint.textSize + lineSpacing
            }
        }
    }

    fun finish() {
        addFooter()
        document.finishPage(page)
    }
}
