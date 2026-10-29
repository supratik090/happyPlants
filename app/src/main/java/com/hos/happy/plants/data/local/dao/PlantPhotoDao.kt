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

    @Query("DELETE FROM plant_photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: String)

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY capturedAt ASC")
    fun getPhotosForPlantFlow(plantId: String): Flow<List<PlantPhotoEntity>>

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY capturedAt DESC LIMIT 1")
    fun getLatestPhotoForPlantFlow(plantId: String): Flow<PlantPhotoEntity?>

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY capturedAt ASC")
    suspend fun getPhotosForPlant(plantId: String): List<PlantPhotoEntity>

    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY capturedAt DESC LIMIT 1")
    suspend fun getLatestPhotoForPlant(plantId: String): PlantPhotoEntity?


    @Query("SELECT COUNT(*) FROM plant_photos WHERE profileId = :profileId AND capturedAt >= :sinceTimestamp")
    suspend fun getPhotoCountSince(profileId: String, sinceTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM plant_photos WHERE profileId = :profileId")
    suspend fun getTotalPhotoCount(profileId: String): Int

    @Query("SELECT * FROM plant_photos WHERE profileId = :profileId ORDER BY capturedAt DESC LIMIT 1")
    suspend fun getLatestPhotoForProfile(profileId: String): PlantPhotoEntity?
}
