package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "room_env_snapshots",
    indices = [
        Index("profileId"),
        Index("roomTag"),
        Index("recordedAt")
    ]
)
data class RoomEnvSnapshotEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val roomTag: String,
    val recordedAt: Long = System.currentTimeMillis(),
    val lightLux: Float,
    val lightFootCandles: Float,
    val lightSource: String, // "SENSOR" or "CAMERA"
    val temperatureCelsius: Float?,
    val humidityPercentage: Float?,
    val aqi: Int?,
    val uvIndex: Float?,
    val daylightHours: Float?,
    val rainMm: Float? = null,
    val note: String? = null
)
