package com.hos.happy.plants.data.repository

import android.content.Context
import com.google.gson.Gson
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.data.model.MasterPlantCatalog
import java.io.InputStreamReader

class PlantMasterRepository(private val context: Context) {

    private val gson = Gson()
    private var cachedPlants: List<MasterPlant>? = null

    fun getAllPlants(): List<MasterPlant> {
        cachedPlants?.let { return it }

        return try {
            context.assets.open("indoor_plants_india.json").use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    val catalog = gson.fromJson(reader, MasterPlantCatalog::class.java)
                    val plants = catalog.plants
                    cachedPlants = plants
                    plants
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
