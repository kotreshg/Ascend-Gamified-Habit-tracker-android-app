package com.example.data.dao

import androidx.room.*
import com.example.data.model.ActivityEntity
import com.example.data.model.BreakEntity
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.SessionEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE isArchived = 0 ORDER BY sortOrder ASC")
    fun getAllActivitiesFlow(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE isArchived = 0 ORDER BY sortOrder ASC")
    suspend fun getAllActivities(): List<ActivityEntity>

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    suspend fun getActivityById(id: String): ActivityEntity?

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    fun getActivityByIdFlow(id: String): Flow<ActivityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Query("UPDATE activities SET isArchived = 1 WHERE id = :id")
    suspend fun archiveActivity(id: String)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteActivityById(id: String)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE status != 'COMPLETED' LIMIT 1")
    fun getActiveSessionFlow(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE status != 'COMPLETED' LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): SessionEntity?

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionByIdFlow(sessionId: Long): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE activityId = :activityId ORDER BY startTime DESC")
    fun getSessionsByActivityFlow(activityId: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE activityId = :activityId ORDER BY startTime DESC")
    suspend fun getSessionsByActivity(activityId: String): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE dateString = :dateString ORDER BY startTime ASC")
    fun getSessionsByDateFlow(dateString: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE dateString = :dateString ORDER BY startTime ASC")
    suspend fun getSessionsByDate(dateString: String): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE activityId = :activityId AND dateString = :dateString ORDER BY startTime ASC")
    fun getSessionsByActivityAndDateFlow(activityId: String, dateString: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE activityId = :activityId AND dateString = :dateString ORDER BY startTime ASC")
    suspend fun getSessionsByActivityAndDate(activityId: String, dateString: String): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM sessions WHERE activityId = :activityId")
    suspend fun deleteSessionsByActivityId(activityId: String)

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()
}

@Dao
interface BreakDao {
    @Query("SELECT * FROM breaks WHERE sessionId = :sessionId ORDER BY startTime ASC")
    fun getBreaksForSessionFlow(sessionId: Long): Flow<List<BreakEntity>>

    @Query("SELECT * FROM breaks WHERE sessionId = :sessionId ORDER BY startTime ASC")
    suspend fun getBreaksForSession(sessionId: Long): List<BreakEntity>

    @Query("SELECT * FROM breaks ORDER BY startTime ASC")
    fun getAllBreaksFlow(): Flow<List<BreakEntity>>

    @Query("SELECT * FROM breaks ORDER BY startTime ASC")
    suspend fun getAllBreaks(): List<BreakEntity>

    @Query("SELECT * FROM breaks WHERE sessionId IN (:sessionIds) ORDER BY startTime ASC")
    suspend fun getBreaksForSessions(sessionIds: List<Long>): List<BreakEntity>

    @Query("SELECT * FROM breaks WHERE sessionId = :sessionId AND endTime IS NULL LIMIT 1")
    suspend fun getActiveBreakForSession(sessionId: Long): BreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreak(breakEntity: BreakEntity): Long

    @Update
    suspend fun updateBreak(breakEntity: BreakEntity)

    @Delete
    suspend fun deleteBreak(breakEntity: BreakEntity)

    @Query("DELETE FROM breaks WHERE sessionId = :sessionId")
    suspend fun deleteBreaksForSession(sessionId: Long)

    @Query("DELETE FROM breaks WHERE sessionId IN (SELECT id FROM sessions WHERE activityId = :activityId)")
    suspend fun deleteBreaksByActivityId(activityId: String)

    @Query("DELETE FROM breaks")
    suspend fun deleteAllBreaks()
}

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions ORDER BY dateString DESC")
    fun getAllCompletionsFlow(): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions ORDER BY dateString DESC")
    suspend fun getAllCompletions(): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE activityId = :activityId ORDER BY dateString DESC")
    fun getCompletionsByActivityFlow(activityId: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE activityId = :activityId ORDER BY dateString DESC")
    suspend fun getCompletionsByActivity(activityId: String): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE activityId = :activityId AND dateString = :dateString LIMIT 1")
    suspend fun getCompletion(activityId: String, dateString: String): HabitCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE activityId = :activityId AND dateString = :dateString")
    suspend fun deleteCompletion(activityId: String, dateString: String)

    @Query("DELETE FROM habit_completions WHERE activityId = :activityId")
    suspend fun deleteAllCompletionsForActivity(activityId: String)

    @Query("DELETE FROM habit_completions")
    suspend fun deleteAllCompletions()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)

    @Update
    suspend fun updateSettings(settings: UserSettingsEntity)
}
