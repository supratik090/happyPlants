package com.hos.happy.plants.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_plants")
data class MasterPlantEntity(
    @PrimaryKey
    val id: Int,
    val commonName: String,
    val botanicalName: String,
    val localNamesJson: String,
    val photoUri: String?,
    val wateringIntervalSummerDays: Int,
    val wateringIntervalWinterDays: Int,
    val wateringNote: String?,
    val lightIdealMinLux: Float?,
    val lightIdealMaxLux: Float?,
    val tempIdealMinC: Float?,
    val tempIdealMaxC: Float?,
    val humidityMinPercent: Float?,
    val humidityMaxPercent: Float?,
    val soilSimpleWords: String?,
    val soilMixRecipe: String?,
    val fertilizeEveryDays: Int,
    val refreshSoilEveryDays: Int,
    val repotEveryDays: Int,
    val rawJson: String
)
