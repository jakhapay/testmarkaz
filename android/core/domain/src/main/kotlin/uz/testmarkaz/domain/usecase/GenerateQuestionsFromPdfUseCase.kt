package uz.testmarkaz.domain.usecase

import android.net.Uri
import kotlinx.coroutines.CancellationException
import uz.testmarkaz.data.db.dao.ProgressDao
import uz.testmarkaz.data.db.dao.QuestionDao
import uz.testmarkaz.data.db.entity.InstalledPackEntity
import uz.testmarkaz.data.db.entity.QuestionEntity
import uz.testmarkaz.domain.pdf.GeneratedQuestion
import uz.testmarkaz.domain.pdf.PdfTextExtractor
import uz.testmarkaz.domain.pdf.QuestionGenerator
import uz.testmarkaz.domain.pdf.QuestionValidator
import uz.testmarkaz.domain.pdf.TemplateQuestionGenerator
import uz.testmarkaz.domain.pdf.TextChunker
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

/**
 * End-to-end on-device pipeline (docs/11):
 *   PDF -> extract -> chunk -> generate -> validate + ground -> dedupe -> save pack.
 *
 * There is no fixed question count: it keeps every valid question the PDF yields
 * ([perChunk] caps per chunk; [maxTotal] = 0 means unlimited). The result is a new
 * `pdf_*` pack written into the shared `questions` table — the existing TestEngine
 * reads it unchanged.
 */
class GenerateQuestionsFromPdfUseCase @Inject constructor(
    private val extractor: PdfTextExtractor,
    private val chunker: TextChunker,
    private val generator: QuestionGenerator,           // LLM or template (DI-bound)
    private val fallback: TemplateQuestionGenerator,    // always-available safety net
    private val validator: QuestionValidator,
    private val questionDao: QuestionDao,
    private val progressDao: ProgressDao
) {
    enum class Stage { EXTRACTING, CHUNKING, GENERATING, VALIDATING, SAVING, DONE }

    data class Result(val packKey: String, val title: String, val questionCount: Int)

    sealed interface Outcome {
        data class Success(val result: Result) : Outcome
        data class Failure(val reason: String) : Outcome
    }

    suspend fun invoke(
        uri: Uri,
        title: String,
        perChunk: Int = 8,
        maxTotal: Int = 0,
        onStage: (Stage) -> Unit = {}
    ): Outcome {
        try {
            onStage(Stage.EXTRACTING)
            val pages = extractor.extract(uri)
            if (!extractor.hasTextLayer(pages)) {
                return Outcome.Failure(
                    "PDF da matn topilmadi (skanerlangan bo'lishi mumkin). " +
                        "OCR hozircha qo'llab-quvvatlanmaydi."
                )
            }

            onStage(Stage.CHUNKING)
            val chunks = chunker.chunk(pages)
            if (chunks.isEmpty()) return Outcome.Failure("Matn yetarli emas.")

            onStage(Stage.GENERATING)
            val accepted = mutableListOf<GeneratedQuestion>()
            for (chunk in chunks) {
                var candidates = generator.generate(chunk, perChunk)
                if (candidates.isEmpty()) {
                    candidates = fallback.generate(chunk, perChunk)   // per-chunk safety net
                }
                for (q in candidates) {
                    val withPage = if (q.sourcePage == null) q.copy(sourcePage = chunk.page) else q
                    if (validator.isValid(withPage, chunk)) accepted.add(withPage)
                }
                if (maxTotal in 1..accepted.size) break
            }

            onStage(Stage.VALIDATING)
            var finalList = validator.dedupe(accepted)
            if (maxTotal > 0) finalList = finalList.take(maxTotal)
            if (finalList.isEmpty()) return Outcome.Failure("Savol hosil qilinmadi.")

            onStage(Stage.SAVING)
            val packKey = "pdf_" + UUID.randomUUID().toString().take(8)
            questionDao.insertAll(finalList.map { it.toEntity(packKey, title) })
            // Some inserts may be ignored as duplicates (unique checksum) — record the real count.
            val stored = questionDao.countByPackKey(packKey)
            progressDao.upsertInstalledPack(
                InstalledPackEntity(
                    packKey = packKey,
                    subjectCode = "pdf",
                    grade = null,
                    gradeMin = null,
                    gradeMax = null,
                    questionCount = stored
                )
            )

            onStage(Stage.DONE)
            return Outcome.Success(Result(packKey, title, stored))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Outcome.Failure("Xatolik: ${e.message ?: "noma'lum"}")
        }
    }

    private fun GeneratedQuestion.toEntity(packKey: String, book: String): QuestionEntity {
        val correctLetter = "ABCD"[correctIndex].toString()
        val checksum = sha256(question + "|" + options.joinToString("|") + "|" + correctLetter)
        return QuestionEntity(
            subject = "pdf",
            grade = 0,
            topic = topic.ifBlank { "PDF" },
            questionText = question,
            optionA = options[0],
            optionB = options[1],
            optionC = options[2],
            optionD = options[3],
            correct = correctLetter,
            difficulty = difficulty,
            sourceBook = book,
            checksum = checksum,
            packKey = packKey
        )
    }

    private fun sha256(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
