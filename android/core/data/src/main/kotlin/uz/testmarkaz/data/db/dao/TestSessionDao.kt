package uz.testmarkaz.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uz.testmarkaz.data.db.entity.SessionAnswerEntity
import uz.testmarkaz.data.db.entity.TestSessionEntity

@Dao
interface TestSessionDao {

    @Insert
    suspend fun insertSession(session: TestSessionEntity)

    @Insert
    suspend fun insertAnswers(answers: List<SessionAnswerEntity>)

    @Query("UPDATE test_sessions SET completedAt = :completedAt, score = :score, durationSeconds = :duration WHERE id = :sessionId")
    suspend fun completeSession(sessionId: String, completedAt: Long, score: Int, duration: Int)

    @Query("UPDATE test_sessions SET syncedToServer = 1 WHERE id = :sessionId")
    suspend fun markSynced(sessionId: String)

    @Query("SELECT * FROM test_sessions WHERE syncedToServer = 0 ORDER BY startedAt DESC")
    suspend fun getPendingSync(): List<TestSessionEntity>

    @Query("SELECT * FROM test_sessions ORDER BY startedAt DESC")
    fun observeAllSessions(): Flow<List<TestSessionEntity>>

    @Query("SELECT * FROM test_sessions ORDER BY startedAt DESC LIMIT 10")
    fun observeRecentSessions(): Flow<List<TestSessionEntity>>

    @Query("SELECT * FROM session_answers WHERE sessionId = :sessionId ORDER BY id")
    suspend fun getAnswersForSession(sessionId: String): List<SessionAnswerEntity>

    @Query("SELECT COUNT(*) FROM test_sessions")
    suspend fun totalSessionCount(): Int

    @Query("SELECT SUM(score) FROM test_sessions")
    suspend fun totalCorrectAnswers(): Int?
}
