package uz.testmarkaz.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_sessions")
data class TestSessionEntity(
    @PrimaryKey val id: String,          // UUID
    val subjectCode: String?,            // null = multi-subject
    val gradeMin: Int,
    val gradeMax: Int,
    val mode: String,                    // SUBJECT|RANGE|RANDOM_CLASS|FULL_RANDOM
    val score: Int,
    val total: Int = 25,
    val durationSeconds: Int? = null,
    val startedAt: Long,                 // epoch millis
    val completedAt: Long? = null,
    val syncedToServer: Boolean = false
)

@Entity(
    tableName = "session_answers",
    indices = [androidx.room.Index("sessionId")]
)
data class SessionAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val questionChecksum: String,
    val questionText: String,            // snapshot
    val selectedOption: String,
    val correctOption: String,
    val isCorrect: Boolean,
    val explanation: String = "",
    val answeredAt: Long = System.currentTimeMillis()
)
