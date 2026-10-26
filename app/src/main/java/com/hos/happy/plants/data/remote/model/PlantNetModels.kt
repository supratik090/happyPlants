package com.hos.happy.plants.data.remote.model

import com.google.gson.annotations.SerializedName

data class PlantNetResponse(
    @SerializedName("query") val query: PlantNetQuery? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("results") val results: List<PlantNetResult> = emptyList(),
    @SerializedName("remainingIdentificationRequests") val remainingRequests: Int? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null
)

data class PlantNetQuery(
    @SerializedName("project") val project: String? = null,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("organs") val organs: List<String> = emptyList()
)

data class PlantNetResult(
    @SerializedName("score") val score: Double = 0.0,
    @SerializedName("species") val species: PlantNetSpecies,
    @SerializedName("images") val images: List<PlantNetImage> = emptyList()
)

data class PlantNetSpecies(
    @SerializedName("scientificNameWithoutAuthor") val scientificNameWithoutAuthor: String = "",
    @SerializedName("scientificNameAuthorship") val scientificNameAuthorship: String? = null,
    @SerializedName("scientificName") val scientificName: String = "",
    @SerializedName("genus") val genus: PlantNetTaxon? = null,
    @SerializedName("family") val family: PlantNetTaxon? = null,
    @SerializedName("commonNames") val commonNames: List<String> = emptyList()
)

data class PlantNetTaxon(
    @SerializedName("scientificNameWithoutAuthor") val scientificNameWithoutAuthor: String = "",
    @SerializedName("scientificNameAuthorship") val scientificNameAuthorship: String? = null,
    @SerializedName("scientificName") val scientificName: String? = null
)

data class PlantNetImage(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organ") val organ: String? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("citation") val citation: String? = null,
    @SerializedName("url") val url: PlantNetImageUrl? = null
)

data class PlantNetImageUrl(
    @SerializedName("o") val original: String? = null,
    @SerializedName("m") val medium: String? = null,
    @SerializedName("s") val small: String? = null
) {
    val bestUrl: String?
        get() = medium ?: original ?: small
}

data class WikipediaSummaryResponse(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("extract") val extract: String? = null,
    @SerializedName("thumbnail") val thumbnail: WikipediaImageSource? = null,
    @SerializedName("originalimage") val originalimage: WikipediaImageSource? = null
)

data class WikipediaImageSource(
    @SerializedName("source") val source: String? = null,
    @SerializedName("width") val width: Int = 0,
    @SerializedName("height") val height: Int = 0
)
