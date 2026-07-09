package com.oussama_chatri.productivityx.features.notes.presentation.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfGenerator {

    suspend fun generatePdf(
        context: Context,
        uri: Uri,
        title: String,
        content: String,
        imageUrls: List<String> = emptyList()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val margin = 50f
            val engine = PdfEngine(doc, pageInfo, margin)

            engine.addTitle(title.ifBlank { "Untitled Note" })
            engine.addHorizontalRule(8f)

            val blocks = PdfMarkdownBlockParser.parse(content)

            val inlineRenderer = PdfInlineRenderer()
            val blockRenderer = PdfBlockRenderer(engine, inlineRenderer)
            val imageHandler = PdfImageHandler(context)
            val tableRenderer = PdfTableRenderer(engine, inlineRenderer)

            for (block in blocks) {
                when (block) {
                    is PdfBlock.Heading -> blockRenderer.renderHeading(block)
                    is PdfBlock.Paragraph -> blockRenderer.renderParagraph(block)
                    is PdfBlock.BulletItem -> blockRenderer.renderBulletItem(block)
                    is PdfBlock.OrderedItem -> blockRenderer.renderOrderedItem(block)
                    is PdfBlock.Blockquote -> blockRenderer.renderBlockquote(block)
                    is PdfBlock.CodeBlock -> blockRenderer.renderCodeBlock(block)
                    is PdfBlock.Divider -> engine.addHorizontalRule(4f)
                    is PdfBlock.Table -> tableRenderer.renderTable(block)
                    is PdfBlock.Image -> {
                        val bitmap = imageHandler.loadImage(block.uri)
                        if (bitmap != null) {
                            blockRenderer.renderImageBitmap(bitmap, block.caption)
                        }
                    }
                }
            }

            if (imageUrls.isNotEmpty()) {
                engine.addVerticalSpace(16f)
                blockRenderer.renderImageGallery(imageUrls, imageHandler)
            }

            engine.addFooter()
            engine.finish()

            context.contentResolver.openOutputStream(uri)?.use { os -> doc.writeTo(os) }
                ?: throw IllegalStateException("Could not open output stream")
            doc.close()
        }
    }
}
