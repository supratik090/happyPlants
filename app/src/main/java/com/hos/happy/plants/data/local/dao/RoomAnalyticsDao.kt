package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hos.happy.plants.data.local.entity.RoomAnalyticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomAnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnalytics(analytics: RoomAnalyticsEntity): Long

    @Query("SELECT * FROM room_analytics WHERE profileId = :profileId AND roomTag = :roomTag LIMIT 1")
    fun getAnalyticsForRoomFlow(profileId: String, roomTag: String): Flow<RoomAnalyticsEntity?>

    @Query("SELECT * FROM room_analytics WHERE profileId = :profileId AND roomTag = :roomTag LIMIT 1")
    suspend fun getAnalyticsForRoom(profileId: String, roomTag: String): RoomAnalyticsEntity?

    @Query("SELECT * FROM room_analytics WHERE profileId = :profileId")
    fun getAllAnalyticsForProfileFlow(profileId: String): Flow<List<RoomAnalyticsEntity>>
}
