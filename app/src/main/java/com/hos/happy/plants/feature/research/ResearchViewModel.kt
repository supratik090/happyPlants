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

data class ResearchUiState(
    val activeMode: ResearchMode = ResearchMode.SEARCH,
    val searchQuery: String = "",
    val selectedPlant: MasterPlant? = null,
    val selectedCategory: String = "All",
    val searchResults: List<MasterPlant> = emptyList(),
    val browsePlants: List<MasterPlant> = emptyList(),
    val popularPlants: List<MasterPlant> = emptyList(),
    // Plant Suggestions per Space based on snapshots
    val spaceSuggestions: List<SpacePlantSuggestionItem> = emptyList(),
    val isRefreshingSuggestions: Boolean = false,
    // Plant Identification state
    val isIdentifying: Boolean = false,
    val identificationError: String? = null,
    val identificationResult: PlantIdentificationResult? = null,
    val showApiKeyDialog: Boolean = false,
    val currentApiKey: String = "",
    val remainingDailyIdentifications: Int = 5
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

                // If any room with snapshot is missing from cached suggestions, calculate & save
                val missingRooms = roomsWithSnapshots.filter { it !in cachedRooms }
                if (missingRooms.isNotEmpty()) {
                    val existingGardenPlants = plantDao.getPlantsForProfile(profileId)
                    for (room in missingRooms) {
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
        val popularNames = listOf("Snake Plant", "Money Plant", "ZZ Plant", "Peace Lily", "Areca Palm", "Monstera")
        return allPlants.filter { plant ->
            popularNames.any { plant.commonName.contains(it, ignoreCase = true) }
        }.take(6)
    }

    fun setMode(mode: ResearchMode) {
        _uiState.value = _uiState.value.copy(activeMode = mode)
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

    fun selectCategory(category: String) {
        val filtered = when (category) {
            "Low Light" -> allPlants.filter { (it.lightLux?.minTolerated ?: 0f) < 800f || (it.lightLux?.idealMin ?: 0f) <= 1500f }
            "Moderate Light" -> allPlants.filter { (it.lightLux?.idealMin ?: 0f) in 1000f..3000f }
            "Bright Light" -> allPlants.filter { (it.lightLux?.idealMin ?: 0f) >= 2000f }
            "Low Water" -> allPlants.filter {
                (it.watering?.hotDryMonths?.min ?: 0) >= 10
            }
            else -> allPlants
        }
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            browsePlants = filtered
        )
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
