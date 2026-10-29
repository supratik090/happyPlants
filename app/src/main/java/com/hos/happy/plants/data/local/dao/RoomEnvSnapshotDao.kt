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

    @Query("SELECT DISTINCT roomTag FROM room_env_snapshots WHERE profileId = :profileId ORDER BY roomTag ASC")
    suspend fun getDistinctRooms(profileId: String): List<String>

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

    @Query("""
        SELECT recordedAt FROM room_env_snapshots 
        WHERE profileId = :profileId 
        AND (:roomTag IS NULL OR roomTag = :roomTag)
        ORDER BY recordedAt DESC
    """)
    fun getRecordedTimestampsFlow(profileId: String, roomTag: String?): Flow<List<Long>>

    @Query("SELECT * FROM room_env_snapshots WHERE profileId = :profileId AND roomTag = :roomTag ORDER BY recordedAt DESC")
    fun getAllSnapshotsForRoomFlow(profileId: String, roomTag: String): Flow<List<RoomEnvSnapshotEntity>>

    @Query("SELECT * FROM room_env_snapshots WHERE profileId = :profileId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestSnapshot(profileId: String): RoomEnvSnapshotEntity?

    @Query("SELECT * FROM room_env_snapshots WHERE profileId = :profileId AND roomTag = :roomTag ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestSnapshotForRoom(profileId: String, roomTag: String): RoomEnvSnapshotEntity?

    @Query("SELECT * FROM room_env_snapshots WHERE id = :snapshotId LIMIT 1")
    suspend fun getSnapshotById(snapshotId: String): RoomEnvSnapshotEntity?

    @Query("DELETE FROM room_env_snapshots WHERE id = :snapshotId")
    suspend fun deleteSnapshotById(snapshotId: String)
}

