package uz.testmarkaz.domain.usecase

import uz.testmarkaz.data.db.dao.QuestionDao
import uz.testmarkaz.data.db.entity.QuestionEntity
import uz.testmarkaz.domain.model.TestConfig
import uz.testmarkaz.domain.model.TestMode
import uz.testmarkaz.domain.model.TestQuestion
import uz.testmarkaz.domain.model.TestSession
import java.util.UUID
import javax.inject.Inject

class GenerateTestUseCase @Inject constructor(
    private val questionDao: QuestionDao
) {

    companion object {
        const val TEST_SIZE = 25
        const val MIN_POOL_FOR_STRATIFIED = 50
        const val EASY_COUNT = 10
        const val MEDIUM_COUNT = 10
        const val HARD_COUNT = 5
    }

    /**
     * Generates a 25-question test for the given [config].
     * Uses stratified difficulty sampling when the pool is large enough,
     * otherwise falls back to pure random.
     *
     * Returns null if fewer than [TEST_SIZE] questions are available.
     */
    suspend fun invoke(config: TestConfig): TestSession? {
        val questions = fetchQuestions(config)
        if (questions.size < TEST_SIZE) return null

        return TestSession(
            sessionId = UUID.randomUUID().toString(),
            config = config,
            questions = questions
        )
    }

    private suspend fun fetchQuestions(config: TestConfig): List<TestQuestion> {
        return when (config.mode) {
            TestMode.SUBJECT -> {
                val subject = config.subject?.code ?: return emptyList()
                val grade = config.gradeMin
                val pool = questionDao.countBySubjectGrade(subject, grade)
                if (pool >= MIN_POOL_FOR_STRATIFIED) {
                    stratified(subject, grade, grade)
                } else {
                    questionDao.randomBySubjectGrade(subject, grade).map { it.toDomain() }
                }
            }

            TestMode.RANGE -> {
                val subject = config.subject?.code ?: return emptyList()
                val pool = questionDao.countBySubjectRange(subject, config.gradeMin, config.gradeMax)
                if (pool >= MIN_POOL_FOR_STRATIFIED) {
                    stratified(subject, config.gradeMin, config.gradeMax)
                } else {
                    questionDao.randomBySubjectRange(subject, config.gradeMin, config.gradeMax)
                        .map { it.toDomain() }
                }
            }

            TestMode.RANDOM_CLASS -> {
                questionDao.randomByGrade(config.gradeMin).map { it.toDomain() }
            }

            TestMode.FULL_RANDOM -> {
                questionDao.randomAll().map { it.toDomain() }
            }
        }
    }

    private suspend fun stratified(subject: String, gradeMin: Int, gradeMax: Int): List<TestQuestion> {
        val easy   = questionDao.randomByDifficulty(subject, gradeMin, gradeMax, 1, EASY_COUNT)
        val medium = questionDao.randomByDifficulty(subject, gradeMin, gradeMax, 2, MEDIUM_COUNT)
        val hard   = questionDao.randomByDifficulty(subject, gradeMin, gradeMax, 3, HARD_COUNT)

        val combined = (easy + medium + hard).shuffled()
        return if (combined.size >= TEST_SIZE) {
            combined.take(TEST_SIZE).map { it.toDomain() }
        } else {
            // Fall back: not enough per-difficulty → pure random
            questionDao.randomBySubjectRange(subject, gradeMin, gradeMax).map { it.toDomain() }
        }
    }

    private fun QuestionEntity.toDomain() = TestQuestion(
        id = id,
        checksum = checksum,
        subject = subject,
        grade = grade,
        topic = topic,
        questionText = questionText,
        options = mapOf("A" to optionA, "B" to optionB, "C" to optionC, "D" to optionD),
        correct = correct,
        explanation = explanation,
        difficulty = difficulty
    )
}
