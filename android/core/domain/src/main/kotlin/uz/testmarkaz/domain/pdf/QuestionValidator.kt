package uz.testmarkaz.domain.pdf

import javax.inject.Inject

/**
 * The safety net: every candidate question must pass these deterministic checks
 * before it can be stored. Catches malformed structure and ungrounded
 * (hallucinated) answers. Ported from tools/pdf2test.
 */
class QuestionValidator @Inject constructor() {

    fun isValid(q: GeneratedQuestion, chunk: TextChunk): Boolean {
        if (q.question.length < 8) return false
        val opts = q.options.filter { it.isNotBlank() }
        if (opts.size != 4) return false
        if (opts.map { it.lowercase() }.toSet().size != 4) return false   // 4 distinct
        if (q.correctIndex !in 0..3) return false
        // grounding: the correct answer's key word should appear in the source text
        val ans = q.options[q.correctIndex].lowercase()
        val key = ans.split(" ").maxByOrNull { it.length } ?: ans
        if (key.length >= 4 && !chunk.text.lowercase().contains(key)) return false
        return true
    }

    /** Removes near-duplicate questions by normalized prefix. */
    fun dedupe(questions: List<GeneratedQuestion>): List<GeneratedQuestion> {
        val seen = HashSet<String>()
        return questions.filter {
            val sig = it.question.lowercase().replace(Regex("\\W+"), "").take(60)
            seen.add(sig)
        }
    }
}
