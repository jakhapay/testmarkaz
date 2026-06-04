package uz.testmarkaz.domain.pdf

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Deterministic fill-in-the-blank (cloze) generator. Lower variety but always
 * valid and fully offline — the safety net from docs/11. It picks a salient
 * keyword from a sentence, blanks it out, and uses other keywords as distractors.
 *
 * Used both as the default [QuestionGenerator] (until an LLM is wired) and as the
 * per-chunk fallback inside [uz.testmarkaz.domain.usecase.GenerateQuestionsFromPdfUseCase].
 * Ported from tools/pdf2test.
 */
@Singleton
class TemplateQuestionGenerator @Inject constructor() : QuestionGenerator {

    override suspend fun generate(chunk: TextChunk, max: Int): List<GeneratedQuestion> {
        val sentences = chunk.text
            .split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.length > 40 }
        val keywords = keywords(chunk.text)
        val out = mutableListOf<GeneratedQuestion>()
        for (s in sentences) {
            val answer = keywords.firstOrNull { it in s } ?: continue
            val distractors = keywords.filter { !it.equals(answer, ignoreCase = true) }.take(8)
            if (distractors.size < 3) continue
            val seed = s.hashCode()
            val opts = (listOf(answer) + distractors.shuffled(Random(seed)).take(3))
                .shuffled(Random(seed + 1))
            out.add(
                GeneratedQuestion(
                    question = s.replaceFirst(answer, "_____"),
                    options = opts,
                    correctIndex = opts.indexOf(answer),
                    topic = "(matndan)",
                    sourcePage = chunk.page,
                    origin = GeneratedQuestion.Origin.TEMPLATE
                )
            )
            if (out.size >= max) break
        }
        return out
    }

    private val stop = setOf(
        "va", "yoki", "ham", "bilan", "uchun", "bu", "shu", "ular", "biz", "siz",
        "men", "sen", "ushbu", "hamda", "lekin", "ammo", "balki", "agar", "chunki",
        "kabi", "singari", "edi", "emas", "bor", "bir", "ikki", "har", "qaysi",
        "nima", "qanday", "qachon"
    )

    private fun keywords(text: String): List<String> {
        val seenLower = HashSet<String>()
        val result = mutableListOf<String>()
        Regex("[\\p{L}'’‘]{5,}").findAll(text)
            .map { it.value }
            .filter { it.lowercase() !in stop }
            .sortedByDescending { it.length }
            .forEach { if (seenLower.add(it.lowercase())) result.add(it) }
        return result
    }
}
