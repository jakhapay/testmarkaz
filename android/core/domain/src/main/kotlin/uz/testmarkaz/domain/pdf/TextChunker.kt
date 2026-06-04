package uz.testmarkaz.domain.pdf

import javax.inject.Inject

/**
 * Cleans extracted page text and groups it into topic-sized chunks on paragraph
 * boundaries. Each chunk is small enough to fit a small model's context window
 * and focused enough that a generated question has a clear source passage.
 * Ported from tools/pdf2test (the Phase 0 prototype).
 */
class TextChunker @Inject constructor() {

    fun chunk(pages: List<PdfPage>, targetChars: Int = 1800): List<TextChunk> {
        val chunks = mutableListOf<TextChunk>()
        var buf = StringBuilder()
        var bufPage = -1
        for (page in pages) {
            val text = clean(page.text)
            if (text.isBlank()) continue
            for (para in text.split("\n")) {
                if (bufPage == -1) bufPage = page.number
                if (buf.length + para.length + 1 > targetChars && buf.isNotBlank()) {
                    chunks.add(TextChunk(bufPage, buf.toString().trim()))
                    buf = StringBuilder(para)
                    bufPage = page.number
                } else {
                    buf.append('\n').append(para)
                }
            }
        }
        if (buf.isNotBlank()) {
            chunks.add(TextChunk(if (bufPage == -1) 1 else bufPage, buf.toString().trim()))
        }
        return chunks.filter { it.text.length > 200 }
    }

    fun clean(text: String): String {
        var t = text.replace(Regex("-\\n(\\w)"), "$1")     // de-hyphenate across line breaks
        t = t.replace(Regex("[ \\t]+"), " ")
        t = t.replace(Regex("\\n{2,}"), "\n")
        return t.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !Regex("\\d{1,4}").matches(it) }
            .joinToString("\n")
    }
}
