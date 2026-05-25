package uz.testmarkaz.domain.model

/** A single question shown during a test session */
data class TestQuestion(
    val id: Long,
    val checksum: String,
    val subject: String,
    val grade: Int,
    val topic: String,
    val questionText: String,
    val options: Map<String, String>,   // "A" -> text, "B" -> text, ...
    val correct: String,
    val explanation: String,
    val difficulty: Int
)

/** A live test session (holds the 25 questions + user answers) */
data class TestSession(
    val sessionId: String,
    val config: TestConfig,
    val questions: List<TestQuestion>,
    val answers: MutableMap<Long, String> = mutableMapOf(),  // questionId -> chosen option
    val startedAt: Long = System.currentTimeMillis()
) {
    val totalQuestions: Int get() = questions.size
    val answeredCount: Int get() = answers.size
    val isComplete: Boolean get() = answeredCount == totalQuestions
    val currentIndex: Int get() = answeredCount.coerceAtMost(totalQuestions - 1)
}

/** Immutable summary produced once a session is completed */
data class TestResult(
    val sessionId: String,
    val config: TestConfig,
    val score: Int,
    val total: Int,
    val durationSeconds: Int,
    val answers: List<AnswerDetail>,
    val completedAt: Long = System.currentTimeMillis()
) {
    val percentage: Int get() = if (total > 0) (score * 100) / total else 0
    val passed: Boolean get() = percentage >= 60
}

data class AnswerDetail(
    val question: TestQuestion,
    val selectedOption: String,
    val isCorrect: Boolean
)
