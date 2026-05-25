package uz.testmarkaz.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import uz.testmarkaz.data.db.entity.InstalledPackEntity
import uz.testmarkaz.data.db.entity.TopicMasteryEntity
import uz.testmarkaz.data.db.entity.UserStatsEntity
import uz.testmarkaz.data.db.entity.WrongAnswerEntity

@Dao
interface ProgressDao {

    // ── Topic mastery ─────────────────────────────────────────────────────

    @Upsert
    suspend fun upsertMastery(mastery: TopicMasteryEntity)

    @Query("SELECT * FROM topic_mastery WHERE subjectCode = :subject AND grade = :grade ORDER BY eloRating ASC")
    fun observeMastery(subject: String, grade: Int): Flow<List<TopicMasteryEntity>>

    @Query("""
        SELECT * FROM topic_mastery
        WHERE totalAnswered >= 5
        ORDER BY (correctCount * 100 / totalAnswered) ASC
        LIMIT :limit
    """)
    fun observeWeakTopics(limit: Int = 5): Flow<List<TopicMasteryEntity>>

    @Query("SELECT * FROM topic_mastery ORDER BY eloRating DESC")
    fun observeAllMastery(): Flow<List<TopicMasteryEntity>>

    // ── User stats (streak, XP, totals) ──────────────────────────────────

    @Upsert
    suspend fun upsertStats(stats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun observeStats(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getStats(): UserStatsEntity?

    @Query("""
        UPDATE user_stats
        SET totalXp = totalXp + :xp, totalTests = totalTests + 1,
            totalCorrect = totalCorrect + :correct, lastTestDay = :day
        WHERE id = 1
    """)
    suspend fun addTestResult(xp: Int, correct: Int, day: Long)

    // ── Wrong answers ─────────────────────────────────────────────────────

    @Upsert
    suspend fun upsertWrongAnswer(wrong: WrongAnswerEntity)

    @Query("SELECT COUNT(*) FROM wrong_answers")
    suspend fun wrongAnswerCount(): Int

    @Query("SELECT * FROM wrong_answers ORDER BY wrongCount DESC LIMIT 50")
    fun observeWrongAnswers(): Flow<List<WrongAnswerEntity>>

    // ── Installed packs ───────────────────────────────────────────────────

    @Upsert
    suspend fun upsertInstalledPack(pack: InstalledPackEntity)

    @Query("SELECT * FROM installed_packs ORDER BY subjectCode, grade")
    fun observeInstalledPacks(): Flow<List<InstalledPackEntity>>

    @Query("SELECT * FROM installed_packs WHERE packKey = :packKey")
    suspend fun getInstalledPack(packKey: String): InstalledPackEntity?

    @Query("DELETE FROM installed_packs WHERE packKey = :packKey")
    suspend fun removePack(packKey: String)
}
