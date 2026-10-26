package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_care_tasks",
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["dateKey"]),
        Index(value = ["profileId", "dateKey"])
    ]
)
data class DailyCareTaskEntity(
    @PrimaryKey
    val id: String,
    val profileId: String,
    val dateKey: String,                // "yyyy-MM-dd" in Asia/Kolkata timezone
    val taskType: String,               // "WATER", "SNAPSHOT", "ROTATE", "INSPECT", "FEED", "CLEAN", "PRUNE", "REPOT"
    val targetId: String,               // plantId or roomTag
    val targetTitle: String,            // plant nickname or space name
    val targetSubtitle: String,         // e.g. "Balcony" or "3 plants assigned"
    val targetPhotoUri: String? = null,
    val title: String,                  // e.g. "Watering Due", "Environment Snapshot Needed"
    val description: String,            // Golden rule or guidance
    val priority: Int,                  // 1 = High/Mandatory (Watering), 2 = Medium (Snapshot), 3 = Routine (Good to have)
    val urgencyDays: Int = 0,           // 0 = due today, >0 = overdue
    val spaceName: String = "",         // e.g. "Living room", "Balcony"
    val isSkipped: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
