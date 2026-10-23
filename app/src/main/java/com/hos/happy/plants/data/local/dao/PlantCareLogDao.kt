package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hos.happy.plants.data.local.entity.PlantCareLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantCareLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PlantCareLogEntity)

    @Query("SELECT * FROM plant_care_logs WHERE plantId = :plantId ORDER BY timestamp DESC")
    fun getLogsForPlantFlow(plantId: String): Flow<List<PlantCareLogEntity>>

    @Query("SELECT * FROM plant_care_logs WHERE plantId = :plantId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogsForPlant(plantId: String, limit: Int = 10): List<PlantCareLogEntity>
}
