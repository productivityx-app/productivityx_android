package com.oussama_chatri.productivityx.features.notes.presentation.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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
