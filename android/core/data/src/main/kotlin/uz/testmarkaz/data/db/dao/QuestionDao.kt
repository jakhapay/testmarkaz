package uz.testmarkaz.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uz.testmarkaz.data.db.entity.QuestionEntity

@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    // ── Core test generation queries ──────────────────────────────────────

    /** Single subject + exact grade — 25 random */
    @Query("""
        SELECT * FROM questions
        WHERE subject = :subject AND grade = :grade
        ORDER BY RANDOM() LIMIT 25
    """)
    suspend fun randomBySubjectGrade(subject: String, grade: Int): List<QuestionEntity>

    /** Single subject + grade range — 25 random */
    @Query("""
        SELECT * FROM questions
        WHERE subject = :subject AND grade BETWEEN :gradeMin AND :gradeMax
        ORDER BY RANDOM() LIMIT 25
    """)
    suspend fun randomBySubjectRange(subject: String, gradeMin: Int, gradeMax: Int): List<QuestionEntity>

    /** All subjects, single grade — 25 random */
    @Query("""
        SELECT * FROM questions
        WHERE grade = :grade
        ORDER BY RANDOM() LIMIT 25
    """)
    suspend fun randomByGrade(grade: Int): List<QuestionEntity>

    /** Fully random from all downloaded questions */
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT 25")
    suspend fun randomAll(): List<QuestionEntity>

    /** Stratified by difficulty — used when pool is large enough */
    @Query("""
        SELECT * FROM questions
        WHERE subject = :subject AND grade BETWEEN :gradeMin AND :gradeMax
          AND difficulty = :difficulty
        ORDER BY RANDOM() LIMIT :limit
    """)
    suspend fun randomByDifficulty(
        subject: String, gradeMin: Int, gradeMax: Int,
        difficulty: Int, limit: Int
    ): List<QuestionEntity>

    // ── Pool size checks ──────────────────────────────────────────────────

    @Query("""
        SELECT COUNT(*) FROM questions
        WHERE subject = :subject AND grade BETWEEN :gradeMin AND :gradeMax
    """)
    suspend fun countBySubjectRange(subject: String, gradeMin: Int, gradeMax: Int): Int

    @Query("SELECT COUNT(*) FROM questions WHERE grade = :grade")
    suspend fun countByGrade(grade: Int): Int

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun countAll(): Int

    // ── Bookmarks / wrong answers test ────────────────────────────────────

    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN wrong_answers w ON q.checksum = w.questionChecksum
        ORDER BY RANDOM() LIMIT 25
    """)
    suspend fun randomFromWrongAnswers(): List<QuestionEntity>

    // ── Subjects & grades available ───────────────────────────────────────

    @Query("SELECT DISTINCT subject FROM questions ORDER BY subject")
    suspend fun availableSubjects(): List<String>

    @Query("SELECT DISTINCT grade FROM questions WHERE subject = :subject ORDER BY grade")
    suspend fun availableGrades(subject: String): List<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE subject = :subject AND grade = :grade")
    suspend fun countBySubjectGrade(subject: String, grade: Int): Int

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<QuestionEntity>
}
