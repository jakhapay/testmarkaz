package uz.testmarkaz.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["subject", "grade"]),
        Index(value = ["topic"]),
        Index(value = ["difficulty"]),
        Index(value = ["checksum"], unique = true)
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val grade: Int,              // 1–11; 0 = DTM
    val topic: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correct: String,         // "A" | "B" | "C" | "D"
    val explanation: String = "",
    val difficulty: Int = 2,     // 1=easy 2=medium 3=hard
    val sourceBook: String = "",
    val lang: String = "uz-Latn",
    val checksum: String,        // SHA256 for dedup
    val packKey: String = "mock"
)
