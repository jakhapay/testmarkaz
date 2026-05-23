package uz.testmarkaz.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import uz.testmarkaz.data.db.dao.QuestionDao
import uz.testmarkaz.data.db.entity.QuestionEntity
import uz.testmarkaz.domain.model.Subject
import uz.testmarkaz.domain.model.TestConfig

class GenerateTestUseCaseTest {

    private lateinit var questionDao: QuestionDao
    private lateinit var useCase: GenerateTestUseCase

    @Before
    fun setUp() {
        questionDao = mockk()
        useCase = GenerateTestUseCase(questionDao)
    }

    @Test
    fun `returns null when fewer than 25 questions available`() = runTest {
        coEvery { questionDao.countBySubjectGrade(any(), any()) } returns 10
        coEvery { questionDao.randomBySubjectGrade(any(), any()) } returns fakeQuestions(10)

        val config = TestConfig.singleSubject(Subject.MATEMATIKA, 9)
        val result = useCase.invoke(config)

        assertNull("Should return null for insufficient questions", result)
    }

    @Test
    fun `returns session with exactly 25 questions`() = runTest {
        coEvery { questionDao.countBySubjectGrade(any(), any()) } returns 25
        coEvery { questionDao.randomBySubjectGrade(any(), any()) } returns fakeQuestions(25)

        val config = TestConfig.singleSubject(Subject.MATEMATIKA, 9)
        val result = useCase.invoke(config)

        assertNotNull(result)
        assertEquals(25, result!!.questions.size)
    }

    @Test
    fun `uses stratified sampling when pool is large enough`() = runTest {
        coEvery { questionDao.countBySubjectGrade(any(), any()) } returns 100
        coEvery { questionDao.countBySubjectRange(any(), any(), any()) } returns 100
        coEvery { questionDao.randomByDifficulty(any(), any(), any(), eq(1), any()) } returns fakeQuestions(10, difficulty = 1)
        coEvery { questionDao.randomByDifficulty(any(), any(), any(), eq(2), any()) } returns fakeQuestions(10, difficulty = 2)
        coEvery { questionDao.randomByDifficulty(any(), any(), any(), eq(3), any()) } returns fakeQuestions(5, difficulty = 3)

        val config = TestConfig.singleSubject(Subject.MATEMATIKA, 9)
        val result = useCase.invoke(config)

        assertNotNull(result)
        assertEquals(25, result!!.questions.size)
    }

    @Test
    fun `full random mode generates 25 questions`() = runTest {
        coEvery { questionDao.randomAll() } returns fakeQuestions(25)

        val config = TestConfig.fullRandom()
        val result = useCase.invoke(config)

        assertNotNull(result)
        assertEquals(25, result!!.questions.size)
    }

    @Test
    fun `session has unique session id`() = runTest {
        coEvery { questionDao.countBySubjectGrade(any(), any()) } returns 25
        coEvery { questionDao.randomBySubjectGrade(any(), any()) } returns fakeQuestions(25)

        val config = TestConfig.singleSubject(Subject.MATEMATIKA, 9)
        val s1 = useCase.invoke(config)
        val s2 = useCase.invoke(config)

        assertNotNull(s1)
        assertNotNull(s2)
        assertNotEquals(s1!!.sessionId, s2!!.sessionId)
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun fakeQuestions(count: Int, difficulty: Int = 2) = (1..count).map { i ->
        QuestionEntity(
            id = i.toLong(),
            subject = "matematika",
            grade = 9,
            topic = "Test mavzu",
            questionText = "Savol $i?",
            optionA = "A javob", optionB = "B javob", optionC = "C javob", optionD = "D javob",
            correct = "A",
            explanation = "Izoh $i",
            difficulty = difficulty,
            checksum = "checksum_$i",
            packKey = "mock"
        )
    }
}
