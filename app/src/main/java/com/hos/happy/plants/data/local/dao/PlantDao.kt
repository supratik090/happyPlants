package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hos.happy.plants.data.local.entity.PlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity)

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    @Query("SELECT * FROM plants WHERE profileId = :profileId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getPlantsForProfileFlow(profileId: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE profileId = :profileId AND isArchived = 0 ORDER BY createdAt DESC")
    suspend fun getPlantsForProfile(profileId: String): List<PlantEntity>

    @Query("SELECT * FROM plants WHERE profileId = :profileId AND roomTag = :roomTag AND isArchived = 0 ORDER BY createdAt DESC")
    fun getPlantsForRoomFlow(profileId: String, roomTag: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE profileId = :profileId AND roomTag = :roomTag AND isArchived = 0 ORDER BY createdAt DESC")
    suspend fun getPlantsForRoom(profileId: String, roomTag: String): List<PlantEntity>

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    suspend fun getPlantById(id: String): PlantEntity?

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    fun getPlantByIdFlow(id: String): Flow<PlantEntity?>

    @Query("UPDATE plants SET lastWateredAt = :timestamp WHERE id = :plantId")
    suspend fun updateLastWatered(plantId: String, timestamp: Long)

    @Query("UPDATE plants SET lastFertilizedAt = :timestamp WHERE id = :plantId")
    suspend fun updateLastFertilized(plantId: String, timestamp: Long)

    @Query("UPDATE plants SET lastSoilRefreshedAt = :timestamp WHERE id = :plantId")
    suspend fun updateLastSoilRefreshed(plantId: String, timestamp: Long)

    @Query("UPDATE plants SET lastRepottedAt = :timestamp WHERE id = :plantId")
    suspend fun updateLastRepotted(plantId: String, timestamp: Long)

    @Query("UPDATE plants SET lastRotatedAt = :timestamp WHERE id = :plantId")
    suspend fun updateLastRotated(plantId: String, timestamp: Long)

    @Query("UPDATE plants SET lastInspectedAt = :timestamp WHERE id = :plantId")
    suspend fun updateLastInspected(plantId: String, timestamp: Long)

    @Query("UPDATE plants SET lastCleanedAt = :timestamp WHERE id = :plantId")
    suspend fun updateLastCleaned(plantId: String, timestamp: Long)

    @Query("UPDATE plants SET lastPrunedAt = :timestamp WHERE id = :plantId")
    suspend fun updateLastPruned(plantId: String, timestamp: Long)

    @Query("UPDATE plants SET waterScheduleType = :type, waterScheduleValue = :value, waterScheduleSetAt = :setAt WHERE id = :plantId")
    suspend fun updateWaterSchedule(plantId: String, type: String, value: String, setAt: Long)

    @Query("UPDATE plants SET wateringIntervalDays = :interval WHERE id = :plantId")
    suspend fun updateWateringInterval(plantId: String, interval: Int)

    @Query("UPDATE plants SET nickname = :nickname WHERE id = :plantId")
    suspend fun updateNickname(plantId: String, nickname: String)
}

