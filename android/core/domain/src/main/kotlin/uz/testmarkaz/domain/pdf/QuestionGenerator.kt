package uz.testmarkaz.domain.pdf

/**
 * Produces candidate questions from a single text chunk.
 *
 * Implementations:
 *  - [TemplateQuestionGenerator] — deterministic, offline, always available (the
 *    safety net). Bound by default in [uz.testmarkaz.di.PdfModule].
 *  - [LlmQuestionGenerator] — on-device LLM (MediaPipe/Gemma); swap the DI binding
 *    to this once a model is shipped.
 *
 * Output is treated as UNVALIDATED — every candidate passes through
 * [QuestionValidator] before it can be stored.
 */
interface QuestionGenerator {
    /** @param max upper bound on questions to return for this chunk (not a total cap). */
    suspend fun generate(chunk: TextChunk, max: Int): List<GeneratedQuestion>
}
