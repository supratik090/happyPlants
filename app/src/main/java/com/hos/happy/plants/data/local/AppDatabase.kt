package com.hos.happy.plants.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hos.happy.plants.data.local.dao.PlantCareLogDao
import com.hos.happy.plants.data.local.dao.PlantDao
import com.hos.happy.plants.data.local.dao.PlantPhotoDao
import com.hos.happy.plants.data.local.dao.RoomAnalyticsDao
import com.hos.happy.plants.data.local.dao.RoomEnvSnapshotDao
import com.hos.happy.plants.data.local.dao.UserProfileDao
import com.hos.happy.plants.data.local.entity.PlantCareLogEntity
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.data.local.entity.PlantPhotoEntity
import com.hos.happy.plants.data.local.entity.RoomAnalyticsEntity
import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import com.hos.happy.plants.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        RoomEnvSnapshotEntity::class,
        RoomAnalyticsEntity::class,
        PlantEntity::class,
        PlantCareLogEntity::class,
        PlantPhotoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun roomEnvSnapshotDao(): RoomEnvSnapshotDao
    abstract fun roomAnalyticsDao(): RoomAnalyticsDao
    abstract fun plantDao(): PlantDao
    abstract fun plantCareLogDao(): PlantCareLogDao
    abstract fun plantPhotoDao(): PlantPhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "happy_plants.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
