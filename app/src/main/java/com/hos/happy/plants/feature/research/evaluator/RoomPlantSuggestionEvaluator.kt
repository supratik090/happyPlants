package com.hos.happy.plants.feature.research.evaluator

import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import com.hos.happy.plants.data.local.entity.RoomPlantSuggestionEntity
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.feature.env.components.SnapshotLabelEvaluator
import org.json.JSONArray

object RoomPlantSuggestionEvaluator {

    fun evaluate(
        profileId: String,
        roomTag: String,
        snapshot: RoomEnvSnapshotEntity,
        allMasterPlants: List<MasterPlant>,
        existingGardenPlants: List<PlantEntity>
    ): RoomPlantSuggestionEntity {
        val lightLabel = SnapshotLabelEvaluator.evaluateLight(snapshot.lightLux).text
        val tempLabel = SnapshotLabelEvaluator.evaluateTemperature(snapshot.temperatureCelsius).text
        val humLabel = SnapshotLabelEvaluator.evaluateHumidity(snapshot.humidityPercentage).text

        // Exclude plants already added to the garden
        val existingMasterIds = existingGardenPlants.mapNotNull { it.masterPlantId }.toSet()
        val existingCommonNames = existingGardenPlants.map { it.commonName.trim().lowercase() }.toSet()
        val existingBotanicalNames = existingGardenPlants.map { it.botanicalName.trim().lowercase() }.toSet()

        val candidatePlants = allMasterPlants.filter { mp ->
            mp.id !in existingMasterIds &&
            mp.commonName.trim().lowercase() !in existingCommonNames &&
            mp.botanicalName.trim().lowercase() !in existingBotanicalNames
        }

        val scoredPlants = candidatePlants.map { plant ->
            val score = scorePlantMatch(plant, snapshot.lightLux, snapshot.temperatureCelsius, snapshot.humidityPercentage)
            Pair(plant, score)
        }

        // Filter plants that are suitable for this space (score >= 35, fallback to >= 25, then any)
        val suitablePlants = scoredPlants.filter { it.second >= 35 }
            .ifEmpty { scoredPlants.filter { it.second >= 25 } }
            .ifEmpty { scoredPlants }

        // Sort suitable plants by popularity score first (highest first), then by environmental match score
        val topSuggestions = suitablePlants
            .sortedWith(
                compareByDescending<Pair<MasterPlant, Int>> { it.first.popularityScore }
                    .thenByDescending { it.second }
            )
            .take(5)
            .map { it.first }
        val idsJson = JSONArray(topSuggestions.map { it.id }).toString()

        return RoomPlantSuggestionEntity(
            id = "${profileId}_${roomTag}",
            profileId = profileId,
            roomTag = roomTag,
            calculatedAt = System.currentTimeMillis(),
            snapshotId = snapshot.id,
            suggestedMasterPlantIdsJson = idsJson,
            lightLabel = lightLabel,
            tempLabel = tempLabel,
            humidityLabel = humLabel
        )
    }

    private fun scorePlantMatch(
        plant: MasterPlant,
        lux: Float,
        tempC: Float?,
        hum: Float?
    ): Int {
        val lightInfo = plant.lightLux
        val minTol = lightInfo?.minTolerated ?: 300f
        val maxTol = lightInfo?.maxTolerated ?: 25000f
        val idealMin = lightInfo?.idealMin ?: minTol
        val idealMax = lightInfo?.idealMax ?: maxTol

        // --- 1. LIGHT SCORE (0 - 50 points) ---
        // Light is the #1 driving factor for indoor plant survival.
        val lightScore = when {
            lux in idealMin..idealMax -> 50
            lux in minTol..maxTol -> 40
            lux < minTol -> {
                val ratio = (lux / minTol).coerceIn(0f, 1f)
                if (minTol <= 400f) {
                    // Naturally shade / low-light tolerant species (e.g. ZZ, Snake, Pothos, Chinese Evergreen)
                    // Even in low light (100 - 300 lux), these plants adapt better than any other.
                    (20 + (18 * ratio)).toInt()
                } else {
                    // Plants needing medium/bright light get penalized progressively as light drops
                    (5 + (15 * ratio)).toInt()
                }
            }
            else -> {
                // lux > maxTol: too much light / scorch risk
                val excessRatio = ((lux - maxTol) / maxTol).coerceIn(0f, 1f)
                (35 - (20 * excessRatio)).toInt().coerceAtLeast(5)
            }
        }

        // --- 2. TEMPERATURE SCORE (0 - 25 points) ---
        val tempScore = if (tempC != null && plant.temperature != null) {
            val tTolMin = plant.temperature.tolerableMin ?: 10f
            val tTolMax = plant.temperature.tolerableMax ?: 35f
            val tIdealMin = plant.temperature.idealMin ?: tTolMin
            val tIdealMax = plant.temperature.idealMax ?: tTolMax

            when {
                tempC in tIdealMin..tIdealMax -> 25
                tempC in tTolMin..tTolMax -> 20
                tempC > tTolMax -> {
                    val diff = tempC - tTolMax
                    (20 - (diff * 2.5f)).toInt().coerceIn(5, 20)
                }
                else -> {
                    val diff = tTolMin - tempC
                    (20 - (diff * 2.5f)).toInt().coerceIn(5, 20)
                }
            }
        } else {
            18 // Neutral default
        }

        // --- 3. HUMIDITY SCORE (0 - 25 points) ---
        val humScore = if (hum != null && plant.humidity != null) {
            val hMin = plant.humidity.min ?: 30f
            val hMax = plant.humidity.max ?: 70f

            when {
                hum in hMin..hMax -> 25
                hum < hMin -> {
                    val diff = hMin - hum
                    (22 - (diff * 0.9f)).toInt().coerceIn(5, 22)
                }
                else -> {
                    val diff = hum - hMax
                    (22 - (diff * 0.7f)).toInt().coerceIn(5, 22)
                }
            }
        } else {
            18 // Neutral default
        }

        return lightScore + tempScore + humScore
    }
}
