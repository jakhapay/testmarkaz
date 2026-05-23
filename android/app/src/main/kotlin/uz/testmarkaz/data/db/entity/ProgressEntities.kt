package uz.testmarkaz.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "topic_mastery",
    primaryKeys = ["subjectCode", "grade", "topic"]
)
data class TopicMasteryEntity(
    val subjectCode: String,
    val grade: Int,
    val topic: String,
    val totalAnswered: Int = 0,
    val correctCount: Int = 0,
    val eloRating: Int = 1500,
    val lastPracticed: Long? = null
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalTests: Int = 0,
    val totalCorrect: Int = 0,
    val lastTestDay: Long? = null      // epoch day (millis / 86400000)
)

@Entity(
    tableName = "wrong_answers",
    primaryKeys = ["questionChecksum"]
)
data class WrongAnswerEntity(
    val questionChecksum: String,
    val subject: String,
    val grade: Int,
    val topic: String,
    val questionText: String,
    val correctOption: String,
    val explanation: String,
    val wrongCount: Int = 1,
    val lastWrongAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "installed_packs",
    primaryKeys = ["packKey"]
)
data class InstalledPackEntity(
    val packKey: String,
    val subjectCode: String,
    val grade: Int?,
    val gradeMin: Int?,
    val gradeMax: Int?,
    val questionCount: Int,
    val version: Int = 1,
    val installedAt: Long = System.currentTimeMillis()
)
