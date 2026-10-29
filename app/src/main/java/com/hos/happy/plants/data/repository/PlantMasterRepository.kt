package com.hos.happy.plants.data.repository

import android.content.Context
import com.google.gson.Gson
import com.hos.happy.plants.data.local.AppDatabase
import com.hos.happy.plants.data.local.entity.MasterPlantEntity
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.data.model.MasterPlantCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class PlantMasterRepository(private val context: Context) {

    private val gson = Gson()
    private val database = AppDatabase.getInstance(context)
    private val masterPlantDao = database.masterPlantDao()
    private var cachedPlants: List<MasterPlant>? = null

    init {
        // Automatically check and seed Room DB in the background on startup/install
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    fun getAllPlants(): List<MasterPlant> {
        cachedPlants?.let { return it }

        return try {
            val plants = loadPlantsFromAssets()
            cachedPlants = plants
            plants
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun loadPlantsFromAssets(): List<MasterPlant> {
        val fileName = "indian_v1.json"
        val allPlants = mutableListOf<MasterPlant>()
        try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    val jsonElement = com.google.gson.JsonParser.parseReader(reader)
                    if (jsonElement.isJsonObject) {
                        val catalog = gson.fromJson(jsonElement, MasterPlantCatalog::class.java)
                        allPlants.addAll(catalog.plants)
                    } else if (jsonElement.isJsonArray) {
                        val type = object : com.google.gson.reflect.TypeToken<List<MasterPlant>>() {}.type
                        val list: List<MasterPlant> = gson.fromJson(jsonElement, type)
                        allPlants.addAll(list)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return allPlants.distinctBy { it.id }
    }

    suspend fun seedDatabaseIfEmpty(): List<MasterPlant> = withContext(Dispatchers.IO) {
        try {
            val plants = loadPlantsFromAssets()
            val count = masterPlantDao.getPlantCount()
            if (count < plants.size) {
                val entities = plants.map { plant ->
                    MasterPlantEntity(
                        id = plant.id,
                        commonName = plant.commonName,
                        botanicalName = plant.botanicalName,
                        localNamesJson = gson.toJson(plant.localNames ?: emptyMap<String, String>()),
                        photoUri = plant.displayImageUrl,
                        wateringIntervalSummerDays = plant.watering?.hotDryMonths?.min ?: 7,
                        wateringIntervalWinterDays = plant.watering?.monsoonAndWinter?.min ?: 10,
                        wateringNote = plant.watering?.note,
                        lightIdealMinLux = plant.lightLux?.idealMin,
                        lightIdealMaxLux = plant.lightLux?.idealMax,
                        tempIdealMinC = plant.temperature?.idealMin,
                        tempIdealMaxC = plant.temperature?.idealMax,
                        humidityMinPercent = plant.humidity?.min,
                        humidityMaxPercent = plant.humidity?.max,
                        soilSimpleWords = plant.soil?.inSimpleWords,
                        soilMixRecipe = plant.soil?.mixRecipe,
                        fertilizeEveryDays = plant.fertilizeEveryDays ?: 60,
                        refreshSoilEveryDays = plant.refreshTopSoilEveryDays ?: 180,
                        repotEveryDays = plant.repotEveryDays ?: 730,
                        rawJson = gson.toJson(plant)
                    )
                }
                masterPlantDao.insertAll(entities)
                cachedPlants = plants
                plants
            } else {
                val entities = masterPlantDao.getAllPlants()
                val plants = entities.mapNotNull { entity ->
                    try {
                        gson.fromJson(entity.rawJson, MasterPlant::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (plants.isNotEmpty()) {
                    cachedPlants = plants
                    plants
                } else {
                    loadPlantsFromAssets()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadPlantsFromAssets()
        }
    }

    suspend fun getAllPlantsFromDb(): List<MasterPlant> = withContext(Dispatchers.IO) {
        val entities = masterPlantDao.getAllPlants()
        if (entities.isEmpty()) {
            seedDatabaseIfEmpty()
        } else {
            entities.mapNotNull { entity ->
                try {
                    gson.fromJson(entity.rawJson, MasterPlant::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun getAllPlantsFlow(): Flow<List<MasterPlant>> {
        return masterPlantDao.getAllPlantsFlow().map { entities ->
            entities.mapNotNull { entity ->
                try {
                    gson.fromJson(entity.rawJson, MasterPlant::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun getPlantById(id: Int): MasterPlant? {
        return getAllPlants().find { it.id == id }
    }

    fun searchPlants(query: String): List<MasterPlant> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return getAllPlants()

        return getAllPlants().filter { plant ->
            plant.commonName.lowercase().contains(q) ||
                    plant.botanicalName.lowercase().contains(q) ||
                    plant.localNames?.values?.any { it.lowercase().contains(q) } == true
        }
    }
}
