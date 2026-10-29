package com.hos.happy.plants.data.remote.model

import com.google.gson.annotations.SerializedName

// --- Response from POST /v2/diseases/identify ---

data class PlantNetDiseaseResponse(
    @SerializedName("query") val query: PlantNetDiseaseQuery? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("results") val results: List<PlantNetDiseaseResult> = emptyList(),
    @SerializedName("version") val version: String? = null,
    @SerializedName("remainingIdentificationRequests") val remainingRequests: Int? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null
)

data class PlantNetDiseaseQuery(
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("organs") val organs: List<String> = emptyList(),
    @SerializedName("includeRelatedImages") val includeRelatedImages: Boolean? = null,
    @SerializedName("noReject") val noReject: Boolean? = null
)

data class PlantNetDiseaseResult(
    @SerializedName("name") val name: String = "",         // EPPO code (e.g. "APHISP")
    @SerializedName("score") val score: Double = 0.0,      // 0..1 confidence
    @SerializedName("images") val images: List<PlantNetDiseaseImage> = emptyList(),
    @SerializedName("description") val description: PlantNetDiseaseDescription? = null,
    @SerializedName("redundancy") val redundancy: Int? = null
)

data class PlantNetDiseaseImage(
    @SerializedName("organ") val organ: String? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("license") val license: String? = null,
    @SerializedName("date") val date: PlantNetDiseaseImageDate? = null,
    @SerializedName("url") val url: PlantNetDiseaseImageUrl? = null,
    @SerializedName("citation") val citation: String? = null
)

data class PlantNetDiseaseImageDate(
    @SerializedName("timestamp") val timestamp: Long? = null,
    @SerializedName("string") val string: String? = null
)

data class PlantNetDiseaseImageUrl(
    @SerializedName("o") val original: String? = null,
    @SerializedName("m") val medium: String? = null,
    @SerializedName("s") val small: String? = null
) {
    val bestUrl: String?
        get() = medium ?: original ?: small
}

data class PlantNetDiseaseDescription(
    @SerializedName("name") val name: String? = null,       // human-readable disease name
    @SerializedName("url") val url: String? = null,         // link to more info
    @SerializedName("language") val language: String? = null
)

// --- Domain model used by the UI ---

data class PlantDiseaseResult(
    val eppoCode: String,                   // e.g. "APHISP"
    val diseaseName: String,                // human-readable name or EPPO code fallback
    val confidencePercent: Int,             // 0–99
    val description: String?,              // short description text (may be null)
    val infoUrl: String?,                   // link for more info
    val referenceImageUrl: String?,         // best illustration image URL
    val treatmentAdvice: String             // inferred treatment advice
)
