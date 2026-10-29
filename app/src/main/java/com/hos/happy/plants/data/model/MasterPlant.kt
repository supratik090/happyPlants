package com.hos.happy.plants.data.model

import com.google.gson.annotations.SerializedName

data class MasterPlantCatalog(
    @SerializedName("meta") val meta: CatalogMeta? = null,
    @SerializedName("plants") val plants: List<MasterPlant> = emptyList()
)

data class CatalogMeta(
    @SerializedName("title") val title: String? = null,
    @SerializedName("version") val version: Int = 1,
    @SerializedName("region") val region: String? = null,
    @SerializedName("total_plants") val totalPlants: Int? = null
)

data class MasterPlant(
    @SerializedName("id") val id: Int,
    @SerializedName("botanical_name") val botanicalName: String,
    @SerializedName("common_name") val commonName: String,
    @SerializedName("region") val region: String? = "India",
    @SerializedName("popularity_score") val popularityScore: Int = 8,
    @SerializedName("local_names") val localNames: Map<String, String>? = null,
    @SerializedName("watering_every_days") val watering: WateringInfo? = null,
    @SerializedName("light_lux") val lightLux: LightLuxInfo? = null,
    @SerializedName("humidity_percent") val humidity: HumidityInfo? = null,
    @SerializedName("temperature_c") val temperature: TemperatureInfo? = null,
    @SerializedName("soil") val soil: SoilInfo? = null,
    @SerializedName("fertilise_or_manure_every_days") val fertilizeEveryDays: Int? = 60,
    @SerializedName("refresh_top_soil_every_days") val refreshTopSoilEveryDays: Int? = 180,
    @SerializedName("repot_every_days") val repotEveryDays: Int? = 730,
    @SerializedName("pet_and_child_safety") val petSafety: PetSafetyInfo? = null,
    @SerializedName("common_problems") val commonProblems: List<CommonProblem> = emptyList(),
    @SerializedName("image") val image: MasterPlantImage? = null
) {
    val displayImageUrl: String?
        get() = image?.bestImageUrl
}

data class WateringInfo(
    @SerializedName("hot_dry_months_mar_jun") val hotDryMonths: MinMaxDays? = null,
    @SerializedName("monsoon_and_winter") val monsoonAndWinter: MinMaxDays? = null,
    @SerializedName("note") val note: String? = null
)

data class MinMaxDays(
    @SerializedName("min") val min: Int = 7,
    @SerializedName("max") val max: Int = 10
)

data class LightLuxInfo(
    @SerializedName("min_tolerated") val minTolerated: Float? = null,
    @SerializedName("ideal_min") val idealMin: Float? = null,
    @SerializedName("ideal_max") val idealMax: Float? = null,
    @SerializedName("max_tolerated") val maxTolerated: Float? = null
)

data class HumidityInfo(
    @SerializedName("min") val min: Float? = null,
    @SerializedName("max") val max: Float? = null
)

data class TemperatureInfo(
    @SerializedName("tolerable_min") val tolerableMin: Float? = null,
    @SerializedName("tolerable_max") val tolerableMax: Float? = null,
    @SerializedName("ideal_min") val idealMin: Float? = null,
    @SerializedName("ideal_max") val idealMax: Float? = null
)

data class SoilInfo(
    @SerializedName("in_simple_words") val inSimpleWords: String? = null,
    @SerializedName("mix_recipe") val mixRecipe: String? = null,
    @SerializedName("ph_min") val phMin: Float? = null,
    @SerializedName("ph_max") val phMax: Float? = null
)

data class PetSafetyInfo(
    @SerializedName("level") val level: String? = null,
    @SerializedName("note") val note: String? = null
)

data class CommonProblem(
    @SerializedName("id") val id: String,
    @SerializedName("problem") val problem: String,
    @SerializedName("do_you_see_this_problem") val doYouSeeThisProblem: String,
    @SerializedName("solve_it_like_this") val solveItLikeThis: List<String> = emptyList()
)

data class MasterPlantImage(
    @SerializedName("wikipedia_page") val wikipediaPage: String? = null,
    @SerializedName("resolver_api_url") val resolverApiUrl: String? = null,
    @SerializedName("direct_image_url") val directImageUrl: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("local_file") val localFile: String? = null,
    @SerializedName("asset_path") val assetPath: String? = null
) {
    val bestImageUrl: String?
        get() = assetPath
            ?: localFile?.let { if (it.startsWith("file:///android_asset/")) it else "file:///android_asset/$it" }
            ?: thumbnailUrl
            ?: directImageUrl
}
