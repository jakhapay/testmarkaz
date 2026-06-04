package uz.testmarkaz.domain.pdf

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the pure (Android-independent) parts of the PDF -> test pipeline:
 * chunking, validation, and the template generator. Mirrors tools/pdf2test.
 */
class PdfPipelineTest {

    private val sampleUz = """
        Nyutonning birinchi qonuni inertsiya qonuni deb ataladi. Bu qonunga ko'ra,
        agar jismga ta'sir etayotgan kuchlar muvozanatlashgan bo'lsa, jism tinch
        holatda qoladi. Jismning o'z holatini saqlashga intilish xususiyati
        inertsiya deb ataladi. Nyutonning ikkinchi qonuni kuch, massa va tezlanish
        orasidagi bog'lanishni ifodalaydi. Ishqalanish kuchi jismlarning siljishiga
        to'sqinlik qiladigan qarshilik kuchidir.
    """.trimIndent()

    @Test
    fun `chunker keeps source page and drops tiny chunks`() {
        val pages = listOf(PdfPage(1, sampleUz), PdfPage(2, "qisqa"))
        val chunks = TextChunker().chunk(pages)
        assertTrue("expected at least one usable chunk", chunks.isNotEmpty())
        assertEquals(1, chunks.first().page)
        assertTrue(chunks.all { it.text.length > 200 || it == chunks.first() })
    }

    @Test
    fun `template generator produces structurally valid grounded questions`() = runTest {
        val chunk = TextChunk(1, sampleUz)
        val validator = QuestionValidator()
        val questions = TemplateQuestionGenerator().generate(chunk, max = 10)

        assertTrue("expected some questions", questions.isNotEmpty())
        questions.forEach { q ->
            assertEquals(4, q.options.size)
            assertEquals(4, q.options.map { it.lowercase() }.toSet().size)   // distinct
            assertTrue(q.correctIndex in 0..3)
            assertTrue("must pass validation", validator.isValid(q, chunk))
            // the blanked answer is grounded in the source text
            assertTrue(chunk.text.contains(q.options[q.correctIndex]))
        }
    }

    @Test
    fun `validator rejects malformed and ungrounded questions`() {
        val chunk = TextChunk(1, sampleUz)
        val validator = QuestionValidator()

        val tooFewOptions = GeneratedQuestion("Savol?", listOf("a", "b", "c"), 0)
        assertFalse(validator.isValid(tooFewOptions, chunk))

        val duplicateOptions = GeneratedQuestion(
            "Savol matni uzun?", listOf("inertsiya", "inertsiya", "massa", "kuch"), 0
        )
        assertFalse(validator.isValid(duplicateOptions, chunk))

        val ungrounded = GeneratedQuestion(
            "Savol matni uzun?", listOf("fotosintez", "mitoxondriya", "xloroplast", "ribosoma"), 0
        )
        assertFalse(validator.isValid(ungrounded, chunk))
    }

    @Test
    fun `dedupe removes near-duplicate questions`() {
        val validator = QuestionValidator()
        val q1 = GeneratedQuestion("Bir xil savol matni", listOf("a", "b", "c", "d"), 0)
        val q2 = GeneratedQuestion("Bir xil savol matni", listOf("a", "b", "c", "d"), 0)
        assertEquals(1, validator.dedupe(listOf(q1, q2)).size)
    }
}
