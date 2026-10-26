package com.hos.happy.plants.data.model

import android.graphics.Bitmap

data class PlantIdentificationResult(
    val commonName: String,
    val botanicalName: String,
    val family: String?,
    val genus: String?,
    val confidencePercent: Int,
    val referenceImageUrl: String?,
    val userCapturedBitmap: Bitmap?,
    val lightCondition: String,
    val moistureNeed: String,
    val soilAdvice: String,
    val localNames: Map<String, String>? = null,
    val masterPlant: MasterPlant? = null,
    val description: String? = null
)
