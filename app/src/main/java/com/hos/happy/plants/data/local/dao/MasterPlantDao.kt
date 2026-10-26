package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hos.happy.plants.data.local.entity.MasterPlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterPlantDao {

    @Query("SELECT * FROM master_plants ORDER BY commonName ASC")
    fun getAllPlants(): List<MasterPlantEntity>

    @Query("SELECT * FROM master_plants ORDER BY commonName ASC")
    fun getAllPlantsFlow(): Flow<List<MasterPlantEntity>>

    @Query("SELECT * FROM master_plants WHERE id = :id LIMIT 1")
    fun getPlantById(id: Int): MasterPlantEntity?

    @Query("SELECT COUNT(*) FROM master_plants")
    fun getPlantCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(plants: List<MasterPlantEntity>)

    @Query("DELETE FROM master_plants")
    fun deleteAll()
}
