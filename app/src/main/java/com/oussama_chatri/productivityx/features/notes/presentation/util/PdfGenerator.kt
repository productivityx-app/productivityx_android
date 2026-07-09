package com.oussama_chatri.productivityx.features.notes.presentation.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfGenerator {
    suspend fun generatePdf(
        context: Context,
        uri: Uri,
        title: String,
        content: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val contentPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
                isAntiAlias = true
            }

            val margin = 50f
            var currentY = margin + 24f

            // Draw title
            canvas.drawText(title.ifBlank { "Untitled Note" }, margin, currentY, titlePaint)
            currentY += 40f

            // Draw content (very basic line wrapping)
            val maxWidth = pageInfo.pageWidth - (margin * 2)
            val lines = content.split("\n")

            for (line in lines) {
                if (line.isBlank()) {
                    currentY += 20f
                    continue
                }

                val words = line.split(" ")
                var currentLine = ""

                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    val measureWidth = contentPaint.measureText(testLine)

                    if (measureWidth > maxWidth) {
                        // Check if we need a new page
                        if (currentY + 20f > pageInfo.pageHeight - margin) {
                            document.finishPage(page)
                            page = document.startPage(pageInfo)
                            canvas = page.canvas
                            currentY = margin + 20f
                        }

                        canvas.drawText(currentLine, margin, currentY, contentPaint)
                        currentLine = word
                        currentY += 20f
                    } else {
                        currentLine = testLine
                    }
                }

                if (currentLine.isNotEmpty()) {
                    if (currentY + 20f > pageInfo.pageHeight - margin) {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = margin + 20f
                    }
                    canvas.drawText(currentLine, margin, currentY, contentPaint)
                    currentY += 20f
                }
            }

            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            } ?: throw IllegalStateException("Could not open output stream")

            document.close()
        }
    }
}
