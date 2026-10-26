package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_identification_usage")
data class PlantIdentificationUsageEntity(
    @PrimaryKey
    val dateKey: String, // "yyyy-MM-dd" in IST (Asia/Kolkata)
    val attemptCount: Int,
    val lastAttemptAt: Long
)
