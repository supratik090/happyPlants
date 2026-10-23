package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomEnvSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: RoomEnvSnapshotEntity): Long

    @Query("SELECT * FROM room_env_snapshots WHERE profileId = :profileId ORDER BY recordedAt DESC")
    fun getAllSnapshotsForProfileFlow(profileId: String): Flow<List<RoomEnvSnapshotEntity>>

    @Query("SELECT DISTINCT roomTag FROM room_env_snapshots WHERE profileId = :profileId ORDER BY roomTag ASC")
    fun getDistinctRoomsFlow(profileId: String): Flow<List<String>>

    @Query("""
        SELECT * FROM room_env_snapshots 
        WHERE profileId = :profileId 
        AND (:roomTag IS NULL OR roomTag = :roomTag)
        AND (:startTimeMillis IS NULL OR recordedAt >= :startTimeMillis)
        AND (:endTimeMillis IS NULL OR recordedAt <= :endTimeMillis)
        ORDER BY recordedAt DESC
    """)
    fun filterSnapshotsFlow(
        profileId: String,
        roomTag: String?,
        startTimeMillis: Long?,
        endTimeMillis: Long?
    ): Flow<List<RoomEnvSnapshotEntity>>

    @Query("SELECT * FROM room_env_snapshots WHERE profileId = :profileId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestSnapshot(profileId: String): RoomEnvSnapshotEntity?
}
