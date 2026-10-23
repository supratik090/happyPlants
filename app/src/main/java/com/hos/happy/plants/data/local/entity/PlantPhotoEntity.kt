package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "plant_photos",
    indices = [
        Index(value = ["plantId"]),
        Index(value = ["profileId"])
    ]
)
data class PlantPhotoEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val plantId: String,
    val profileId: String,
    val photoUri: String,
    val capturedAt: Long = System.currentTimeMillis(),
    val caption: String? = null,
    val careLogId: String? = null
)
