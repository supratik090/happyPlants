package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "app_sessions",
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["startTime"])
    ]
)
data class AppSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationSeconds: Long = 0L,
    val isSynced: Boolean = false
)
