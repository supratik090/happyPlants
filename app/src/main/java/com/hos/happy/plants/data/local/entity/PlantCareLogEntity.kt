package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "plant_care_logs",
    indices = [
        Index(value = ["plantId"]),
        Index(value = ["profileId"])
    ]
)
data class PlantCareLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val plantId: String,
    val profileId: String,
    val careType: String, // "WATER", "FERTILIZE", "SOIL_REFRESH", "REPOT"
    val timestamp: Long = System.currentTimeMillis(),
    val remark: String? = null,
    val photoUri: String? = null
)
