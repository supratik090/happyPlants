package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "room_plant_suggestions",
    indices = [
        Index(value = ["profileId", "roomTag"], unique = true),
        Index("profileId"),
        Index("roomTag")
    ]
)
data class RoomPlantSuggestionEntity(
    @PrimaryKey
    val id: String, // "${profileId}_${roomTag}"
    val profileId: String,
    val roomTag: String,
    val calculatedAt: Long = System.currentTimeMillis(),
    val snapshotId: String?,
    val suggestedMasterPlantIdsJson: String, // JSON array: e.g. "[1, 4, 7]"
    val lightLabel: String,
    val tempLabel: String,
    val humidityLabel: String
)
