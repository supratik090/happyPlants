package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hos.happy.plants.data.local.entity.PlantIdentificationUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantIdentificationUsageDao {

    @Query("SELECT * FROM plant_identification_usage WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getUsageForDate(dateKey: String): PlantIdentificationUsageEntity?

    @Query("SELECT * FROM plant_identification_usage WHERE dateKey = :dateKey LIMIT 1")
    fun getUsageFlowForDate(dateKey: String): Flow<PlantIdentificationUsageEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(usage: PlantIdentificationUsageEntity)

    @Query("DELETE FROM plant_identification_usage WHERE dateKey < :cutoffDate")
    suspend fun cleanupOldRecords(cutoffDate: String)
}
