package com.hos.happy.plants.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hos.happy.plants.data.local.dao.DailyCareTaskDao
import com.hos.happy.plants.data.local.dao.MasterPlantDao
import com.hos.happy.plants.data.local.dao.PlantCareLogDao
import com.hos.happy.plants.data.local.dao.PlantDao
import com.hos.happy.plants.data.local.dao.PlantIdentificationUsageDao
import com.hos.happy.plants.data.local.dao.PlantPhotoDao
import com.hos.happy.plants.data.local.dao.RoomAnalyticsDao
import com.hos.happy.plants.data.local.dao.RoomEnvSnapshotDao
import com.hos.happy.plants.data.local.dao.RoomPlantSuggestionDao
import com.hos.happy.plants.data.local.dao.UserProfileDao
import com.hos.happy.plants.data.local.entity.DailyCareTaskEntity
import com.hos.happy.plants.data.local.entity.MasterPlantEntity
import com.hos.happy.plants.data.local.entity.PlantCareLogEntity
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.data.local.entity.PlantIdentificationUsageEntity
import com.hos.happy.plants.data.local.entity.PlantPhotoEntity
import com.hos.happy.plants.data.local.entity.RoomAnalyticsEntity
import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import com.hos.happy.plants.data.local.entity.RoomPlantSuggestionEntity
import com.hos.happy.plants.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        RoomEnvSnapshotEntity::class,
        RoomAnalyticsEntity::class,
        PlantEntity::class,
        PlantCareLogEntity::class,
        PlantPhotoEntity::class,
        MasterPlantEntity::class,
        PlantIdentificationUsageEntity::class,
        DailyCareTaskEntity::class,
        RoomPlantSuggestionEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun roomEnvSnapshotDao(): RoomEnvSnapshotDao
    abstract fun roomAnalyticsDao(): RoomAnalyticsDao
    abstract fun plantDao(): PlantDao
    abstract fun plantCareLogDao(): PlantCareLogDao
    abstract fun plantPhotoDao(): PlantPhotoDao
    abstract fun masterPlantDao(): MasterPlantDao
    abstract fun plantIdentificationUsageDao(): PlantIdentificationUsageDao
    abstract fun dailyCareTaskDao(): DailyCareTaskDao
    abstract fun roomPlantSuggestionDao(): RoomPlantSuggestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `room_analytics` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `profileId` TEXT NOT NULL,
                        `roomTag` TEXT NOT NULL,
                        `calculatedAt` INTEGER NOT NULL,
                        `calculatedDateStr` TEXT NOT NULL,
                        `vpdKpa` REAL NOT NULL,
                        `dliMol` REAL NOT NULL,
                        `snapshotCount` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_room_analytics_profileId_roomTag` ON `room_analytics` (`profileId`, `roomTag`)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `plants` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `profileId` TEXT NOT NULL,
                        `masterPlantId` INTEGER,
                        `nickname` TEXT NOT NULL,
                        `commonName` TEXT NOT NULL,
                        `botanicalName` TEXT NOT NULL,
                        `roomTag` TEXT NOT NULL,
                        `photoUri` TEXT,
                        `wateringIntervalDays` INTEGER NOT NULL,
                        `fertilizeIntervalDays` INTEGER NOT NULL,
                        `refreshSoilIntervalDays` INTEGER NOT NULL,
                        `repotIntervalDays` INTEGER NOT NULL,
                        `rotateIntervalDays` INTEGER NOT NULL,
                        `inspectIntervalDays` INTEGER NOT NULL,
                        `cleanIntervalDays` INTEGER NOT NULL,
                        `pruneIntervalDays` INTEGER NOT NULL,
                        `lastWateredAt` INTEGER,
                        `lastFertilizedAt` INTEGER,
                        `lastSoilRefreshedAt` INTEGER,
                        `lastRepottedAt` INTEGER,
                        `lastRotatedAt` INTEGER,
                        `lastInspectedAt` INTEGER,
                        `lastCleanedAt` INTEGER,
                        `lastPrunedAt` INTEGER,
                        `lightIdealMinLux` REAL,
                        `lightIdealMaxLux` REAL,
                        `soilSimpleWords` TEXT,
                        `soilMixRecipe` TEXT,
                        `petSafetyNote` TEXT,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `isArchived` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_plants_profileId` ON `plants` (`profileId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_plants_roomTag` ON `plants` (`roomTag`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `plant_care_logs` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `plantId` TEXT NOT NULL,
                        `profileId` TEXT NOT NULL,
                        `careType` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `remark` TEXT,
                        `photoUri` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_plant_care_logs_plantId` ON `plant_care_logs` (`plantId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_plant_care_logs_profileId` ON `plant_care_logs` (`profileId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `plant_photos` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `plantId` TEXT NOT NULL,
                        `profileId` TEXT NOT NULL,
                        `photoUri` TEXT NOT NULL,
                        `capturedAt` INTEGER NOT NULL,
                        `caption` TEXT,
                        `careLogId` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_plant_photos_plantId` ON `plant_photos` (`plantId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_plant_photos_profileId` ON `plant_photos` (`profileId`)")
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `room_env_snapshots` ADD COLUMN `rainMm` REAL")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `master_plants` (
                        `id` INTEGER NOT NULL PRIMARY KEY,
                        `commonName` TEXT NOT NULL,
                        `botanicalName` TEXT NOT NULL,
                        `localNamesJson` TEXT NOT NULL,
                        `photoUri` TEXT,
                        `wateringIntervalSummerDays` INTEGER NOT NULL,
                        `wateringIntervalWinterDays` INTEGER NOT NULL,
                        `wateringNote` TEXT,
                        `lightIdealMinLux` REAL,
                        `lightIdealMaxLux` REAL,
                        `tempIdealMinC` REAL,
                        `tempIdealMaxC` REAL,
                        `humidityMinPercent` REAL,
                        `humidityMaxPercent` REAL,
                        `soilSimpleWords` TEXT,
                        `soilMixRecipe` TEXT,
                        `fertilizeEveryDays` INTEGER NOT NULL,
                        `refreshSoilEveryDays` INTEGER NOT NULL,
                        `repotEveryDays` INTEGER NOT NULL,
                        `rawJson` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `plant_identification_usage` (
                        `dateKey` TEXT NOT NULL PRIMARY KEY,
                        `attemptCount` INTEGER NOT NULL,
                        `lastAttemptAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_care_tasks` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `profileId` TEXT NOT NULL,
                        `dateKey` TEXT NOT NULL,
                        `taskType` TEXT NOT NULL,
                        `targetId` TEXT NOT NULL,
                        `targetTitle` TEXT NOT NULL,
                        `targetSubtitle` TEXT NOT NULL,
                        `targetPhotoUri` TEXT,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `priority` INTEGER NOT NULL,
                        `urgencyDays` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_care_tasks_profileId` ON `daily_care_tasks` (`profileId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_care_tasks_dateKey` ON `daily_care_tasks` (`dateKey`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_care_tasks_profileId_dateKey` ON `daily_care_tasks` (`profileId`, `dateKey`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `daily_care_tasks` ADD COLUMN `spaceName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `daily_care_tasks` ADD COLUMN `isSkipped` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `room_plant_suggestions` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `profileId` TEXT NOT NULL,
                        `roomTag` TEXT NOT NULL,
                        `calculatedAt` INTEGER NOT NULL,
                        `snapshotId` TEXT,
                        `suggestedMasterPlantIdsJson` TEXT NOT NULL,
                        `lightLabel` TEXT NOT NULL,
                        `tempLabel` TEXT NOT NULL,
                        `humidityLabel` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_room_plant_suggestions_profileId_roomTag` ON `room_plant_suggestions` (`profileId`, `roomTag`)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_room_plant_suggestions_profileId` ON `room_plant_suggestions` (`profileId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_room_plant_suggestions_roomTag` ON `room_plant_suggestions` (`roomTag`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "happy_plants.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
