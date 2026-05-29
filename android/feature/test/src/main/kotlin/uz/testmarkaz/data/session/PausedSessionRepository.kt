package uz.testmarkaz.data.session

import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import uz.testmarkaz.data.db.dao.PausedSessionDao
import uz.testmarkaz.data.db.dao.QuestionDao
import uz.testmarkaz.data.db.entity.PausedSessionEntity
import uz.testmarkaz.domain.model.Subject
import uz.testmarkaz.domain.model.TestConfig
import uz.testmarkaz.domain.model.TestMode
import uz.testmarkaz.domain.model.TestQuestion
import uz.testmarkaz.domain.model.TestSession
import javax.inject.Inject
import javax.inject.Singleton

data class RestoredSession(
    val session: TestSession,
    val currentIndex: Int
)

@Singleton
class PausedSessionRepository @Inject constructor(
    private val dao: PausedSessionDao,
    private val questionDao: QuestionDao
) {
    val hasPausedSession: Flow<Boolean> = dao.observeHasPaused()

    suspend fun save(session: TestSession, currentIndex: Int) {
        val answersJson = JSONObject()
            .apply { session.answers.forEach { (id, opt) -> put(id.toString(), opt) } }
            .toString()

        dao.save(
            PausedSessionEntity(
                sessionId    = session.sessionId,
                currentIndex = currentIndex,
                answersJson  = answersJson,
                questionIds  = session.questions.joinToString(",") { it.id.toString() },
                mode         = session.config.mode.name,
                subjectCode  = session.config.subject?.code,
                gradeMin     = session.config.gradeMin,
                gradeMax     = session.config.gradeMax,
                startedAt    = session.startedAt
            )
        )
    }

    suspend fun restore(): RestoredSession? {
        val entity = dao.getLatest() ?: return null
        val ids = entity.questionIds.split(",").mapNotNull { it.toLongOrNull() }
        if (ids.isEmpty()) return null

        val byId = questionDao.getByIds(ids).associateBy { it.id }
        val questions = ids.mapNotNull { id ->
            byId[id]?.let { e ->
                TestQuestion(
                    id           = e.id,
                    checksum     = e.checksum,
                    subject      = e.subject,
                    grade        = e.grade,
                    topic        = e.topic,
                    questionText = e.questionText,
                    options      = linkedMapOf("A" to e.optionA, "B" to e.optionB, "C" to e.optionC, "D" to e.optionD),
                    correct      = e.correct,
                    explanation  = e.explanation,
                    difficulty   = e.difficulty
                )
            }
        }
        if (questions.isEmpty()) return null

        val json = JSONObject(entity.answersJson)
        val answers = mutableMapOf<Long, String>()
        json.keys().forEach { key -> key.toLongOrNull()?.let { answers[it] = json.getString(key) } }

        return RestoredSession(
            session = TestSession(
                sessionId = entity.sessionId,
                config    = TestConfig(
                    mode     = TestMode.valueOf(entity.mode),
                    subject  = entity.subjectCode?.let { Subject.fromCode(it) },
                    gradeMin = entity.gradeMin,
                    gradeMax = entity.gradeMax
                ),
                questions = questions,
                answers   = answers,
                startedAt = entity.startedAt
            ),
            currentIndex = entity.currentIndex
        )
    }

    suspend fun clear(sessionId: String) = dao.delete(sessionId)
}
