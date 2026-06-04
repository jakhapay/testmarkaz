package uz.testmarkaz.domain.pdf

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts the text layer from a PDF, page by page.
 *
 * OCR for scanned PDFs is out of scope for v1 (see docs/11 §1b): pages with no
 * text layer come back blank and are dropped during chunking. [hasTextLayer]
 * lets callers reject a fully-scanned document with a clear message.
 */
@Singleton
class PdfTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun extract(uri: Uri): List<PdfPage> {
        PDFBoxResourceLoader.init(context)
        val input = context.contentResolver.openInputStream(uri)
            ?: error("PDF oqimini ochib bo'lmadi")
        input.use { stream ->
            PDDocument.load(stream).use { doc ->
                val stripper = PDFTextStripper()
                val pages = ArrayList<PdfPage>(doc.numberOfPages)
                for (i in 1..doc.numberOfPages) {
                    stripper.startPage = i
                    stripper.endPage = i
                    pages.add(PdfPage(i, stripper.getText(doc).orEmpty()))
                }
                return pages
            }
        }
    }

    /** True if at least one page has a usable amount of extractable text. */
    fun hasTextLayer(pages: List<PdfPage>): Boolean =
        pages.any { it.text.trim().length >= 20 }
}
