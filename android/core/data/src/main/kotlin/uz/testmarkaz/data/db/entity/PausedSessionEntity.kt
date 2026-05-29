package uz.testmarkaz.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paused_sessions")
data class PausedSessionEntity(
    @PrimaryKey val sessionId: String,
    val currentIndex: Int,
    /** JSON: {"<questionId>": "<option>", ...} */
    val answersJson: String,
    /** Comma-separated question IDs in session order */
    val questionIds: String,
    val mode: String,
    val subjectCode: String?,
    val gradeMin: Int,
    val gradeMax: Int,
    val startedAt: Long,
    val savedAt: Long = System.currentTimeMillis()
)
