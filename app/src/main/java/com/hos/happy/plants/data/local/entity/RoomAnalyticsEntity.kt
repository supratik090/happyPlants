package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "room_analytics",
    indices = [
        Index(value = ["profileId", "roomTag"], unique = true)
    ]
)
data class RoomAnalyticsEntity(
    @PrimaryKey
    val id: String, // "${profileId}_${roomTag}"
    val profileId: String,
    val roomTag: String,
    val calculatedAt: Long = System.currentTimeMillis(),
    val calculatedDateStr: String,
    val vpdKpa: Float,
    val dliMol: Float,
    val snapshotCount: Int = 3
)
