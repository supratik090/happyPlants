package com.hos.happy.plants.data.remote.model

data class WeeklyMacroClimate(
    val totalRainMm: Float,
    val avgSunlightHoursPerDay: Float,
    val totalSunlightHours: Float,
    val avgHumidityPercent: Float,
    val avgPm10: Float?,
    val avgAqi: Int?,
    val rainStatement: String,
    val sunlightStatement: String,
    val humidityStatement: String,
    val dustStatement: String
)
