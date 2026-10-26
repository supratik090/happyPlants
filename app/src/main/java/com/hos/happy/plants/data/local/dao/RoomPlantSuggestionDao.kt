package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hos.happy.plants.data.local.entity.RoomPlantSuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomPlantSuggestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSuggestion(suggestion: RoomPlantSuggestionEntity): Long

    @Query("SELECT * FROM room_plant_suggestions WHERE profileId = :profileId AND roomTag = :roomTag LIMIT 1")
    suspend fun getSuggestionForRoom(profileId: String, roomTag: String): RoomPlantSuggestionEntity?

    @Query("SELECT * FROM room_plant_suggestions WHERE profileId = :profileId AND roomTag = :roomTag LIMIT 1")
    fun getSuggestionForRoomFlow(profileId: String, roomTag: String): Flow<RoomPlantSuggestionEntity?>

    @Query("SELECT * FROM room_plant_suggestions WHERE profileId = :profileId")
    fun getAllSuggestionsForProfileFlow(profileId: String): Flow<List<RoomPlantSuggestionEntity>>

    @Query("SELECT * FROM room_plant_suggestions WHERE profileId = :profileId")
    suspend fun getAllSuggestionsForProfile(profileId: String): List<RoomPlantSuggestionEntity>

    @Query("DELETE FROM room_plant_suggestions WHERE profileId = :profileId AND roomTag = :roomTag")
    suspend fun deleteForRoom(profileId: String, roomTag: String)
}
