package com.hos.happy.plants.feature.research

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.data.model.PlantIdentificationResult
import com.hos.happy.plants.data.preferences.UserPreferencesRepository
import com.hos.happy.plants.data.remote.PlantNetApiService
import com.hos.happy.plants.data.repository.PlantIdentificationRepository
import com.hos.happy.plants.data.repository.PlantMasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.hos.happy.plants.data.local.AppDatabase
import com.hos.happy.plants.feature.research.evaluator.RoomPlantSuggestionEvaluator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import org.json.JSONArray

enum class ResearchMode {
    SEARCH,
    BROWSE
}

data class SpacePlantSuggestionItem(
    val roomTag: String,
    val lightLabel: String,
    val tempLabel: String,
    val humidityLabel: String,
    val suggestedPlants: List<MasterPlant>
)

data class PlantFilterCriteria(
    val temperature: String? = null, // "Warm", "Cold", "Moderate"
    val humidity: String? = null,    // "Low", "Moderate", "High"
    val waterCare: String? = null,   // "Low", "Medium", "High"
    val light: String? = null        // "Low", "Moderate", "Bright"
) {
    val isActive: Boolean
        get() = temperature != null || humidity != null || waterCare != null || light != null

    val activeCount: Int
        get() = listOfNotNull(temperature, humidity, waterCare, light).size
}

data class ResearchUiState(
    val activeMode: ResearchMode = ResearchMode.SEARCH,
    val searchQuery: String = "",
    val selectedPlant: MasterPlant? = null,
    val selectedCategory: String = "All",
    val filterCriteria: PlantFilterCriteria = PlantFilterCriteria(),
    val totalPlantCount: Int = 0,
    val searchResults: List<MasterPlant> = emptyList(),
    val browsePlants: List<MasterPlant> = emptyList(),
    val popularPlants: List<MasterPlant> = emptyList(),
    // Plant Suggestions per Space based on snapshots
    val spaceSuggestions: List<SpacePlantSuggestionItem> = emptyList(),
    val isRefreshingSuggestions: Boolean = false,
    val isBrowseLoading: Boolean = false,
    // Plant Identification state
    val isIdentifying: Boolean = false,
    val identificationError: String? = null,
    val identificationResult: PlantIdentificationResult? = null,
    val showApiKeyDialog: Boolean = false,
    val currentApiKey: String = "",
    val remainingDailyIdentifications: Int = 3
)

class ResearchViewModel(application: Application) : AndroidViewModel(application) {

    val masterRepo = PlantMasterRepository(application)
    val idRepo = PlantIdentificationRepository(application)
    private val prefsRepo = UserPreferencesRepository(application)
    private val plantNetApi = PlantNetApiService()
    private val allPlants = masterRepo.getAllPlants()

    private val db = AppDatabase.getInstance(application)
    private val snapshotDao = db.roomEnvSnapshotDao()
    private val roomPlantSuggestionDao = db.roomPlantSuggestionDao()
    private val plantDao = db.plantDao()

    private val _uiState = MutableStateFlow(
        ResearchUiState(
            browsePlants = allPlants,
            totalPlantCount = allPlants.size,
            popularPlants = getInitialPopularPlants()
        )
    )
    val uiState: StateFlow<ResearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val key = prefsRepo.getPlantNetApiKey()
            _uiState.value = _uiState.value.copy(currentApiKey = key)
        }

        viewModelScope.launch {
            idRepo.getRemainingAttemptsFlow().collect { remaining ->
                _uiState.value = _uiState.value.copy(remainingDailyIdentifications = remaining)
            }
        }

        viewModelScope.launch {
            prefsRepo.activeProfileIdFlow.collectLatest { profileId ->
                if (!profileId.isNullOrBlank()) {
                    observeAndSyncSuggestions(profileId)
                }
            }
        }
    }

    private fun observeAndSyncSuggestions(profileId: String) {
        viewModelScope.launch {
            roomPlantSuggestionDao.getAllSuggestionsForProfileFlow(profileId).collectLatest { cachedSuggestions ->
                val roomsWithSnapshots = snapshotDao.getDistinctRooms(profileId)
                val cachedRooms = cachedSuggestions.map { it.roomTag }.toSet()

                // If any room with snapshot is missing from cached suggestions, or has 0 suggestions, calculate & save
                val staleOrMissingRooms = roomsWithSnapshots.filter { room ->
                    val existing = cachedSuggestions.firstOrNull { it.roomTag == room }
                    existing == null || existing.suggestedMasterPlantIdsJson == "[]" || existing.suggestedMasterPlantIdsJson.isBlank()
                }
                if (staleOrMissingRooms.isNotEmpty()) {
                    val existingGardenPlants = plantDao.getPlantsForProfile(profileId)
                    for (room in staleOrMissingRooms) {
                        val latestSnap = snapshotDao.getLatestSnapshotForRoom(profileId, room)
                        if (latestSnap != null) {
                            val entity = RoomPlantSuggestionEvaluator.evaluate(
                                profileId = profileId,
                                roomTag = room,
                                snapshot = latestSnap,
                                allMasterPlants = allPlants,
                                existingGardenPlants = existingGardenPlants
                            )
                            roomPlantSuggestionDao.upsertSuggestion(entity)
                        }
                    }
                }

                // Map only rooms that currently have snapshots to UI items
                val plantMap = allPlants.associateBy { it.id }
                val uiItems = cachedSuggestions
                    .filter { it.roomTag in roomsWithSnapshots }
                    .map { suggestion ->
                        val plantIds = try {
                            val arr = JSONArray(suggestion.suggestedMasterPlantIdsJson)
                            (0 until arr.length()).map { arr.getInt(it) }
                        } catch (e: Exception) {
                            emptyList()
                        }
                        val plants = plantIds.mapNotNull { plantMap[it] }
                            .sortedWith(
                                compareByDescending<MasterPlant> { it.popularityScore }
                                    .thenBy { plantIds.indexOf(it.id) }
                            )
                        SpacePlantSuggestionItem(
                            roomTag = suggestion.roomTag,
                            lightLabel = suggestion.lightLabel,
                            tempLabel = suggestion.tempLabel,
                            humidityLabel = suggestion.humidityLabel,
                            suggestedPlants = plants
                        )
                    }

                _uiState.value = _uiState.value.copy(spaceSuggestions = uiItems)
            }
        }
    }

    fun refreshSuggestions() {
        viewModelScope.launch {
            val profileId = prefsRepo.activeProfileIdFlow.first() ?: return@launch
            _uiState.value = _uiState.value.copy(isRefreshingSuggestions = true)
            try {
                val roomsWithSnapshots = snapshotDao.getDistinctRooms(profileId)
                val existingGardenPlants = plantDao.getPlantsForProfile(profileId)

                for (room in roomsWithSnapshots) {
                    val latestSnap = snapshotDao.getLatestSnapshotForRoom(profileId, room)
                    if (latestSnap != null) {
                        val entity = RoomPlantSuggestionEvaluator.evaluate(
                            profileId = profileId,
                            roomTag = room,
                            snapshot = latestSnap,
                            allMasterPlants = allPlants,
                            existingGardenPlants = existingGardenPlants
                        )
                        roomPlantSuggestionDao.upsertSuggestion(entity)
                    }
                }
            } catch (e: Exception) {
                // Ignore error on refresh
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshingSuggestions = false)
            }
        }
    }

    private fun getInitialPopularPlants(): List<MasterPlant> {
        return allPlants
            .sortedByDescending { it.popularityScore }
            .take(6)
    }

    fun setMode(mode: ResearchMode) {
        if (mode == ResearchMode.BROWSE && _uiState.value.activeMode != ResearchMode.BROWSE) {
            _uiState.value = _uiState.value.copy(activeMode = mode, isBrowseLoading = true)
            viewModelScope.launch {
                kotlinx.coroutines.delay(200)
                _uiState.value = _uiState.value.copy(isBrowseLoading = false)
            }
        } else {
            _uiState.value = _uiState.value.copy(activeMode = mode)
        }
    }

    fun onSearchQueryChanged(query: String) {
        val results = if (query.isBlank()) {
            emptyList()
        } else {
            masterRepo.searchPlants(query)
        }
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            searchResults = results
        )
    }

    fun selectPlant(plant: MasterPlant?) {
        _uiState.value = _uiState.value.copy(
            selectedPlant = plant,
            searchQuery = plant?.commonName ?: _uiState.value.searchQuery,
            searchResults = emptyList() // dismiss dropdown once selected
        )
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            selectedPlant = null
        )
    }

    fun setTemperatureFilter(temp: String?) {
        val current = _uiState.value.filterCriteria.temperature
        val newTemp = if (current == temp) null else temp
        applyFilters(_uiState.value.filterCriteria.copy(temperature = newTemp))
    }

    fun setHumidityFilter(hum: String?) {
        val current = _uiState.value.filterCriteria.humidity
        val newHum = if (current == hum) null else hum
        applyFilters(_uiState.value.filterCriteria.copy(humidity = newHum))
    }

    fun setWaterCareFilter(water: String?) {
        val current = _uiState.value.filterCriteria.waterCare
        val newWater = if (current == water) null else water
        applyFilters(_uiState.value.filterCriteria.copy(waterCare = newWater))
    }

    fun setLightFilter(light: String?) {
        val current = _uiState.value.filterCriteria.light
        val newLight = if (current == light) null else light
        applyFilters(_uiState.value.filterCriteria.copy(light = newLight))
    }

    fun clearAllFilters() {
        applyFilters(PlantFilterCriteria())
    }

    private fun applyFilters(criteria: PlantFilterCriteria) {
        _uiState.value = _uiState.value.copy(filterCriteria = criteria, isBrowseLoading = true)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val filtered = allPlants.filter { plant ->
                // 1. Temperature filter
                val matchTemp = when (criteria.temperature) {
                    "Warm" -> {
                        val t = plant.temperature
                        (t?.idealMax ?: 28f) >= 30f || (t?.tolerableMax ?: 35f) >= 36f
                    }
                    "Cold" -> {
                        val t = plant.temperature
                        (t?.tolerableMin ?: 15f) <= 12f || (t?.idealMin ?: 18f) <= 16f
                    }
                    "Moderate" -> {
                        val t = plant.temperature
                        (t?.idealMin ?: 18f) <= 20f && (t?.idealMax ?: 28f) <= 28f
                    }
                    else -> true
                }

                // 2. Humidity filter
                val matchHum = when (criteria.humidity) {
                    "Low" -> {
                        val h = plant.humidity
                        (h?.min ?: 40f) <= 35f
                    }
                    "Moderate" -> {
                        val h = plant.humidity
                        (h?.min ?: 40f) <= 50f && (h?.max ?: 70f) <= 75f
                    }
                    "High" -> {
                        val h = plant.humidity
                        (h?.max ?: 70f) >= 75f || (h?.min ?: 40f) >= 55f
                    }
                    else -> true
                }

                // 3. Water Care filter
                val matchWater = when (criteria.waterCare) {
                    "Low" -> {
                        val minDays = plant.watering?.hotDryMonths?.min ?: 5
                        val maxDays = plant.watering?.hotDryMonths?.max ?: 7
                        minDays >= 7 || maxDays >= 10
                    }
                    "Medium" -> {
                        val minDays = plant.watering?.hotDryMonths?.min ?: 5
                        val maxDays = plant.watering?.hotDryMonths?.max ?: 7
                        !(minDays >= 7 || maxDays >= 10) && !(minDays <= 3 || maxDays <= 4)
                    }
                    "High" -> {
                        val minDays = plant.watering?.hotDryMonths?.min ?: 5
                        val maxDays = plant.watering?.hotDryMonths?.max ?: 7
                        minDays <= 3 || maxDays <= 4
                    }
                    else -> true
                }

                // 4. Light filter
                val matchLight = when (criteria.light) {
                    "Low" -> (plant.lightLux?.minTolerated ?: 0f) < 800f || (plant.lightLux?.idealMin ?: 0f) <= 1500f
                    "Moderate" -> (plant.lightLux?.idealMin ?: 0f) in 1000f..3000f
                    "Bright" -> (plant.lightLux?.idealMin ?: 0f) >= 2000f
                    else -> true
                }

                matchTemp && matchHum && matchWater && matchLight
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    filterCriteria = criteria,
                    browsePlants = filtered,
                    selectedCategory = if (criteria.isActive) "Filtered (${criteria.activeCount})" else "All",
                    isBrowseLoading = false
                )
            }
        }
    }

    fun selectCategory(category: String) {
        when (category) {
            "All" -> clearAllFilters()
            "Low Light" -> setLightFilter("Low")
            "Moderate Light" -> setLightFilter("Moderate")
            "Bright Light" -> setLightFilter("Bright")
            "Low Water" -> setWaterCareFilter("Low")
            "Medium Water" -> setWaterCareFilter("Medium")
            "High Water" -> setWaterCareFilter("High")
            "Warm" -> setTemperatureFilter("Warm")
            "Cold" -> setTemperatureFilter("Cold")
            "Moderate Temp" -> setTemperatureFilter("Moderate")
            "Low Humidity" -> setHumidityFilter("Low")
            "Moderate Humidity" -> setHumidityFilter("Moderate")
            "High Humidity" -> setHumidityFilter("High")
            else -> clearAllFilters()
        }
    }

    /**
     * PlantNet Live Image Identification with daily 5-attempt limit reset at 12:00 AM IST
     */
    fun identifyPlant(bitmap: Bitmap) {
        viewModelScope.launch {
            if (!idRepo.canAttemptIdentification()) {
                _uiState.value = _uiState.value.copy(
                    identificationError = "Daily photo identification limit reached (5/5 used). Resets at 12:00 AM IST."
                )
                return@launch
            }

            val apiKey = prefsRepo.getPlantNetApiKey()
            if (apiKey.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    showApiKeyDialog = true,
                    identificationError = "Please enter your Pl@ntNet API Key to identify plants."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isIdentifying = true,
                identificationError = null,
                identificationResult = null
            )

            // Persist attempt to Room DB
            idRepo.recordAttempt()
            com.hos.happy.plants.data.telemetry.FirebaseTelemetryManager.getInstance()?.recordPlantIdentityCall()

            val result = plantNetApi.identifyPlant(
                bitmap = bitmap,
                apiKey = apiKey,
                localCatalog = allPlants
            )

            result.fold(
                onSuccess = { identifiedPlant ->
                    _uiState.value = _uiState.value.copy(
                        isIdentifying = false,
                        identificationResult = identifiedPlant,
                        identificationError = null
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isIdentifying = false,
                        identificationError = err.localizedMessage ?: "Failed to identify plant. Please try again."
                    )
                }
            )
        }
    }

    fun clearIdentification() {
        _uiState.value = _uiState.value.copy(
            identificationResult = null,
            identificationError = null,
            isIdentifying = false
        )
    }

    fun showApiKeyDialog() {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = true)
    }

    fun dismissApiKeyDialog() {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = false)
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            prefsRepo.setPlantNetApiKey(key)
            _uiState.value = _uiState.value.copy(
                currentApiKey = key,
                showApiKeyDialog = false,
                identificationError = null
            )
        }
    }
}
