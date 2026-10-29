package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hos.happy.plants.data.local.entity.AppSessionEntity

@Dao
interface AppSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AppSessionEntity)

    @Update
    suspend fun updateSession(session: AppSessionEntity)

    @Query("UPDATE app_sessions SET endTime = :endTime, durationSeconds = :durationSeconds WHERE id = :id")
    suspend fun updateSessionEndTime(id: String, endTime: Long, durationSeconds: Long)

    @Query("SELECT * FROM app_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): AppSessionEntity?

    @Query("SELECT * FROM app_sessions WHERE profileId = :profileId AND isSynced = 0 ORDER BY startTime ASC")
    suspend fun getUnsyncedSessions(profileId: String): List<AppSessionEntity>

    @Query("UPDATE app_sessions SET isSynced = 1 WHERE id IN (:sessionIds)")
    suspend fun markSessionsAsSynced(sessionIds: List<String>)

    @Query("SELECT COUNT(*) FROM app_sessions WHERE profileId = :profileId AND startTime >= :sinceTimestamp")
    suspend fun getLaunchCountSince(profileId: String, sinceTimestamp: Long): Int

    @Query("SELECT SUM(durationSeconds) FROM app_sessions WHERE profileId = :profileId AND startTime >= :sinceTimestamp")
    suspend fun getTotalDurationSecondsSince(profileId: String, sinceTimestamp: Long): Long?

    @Query("SELECT COUNT(*) FROM app_sessions WHERE profileId = :profileId")
    suspend fun getTotalLaunchCount(profileId: String): Int

    @Query("SELECT SUM(durationSeconds) FROM app_sessions WHERE profileId = :profileId")
    suspend fun getTotalDurationSeconds(profileId: String): Long?

    @Query("SELECT * FROM app_sessions WHERE profileId = :profileId ORDER BY startTime DESC")
    suspend fun getAllSessions(profileId: String): List<AppSessionEntity>

    @Query("UPDATE app_sessions SET profileId = :newProfileId WHERE profileId = 'anonymous'")
    suspend fun claimAnonymousSessions(newProfileId: String)
}
