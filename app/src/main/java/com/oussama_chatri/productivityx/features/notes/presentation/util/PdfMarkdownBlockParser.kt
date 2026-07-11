package com.oussama_chatri.productivityx.features.notes.presentation.util

sealed class PdfBlock {
    data class Heading(val level: Int, val text: String) : PdfBlock()
    data class Paragraph(val text: String) : PdfBlock()
    data class BulletItem(val text: String, val indent: Int = 0) : PdfBlock()
    data class TaskItem(val checked: Boolean, val text: String, val indent: Int = 0) : PdfBlock()
    data class OrderedItem(val number: Int, val text: String, val indent: Int = 0) : PdfBlock()
    data class Blockquote(val text: String) : PdfBlock()
    data class CodeBlock(val code: String, val language: String = "") : PdfBlock()
    data object Divider : PdfBlock()
    data class Image(val uri: String, val caption: String? = null) : PdfBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>, val alignments: List<Char> = emptyList()) : PdfBlock()
}

object PdfMarkdownBlockParser {

    fun parse(content: String): List<PdfBlock> {
        val blocks = mutableListOf<PdfBlock>()
        val lines = content.split("\n")
        var i = 0

        while (i < lines.size) {
            val rawLine = lines[i]
            val trimmed = rawLine.trimStart()

            when {
                trimmed.startsWith("```") -> {
                    val fence = trimmed
                    val lang = fence.removePrefix("```").trim()
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    i++
                    blocks.add(PdfBlock.CodeBlock(codeLines.joinToString("\n"), lang))
                }

                trimmed.matches(Regex("^#{1,6}\\s.*")) -> {
                    val level = trimmed.takeWhile { it == '#' }.length
                    val text = trimmed.drop(level).trim()
                    if (text.isNotEmpty()) {
                        blocks.add(PdfBlock.Heading(level, text))
                    }
                    i++
                }

                trimmed.startsWith("> ") -> {
                    val quoteLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trimStart().startsWith("> ")) {
                        quoteLines.add(lines[i].trimStart().removePrefix("> ").trim())
                        i++
                    }
                    blocks.add(PdfBlock.Blockquote(quoteLines.joinToString(" ")))
                }

                trimmed.startsWith("- [") -> {
                    val checked = trimmed.length > 4 && trimmed[3] == 'x'
                    val text = trimmed.substring(trimmed.indexOf(']') + 1).trim()
                    blocks.add(PdfBlock.TaskItem(checked, text))
                    i++
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val text = trimmed.removePrefix("- ").removePrefix("* ").trim()
                    blocks.add(PdfBlock.BulletItem(text))
                    i++
                }

                trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val dotIdx = trimmed.indexOf('.')
                    val num = trimmed.substring(0, dotIdx).toIntOrNull() ?: 1
                    val text = trimmed.substring(dotIdx + 1).trim()
                    blocks.add(PdfBlock.OrderedItem(num, text))
                    i++
                }

                trimmed.matches(Regex("^-{3,}$")) || trimmed.matches(Regex("^\\*{3,}$")) || trimmed.matches(Regex("^_{3,}$")) -> {
                    if (blocks.lastOrNull() !is PdfBlock.Divider) {
                        blocks.add(PdfBlock.Divider)
                    }
                    i++
                }

                trimmed.startsWith("|") && trimmed.endsWith("|") -> {
                    val tableLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trimStart().startsWith("|")) {
                        tableLines.add(lines[i].trimStart())
                        i++
                    }
                    val table = parseTable(tableLines)
                    if (table != null) blocks.add(table)
                }

                trimmed.startsWith("![" ) -> {
                    val altEnd = trimmed.indexOf(']')
                    if (altEnd != -1 && altEnd + 1 < trimmed.length && trimmed[altEnd + 1] == '(') {
                        val urlEnd = trimmed.indexOf(')', altEnd + 1)
                        if (urlEnd != -1) {
                            val alt = trimmed.substring(2, altEnd)
                            val url = trimmed.substring(altEnd + 2, urlEnd)
                            val caption = alt.ifBlank { null }
                            blocks.add(PdfBlock.Image(url, caption))
                            i++
                            continue
                        }
                    }
                    blocks.add(PdfBlock.Paragraph(rawLine))
                    i++
                }

                trimmed.isBlank() -> {
                    i++
                }

                else -> {
                    val paraLines = mutableListOf(rawLine)
                    i++
                    while (i < lines.size) {
                        val next = lines[i].trimStart()
                        if (next.isBlank() || next.startsWith("#") || next.startsWith("```") ||
                            next.startsWith("> ") || next.startsWith("- ") || next.startsWith("* ") ||
                            next.matches(Regex("^\\d+\\.\\s.*")) || next.matches(Regex("^-{3,}$")) ||
                            next.matches(Regex("^\\*{3,}$")) || next.matches(Regex("^_{3,}$")) ||
                            (next.startsWith("|") && next.endsWith("|")) || next.startsWith("![")) {
                            break
                        }
                        paraLines.add(lines[i])
                        i++
                    }
                    blocks.add(PdfBlock.Paragraph(paraLines.joinToString("\n")))
                }
            }
        }

        return blocks
    }

    private fun parseTable(lines: List<String>): PdfBlock.Table? {
        if (lines.size < 2) return null

        val headerCells = splitTableRow(lines[0])
        if (headerCells.isEmpty()) return null

        val separatorCells = splitTableRow(lines[1])
        if (separatorCells.isEmpty()) return null

        val alignments = separatorCells.map { cell ->
            val t = cell.trim()
            when {
                t.startsWith(":") && t.endsWith(":") -> 'c'
                t.endsWith(":") -> 'r'
                t.startsWith(":") -> 'l'
                else -> 'l'
            }
        }

        val rows = mutableListOf<List<String>>()
        for (i in 2 until lines.size) {
            val cells = splitTableRow(lines[i])
            if (cells.isNotEmpty()) {
                rows.add(cells)
            }
        }

        return PdfBlock.Table(headerCells, rows, alignments)
    }

    private fun splitTableRow(line: String): List<String> {
        val s = line.trim().removeSurrounding("|")
        val cells = mutableListOf<String>()
        val current = StringBuilder()
        var inCell = true
        for (ch in s) {
            when {
                ch == '|' && inCell -> {
                    cells.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty() || cells.isNotEmpty()) {
            cells.add(current.toString().trim())
        }
        return cells
    }
}
