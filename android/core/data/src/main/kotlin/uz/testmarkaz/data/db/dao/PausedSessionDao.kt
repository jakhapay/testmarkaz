package uz.testmarkaz.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import uz.testmarkaz.data.db.entity.PausedSessionEntity

@Dao
interface PausedSessionDao {

    @Upsert
    suspend fun save(entity: PausedSessionEntity)

    @Query("SELECT * FROM paused_sessions ORDER BY savedAt DESC LIMIT 1")
    suspend fun getLatest(): PausedSessionEntity?

    @Query("SELECT * FROM paused_sessions WHERE sessionId = :sessionId")
    suspend fun getById(sessionId: String): PausedSessionEntity?

    @Query("SELECT * FROM paused_sessions ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<PausedSessionEntity>>

    @Query("SELECT COUNT(*) > 0 FROM paused_sessions")
    fun observeHasPaused(): Flow<Boolean>

    @Query("DELETE FROM paused_sessions WHERE sessionId = :sessionId")
    suspend fun delete(sessionId: String)

    @Query("DELETE FROM paused_sessions")
    suspend fun deleteAll()
}
