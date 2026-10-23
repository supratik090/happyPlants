package com.hos.happy.plants.feature.garden

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hos.happy.plants.data.local.AppDatabase
import com.hos.happy.plants.data.local.entity.PlantCareLogEntity
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.data.local.entity.PlantPhotoEntity
import com.hos.happy.plants.data.model.CommonProblem
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.data.preferences.UserPreferencesRepository
import com.hos.happy.plants.data.repository.PlantMasterRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class GardenTab {
    TODAY,
    GARDEN
}

enum class PlantCareType(val title: String, val icon: String) {
    WATER("Water", "💧"),
    FERTILIZE("Fertilize", "🌱"),
    SOIL_REFRESH("Soil Refresh", "🪵"),
    REPOT("Repot", "🪴")
}

data class TodayCareTask(
    val plant: PlantEntity,
    val careType: PlantCareType,
    val urgencyDays: Int, // e.g. 0 = due today, >0 = overdue by N days
    val isOverdue: Boolean
)

data class GardenUiState(
    val activeTab: GardenTab = GardenTab.TODAY,
    val currentProfileId: String? = null,
    val selectedRoomFilter: String = "All",
    val expandedPlantId: String? = null,
    val activeDoctorPlant: PlantEntity? = null,
    val activeJourneyPlant: PlantEntity? = null,
    val showAddPlantDialog: Boolean = false,
    val actionDialogTask: TodayCareTask? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class GardenViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val plantDao = db.plantDao()
    private val careLogDao = db.plantCareLogDao()
    private val photoDao = db.plantPhotoDao()
    private val prefs = UserPreferencesRepository(application)
    val masterRepo = PlantMasterRepository(application)

    private val _uiState = MutableStateFlow(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    // Room tags: default 4 + custom
    val defaultRoomTags = listOf("Living", "Bed", "Bathroom", "Balcony")
    val availableRoomTags: StateFlow<List<String>> = prefs.customRoomTagsFlow.map { customTags ->
        (defaultRoomTags + customTags).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultRoomTags)

    init {
        viewModelScope.launch {
            prefs.activeProfileIdFlow.collect { profileId ->
                _uiState.value = _uiState.value.copy(currentProfileId = profileId)
            }
        }
    }

    // Plants for current profile
    val allPlants: StateFlow<List<PlantEntity>> = _uiState.flatMapLatest { state ->
        val profileId = state.currentProfileId ?: return@flatMapLatest flowOf(emptyList())
        plantDao.getPlantsForProfileFlow(profileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's due and overdue tasks
    val todayTasks: StateFlow<List<TodayCareTask>> = allPlants.map { plants ->
        val now = System.currentTimeMillis()
        val oneDayMillis = 86_400_000L
        val tasks = mutableListOf<TodayCareTask>()

        for (plant in plants) {
            // 1. Water Check
            val lastWater = plant.lastWateredAt ?: plant.createdAt
            val daysSinceWater = ((now - lastWater) / oneDayMillis).toInt()
            if (daysSinceWater >= plant.wateringIntervalDays) {
                val overdue = daysSinceWater - plant.wateringIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        careType = PlantCareType.WATER,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0
                    )
                )
            }

            // 2. Fertilize Check
            val lastFert = plant.lastFertilizedAt ?: plant.createdAt
            val daysSinceFert = ((now - lastFert) / oneDayMillis).toInt()
            if (daysSinceFert >= plant.fertilizeIntervalDays) {
                val overdue = daysSinceFert - plant.fertilizeIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        careType = PlantCareType.FERTILIZE,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0
                    )
                )
            }

            // 3. Soil Refresh Check
            val lastSoil = plant.lastSoilRefreshedAt ?: plant.createdAt
            val daysSinceSoil = ((now - lastSoil) / oneDayMillis).toInt()
            if (daysSinceSoil >= plant.refreshSoilIntervalDays) {
                val overdue = daysSinceSoil - plant.refreshSoilIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        careType = PlantCareType.SOIL_REFRESH,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0
                    )
                )
            }

            // 4. Repot Check
            val lastRepot = plant.lastRepottedAt ?: plant.createdAt
            val daysSinceRepot = ((now - lastRepot) / oneDayMillis).toInt()
            if (daysSinceRepot >= plant.repotIntervalDays) {
                val overdue = daysSinceRepot - plant.repotIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        careType = PlantCareType.REPOT,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0
                    )
                )
            }
        }

        // Sort: overdue first, then by urgency
        tasks.sortedWith(compareByDescending<TodayCareTask> { it.isOverdue }.thenByDescending { it.urgencyDays })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered plants for Garden Tab
    val filteredGardenPlants: StateFlow<List<PlantEntity>> = combine(allPlants, _uiState) { plants, state ->
        if (state.selectedRoomFilter == "All") {
            plants
        } else {
            plants.filter { it.roomTag.equals(state.selectedRoomFilter, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: GardenTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun selectRoomFilter(room: String) {
        _uiState.value = _uiState.value.copy(selectedRoomFilter = room)
    }

    fun togglePlantExpanded(plantId: String) {
        val current = _uiState.value.expandedPlantId
        _uiState.value = _uiState.value.copy(
            expandedPlantId = if (current == plantId) null else plantId
        )
    }

    fun openCareDoctor(plant: PlantEntity) {
        _uiState.value = _uiState.value.copy(activeDoctorPlant = plant)
    }

    fun closeCareDoctor() {
        _uiState.value = _uiState.value.copy(activeDoctorPlant = null)
    }

    fun openPlantJourney(plant: PlantEntity) {
        _uiState.value = _uiState.value.copy(activeJourneyPlant = plant)
    }

    fun closePlantJourney() {
        _uiState.value = _uiState.value.copy(activeJourneyPlant = null)
    }

    fun openAddPlantDialog() {
        _uiState.value = _uiState.value.copy(showAddPlantDialog = true)
    }

    fun closeAddPlantDialog() {
        _uiState.value = _uiState.value.copy(showAddPlantDialog = false)
    }

    fun openActionDialog(task: TodayCareTask) {
        _uiState.value = _uiState.value.copy(actionDialogTask = task)
    }

    fun closeActionDialog() {
        _uiState.value = _uiState.value.copy(actionDialogTask = null)
    }

    fun addCustomRoom(roomName: String) {
        if (roomName.isNotBlank()) {
            viewModelScope.launch {
                prefs.addCustomRoomTag(roomName.trim())
            }
        }
    }

    fun logCareAction(
        plant: PlantEntity,
        careType: PlantCareType,
        remark: String?,
        photoUri: String?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val profileId = _uiState.value.currentProfileId ?: return@launch

            // 1. Log Care Event
            val log = PlantCareLogEntity(
                plantId = plant.id,
                profileId = profileId,
                careType = careType.name,
                timestamp = now,
                remark = remark,
                photoUri = photoUri
            )
            careLogDao.insertLog(log)

            // 2. Update plant last action time
            when (careType) {
                PlantCareType.WATER -> plantDao.updateLastWatered(plant.id, now)
                PlantCareType.FERTILIZE -> plantDao.updateLastFertilized(plant.id, now)
                PlantCareType.SOIL_REFRESH -> plantDao.updateLastSoilRefreshed(plant.id, now)
                PlantCareType.REPOT -> plantDao.updateLastRepotted(plant.id, now)
            }

            // 3. If photo attached, add to Plant Photo Journey
            if (!photoUri.isNullOrBlank()) {
                val photo = PlantPhotoEntity(
                    plantId = plant.id,
                    profileId = profileId,
                    photoUri = photoUri,
                    capturedAt = now,
                    caption = remark ?: "${careType.title} recorded",
                    careLogId = log.id
                )
                photoDao.insertPhoto(photo)
            }

            closeActionDialog()
        }
    }

    fun createPlant(
        nickname: String,
        commonName: String,
        botanicalName: String,
        roomTag: String,
        masterPlant: MasterPlant?,
        customPhotoUri: String?,
        wateringDays: Int,
        fertilizeDays: Int,
        refreshSoilDays: Int,
        repotDays: Int,
        notes: String?
    ) {
        viewModelScope.launch {
            val profileId = _uiState.value.currentProfileId ?: return@launch
            val photo = customPhotoUri ?: masterPlant?.image?.directImageUrl ?: masterPlant?.image?.thumbnailUrl

            val plant = PlantEntity(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                masterPlantId = masterPlant?.id,
                nickname = nickname.ifBlank { commonName },
                commonName = commonName,
                botanicalName = botanicalName,
                roomTag = roomTag,
                photoUri = photo,
                wateringIntervalDays = wateringDays,
                fertilizeIntervalDays = fertilizeDays,
                refreshSoilIntervalDays = refreshSoilDays,
                repotIntervalDays = repotDays,
                lastWateredAt = System.currentTimeMillis(), // Initialized as freshly cared
                lastFertilizedAt = System.currentTimeMillis(),
                lastSoilRefreshedAt = System.currentTimeMillis(),
                lastRepottedAt = System.currentTimeMillis(),
                lightIdealMinLux = masterPlant?.lightLux?.idealMin,
                lightIdealMaxLux = masterPlant?.lightLux?.idealMax,
                soilSimpleWords = masterPlant?.soil?.inSimpleWords,
                soilMixRecipe = masterPlant?.soil?.mixRecipe,
                petSafetyNote = masterPlant?.petSafety?.note,
                notes = notes
            )
            plantDao.insertPlant(plant)

            // Seed first photo for journey if image exists
            if (!photo.isNullOrBlank()) {
                photoDao.insertPhoto(
                    PlantPhotoEntity(
                        plantId = plant.id,
                        profileId = profileId,
                        photoUri = photo,
                        capturedAt = System.currentTimeMillis(),
                        caption = "Added to Garden (${plant.roomTag})"
                    )
                )
            }

            closeAddPlantDialog()
        }
    }

    fun getPhotosForPlantFlow(plantId: String) = photoDao.getPhotosForPlantFlow(plantId)
    fun getCareLogsForPlantFlow(plantId: String) = careLogDao.getLogsForPlantFlow(plantId)

    fun getCommonProblemsForPlant(plant: PlantEntity): List<CommonProblem> {
        val masterId = plant.masterPlantId ?: return emptyList()
        return masterRepo.getPlantById(masterId)?.commonProblems ?: emptyList()
    }
}
