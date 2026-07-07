package com.oussama_chatri.productivityx.features.pomodoro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oussama_chatri.productivityx.features.pomodoro.data.local.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PomodoroSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<PomodoroSessionEntity>)

    @Query("DELETE FROM pomodoro_sessions_local WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pomodoro_sessions_local WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query(
        """
        SELECT COALESCE(SUM(actual_duration_seconds), 0) / 60
        FROM pomodoro_sessions_local
        WHERE user_id = :userId
          AND type = 'FOCUS'
          AND completed = 1
          AND started_at >= :dayStartMs
          AND started_at  < :dayEndMs
    """
    )
    suspend fun totalFocusMinutesToday(userId: String, dayStartMs: Long, dayEndMs: Long): Int

    @Query(
        """
        SELECT * FROM pomodoro_sessions_local
        WHERE user_id = :userId
          AND type = 'FOCUS'
          AND completed = 1
          AND started_at >= :startMs
          AND started_at < :endMs
        ORDER BY started_at ASC
    """
    )
    suspend fun getFocusSessionsInRange(userId: String, startMs: Long, endMs: Long): List<PomodoroSessionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM pomodoro_sessions_local
        WHERE user_id = :userId
          AND type = 'FOCUS'
          AND completed = 1
          AND started_at >= :startMs
          AND started_at < :endMs
    """
    )
    suspend fun countFocusSessionsInRange(userId: String, startMs: Long, endMs: Long): Long

    @Query(
        """
        SELECT COALESCE(SUM(actual_duration_seconds), 0)
        FROM pomodoro_sessions_local
        WHERE user_id = :userId
          AND type = 'FOCUS'
          AND completed = 1
    """
    )
    suspend fun getTotalFocusSeconds(userId: String): Long

    @Query(
        """
        SELECT COALESCE(SUM(actual_duration_seconds), 0)
        FROM pomodoro_sessions_local
        WHERE user_id = :userId
          AND type = 'FOCUS'
          AND completed = 1
          AND started_at >= :startMs
          AND started_at < :endMs
    """
    )
    suspend fun sumFocusSecondsInRange(userId: String, startMs: Long, endMs: Long): Long

    @Query(
        """
        SELECT COUNT(*) FROM pomodoro_sessions_local
        WHERE user_id = :userId
          AND type = 'FOCUS'
          AND completed = 1
          AND interrupted = 1
          AND started_at >= :startMs
          AND started_at < :endMs
    """
    )
    suspend fun countInterruptedFocusSessionsInRange(userId: String, startMs: Long, endMs: Long): Long

    @Query(
        """
        SELECT * FROM pomodoro_sessions_local
        WHERE user_id = :userId
          AND type = 'FOCUS'
          AND completed = 1
        ORDER BY started_at ASC
    """
    )
    suspend fun getAllCompletedFocusSessions(userId: String): List<PomodoroSessionEntity>

    @Query(
        """
        SELECT * FROM pomodoro_sessions_local
        WHERE user_id = :userId
        ORDER BY started_at DESC
        LIMIT :limit
    """
    )
    suspend fun getRecentSessions(userId: String, limit: Int = 20): List<PomodoroSessionEntity>

    @Query("SELECT * FROM pomodoro_sessions_local WHERE id = :id")
    suspend fun getById(id: String): PomodoroSessionEntity?

    @Query(
        """
        SELECT * FROM pomodoro_sessions_local
        WHERE user_id = :userId AND ended_at IS NULL
        ORDER BY started_at DESC
        LIMIT 1
    """
    )
    suspend fun getActiveSession(userId: String): PomodoroSessionEntity?

    @Query(
        """
        SELECT * FROM pomodoro_sessions_local
        WHERE user_id = :userId
        ORDER BY started_at DESC
        LIMIT :limit
    """
    )
    fun observeSessions(userId: String, limit: Int = 50): Flow<List<PomodoroSessionEntity>>
}
