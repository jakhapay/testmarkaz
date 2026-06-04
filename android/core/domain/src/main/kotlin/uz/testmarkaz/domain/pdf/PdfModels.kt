package uz.testmarkaz.domain.pdf

/** One page of text extracted from a PDF. */
data class PdfPage(val number: Int, val text: String)

/** A topic-sized slice of cleaned text, with its source page kept for provenance. */
data class TextChunk(val page: Int, val text: String)

/**
 * A question produced by a [QuestionGenerator], before validation and before it
 * becomes a [uz.testmarkaz.data.db.entity.QuestionEntity].
 */
data class GeneratedQuestion(
    val question: String,
    val options: List<String>,   // exactly 4
    val correctIndex: Int,       // 0..3
    val topic: String = "",
    val difficulty: Int = 2,     // 1=easy 2=medium 3=hard
    val sourcePage: Int? = null,
    val origin: Origin = Origin.LLM
) {
    enum class Origin { LLM, TEMPLATE }
}
