package uz.testmarkaz.domain.pdf

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * On-device LLM generator (MediaPipe LLM Inference + a Gemma/Qwen `.task` model).
 *
 * SCAFFOLD ONLY — not yet bound in DI. To enable on-device generation:
 *   1. Uncomment the MediaPipe dependency in app/build.gradle.kts:
 *        implementation("com.google.mediapipe:tasks-genai:0.10.14")
 *   2. Download a model (e.g. `gemma2-2b-it-int4.task`) into filesDir on first use
 *      — one-time download, fully offline afterwards (docs/11 §4).
 *   3. Implement [generate]: build the prompt with [PROMPT], run LlmInference,
 *      parse the JSON response into [GeneratedQuestion]s. Prefer JSON/grammar-
 *      constrained decoding so malformed output is impossible.
 *   4. In [uz.testmarkaz.di.PdfModule], bind QuestionGenerator to this class
 *      instead of TemplateQuestionGenerator.
 *
 * Until then [generate] returns empty, so the use case's per-chunk template
 * fallback still produces a complete test. This keeps the feature working with
 * zero model download.
 */
class LlmQuestionGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) : QuestionGenerator {

    override suspend fun generate(chunk: TextChunk, max: Int): List<GeneratedQuestion> {
        // TODO: run MediaPipe LlmInference with PROMPT.format(max, chunk.text),
        // then parse JSON {"questions":[...]} -> List<GeneratedQuestion>.
        return emptyList()
    }

    companion object {
        /** Uzbek prompt requesting strict JSON. Mirrors tools/pdf2test PROMPT_UZ. */
        const val PROMPT = """Sen tajribali o'qituvchisan. Quyidagi matn asosida %d ta test savoli tuz. Faqat shu matndagi ma'lumotdan foydalan, o'zingdan qo'shma.
Har bir savol uchun: aniq savol (o'zbek tilida), to'rtta variant (faqat bittasi to'g'ri), to'g'ri variant indeksi (0 dan 3 gacha), mavzu nomi.
Javobni FAQAT quyidagi JSON ko'rinishida ber, boshqa hech narsa yozma:
{"questions":[{"question":"...","options":["...","...","...","..."],"correct_index":0,"topic":"..."}]}
MATN:
%s"""
    }
}
