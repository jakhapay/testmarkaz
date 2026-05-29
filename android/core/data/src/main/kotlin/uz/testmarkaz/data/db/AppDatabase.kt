package uz.testmarkaz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import uz.testmarkaz.data.db.dao.PausedSessionDao
import uz.testmarkaz.data.db.dao.ProgressDao
import uz.testmarkaz.data.db.dao.QuestionDao
import uz.testmarkaz.data.db.dao.TestSessionDao
import uz.testmarkaz.data.db.entity.InstalledPackEntity
import uz.testmarkaz.data.db.entity.PausedSessionEntity
import uz.testmarkaz.data.db.entity.QuestionEntity
import uz.testmarkaz.data.db.entity.SessionAnswerEntity
import uz.testmarkaz.data.db.entity.TestSessionEntity
import uz.testmarkaz.data.db.entity.TopicMasteryEntity
import uz.testmarkaz.data.db.entity.UserStatsEntity
import uz.testmarkaz.data.db.entity.WrongAnswerEntity

@Database(
    entities = [
        QuestionEntity::class,
        TestSessionEntity::class,
        SessionAnswerEntity::class,
        TopicMasteryEntity::class,
        UserStatsEntity::class,
        WrongAnswerEntity::class,
        InstalledPackEntity::class,
        PausedSessionEntity::class,
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun testSessionDao(): TestSessionDao
    abstract fun progressDao(): ProgressDao
    abstract fun pausedSessionDao(): PausedSessionDao
}
