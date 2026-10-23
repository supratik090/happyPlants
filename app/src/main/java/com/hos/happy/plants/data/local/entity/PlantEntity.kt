package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "plants",
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["roomTag"])
    ]
)
data class PlantEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val masterPlantId: Int? = null,
    val nickname: String,
    val commonName: String,
    val botanicalName: String,
    val roomTag: String,
    val photoUri: String? = null,

    // Care Intervals (in days)
    val wateringIntervalDays: Int = 7,
    val fertilizeIntervalDays: Int = 60,
    val refreshSoilIntervalDays: Int = 180,
    val repotIntervalDays: Int = 730,

    // Last recorded care actions (epoch millis)
    val lastWateredAt: Long? = null,
    val lastFertilizedAt: Long? = null,
    val lastSoilRefreshedAt: Long? = null,
    val lastRepottedAt: Long? = null,

    // Environmental & Care Guidelines (cached from master plant)
    val lightIdealMinLux: Float? = null,
    val lightIdealMaxLux: Float? = null,
    val soilSimpleWords: String? = null,
    val soilMixRecipe: String? = null,
    val petSafetyNote: String? = null,

    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
