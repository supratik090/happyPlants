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

        val scoredPlants = candidatePlants.mapNotNull { plant ->
            val score = scorePlantMatch(plant, snapshot.lightLux, snapshot.temperatureCelsius, snapshot.humidityPercentage)
            if (score != null && score >= 40) {
                Pair(plant, score)
            } else {
                null
            }
        }.sortedByDescending { it.second }

        // Top suggestions limited to up to 5
        val topSuggestions = scoredPlants.take(5).map { it.first }
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
    ): Int? {
        val lightInfo = plant.lightLux ?: return null
        val minTol = lightInfo.minTolerated ?: 200f
        val maxTol = lightInfo.maxTolerated ?: 30000f
        val idealMin = lightInfo.idealMin ?: minTol
        val idealMax = lightInfo.idealMax ?: maxTol

        // Light check: must be tolerably close to survivable range
        if (lux < minTol * 0.8f || lux > maxTol * 1.25f) {
            return null // Incompatible light condition
        }

        var score = 0

        // Light Score (0 - 50)
        score += when {
            lux in idealMin..idealMax -> 50
            lux in minTol..maxTol -> 35
            else -> 20
        }

        // Temperature Score (0 - 25)
        if (tempC != null && plant.temperature != null) {
            val tTolMin = plant.temperature.tolerableMin ?: 10f
            val tTolMax = plant.temperature.tolerableMax ?: 38f
            val tIdealMin = plant.temperature.idealMin ?: tTolMin
            val tIdealMax = plant.temperature.idealMax ?: tTolMax

            if (tempC < tTolMin - 3f || tempC > tTolMax + 3f) {
                return null // Incompatible temperature
            }

            score += when {
                tempC in tIdealMin..tIdealMax -> 25
                tempC in tTolMin..tTolMax -> 15
                else -> 10
            }
        } else {
            score += 20 // Neutral if not recorded
        }

        // Humidity Score (0 - 25)
        if (hum != null && plant.humidity != null) {
            val hMin = plant.humidity.min ?: 30f
            val hMax = plant.humidity.max ?: 70f

            if (hum < hMin - 18f || hum > hMax + 25f) {
                return null // Incompatible humidity
            }

            score += when {
                hum in hMin..hMax -> 25
                hum in (hMin - 10f)..(hMax + 12f) -> 18
                else -> 10
            }
        } else {
            score += 20 // Neutral if not recorded
        }

        return score
    }
}
