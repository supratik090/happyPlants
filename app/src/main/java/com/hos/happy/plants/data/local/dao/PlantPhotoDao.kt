package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hos.happy.plants.data.local.entity.PlantPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PlantPhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PlantPhotoEntity)

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY capturedAt ASC")
    fun getPhotosForPlantFlow(plantId: String): Flow<List<PlantPhotoEntity>>

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY capturedAt ASC")
    suspend fun getPhotosForPlant(plantId: String): List<PlantPhotoEntity>
}
