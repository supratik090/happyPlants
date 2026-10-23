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

enum class PlantActivityType(
    val title: String,
    val icon: String,
    val isEssentialByDefault: Boolean,
    val goldenRule: String
) {
    WATER(
        title = "Watering",
        icon = "💧",
        isEssentialByDefault = true,
        goldenRule = "Water only when the top 2–3 cm of soil is dry to prevent root rot."
    ),
    ROTATE(
        title = "Rotate Pot",
        icon = "🔄",
        isEssentialByDefault = false,
        goldenRule = "Turn the pot a quarter turn weekly so it grows evenly toward light."
    ),
    FEED(
        title = "Feeding",
        icon = "🌱",
        isEssentialByDefault = false,
        goldenRule = "Manure or diluted fertiliser every 30–90 days during growing season."
    ),
    INSPECT(
        title = "Pest Inspection",
        icon = "🔍",
        isEssentialByDefault = false,
        goldenRule = "Weekly look under leaves and at stems catches problems while easy to fix."
    ),
    CLEAN(
        title = "Leaf Cleaning",
        icon = "✨",
        isEssentialByDefault = false,
        goldenRule = "Wipe dust off leaves to open pores and maximize sunlight absorption."
    ),
    PRUNE(
        title = "Remove Dead Leaves",
        icon = "✂️",
        isEssentialByDefault = false,
        goldenRule = "Pruning yellow/brown leaves stops disease spread and saves energy."
    ),
    REPOT(
        title = "Repotting",
        icon = "🪴",
        isEssentialByDefault = false,
        goldenRule = "Every 1–2 years so roots have room and soil remains fresh."
    )
}

data class PlantActivityStatus(
    val activity: PlantActivityType,
    val intervalDays: Int,
    val daysElapsed: Int,
    val daysRemaining: Int,
    val isDueNow: Boolean,
    val isOverdue: Boolean,
    val overdueDays: Int,
    val lastDoneAt: Long?
)

data class TodayCareTask(
    val plant: PlantEntity,
    val activity: PlantActivityType,
    val isEssential: Boolean,       // Essential vs Good to Have
    val urgencyDays: Int,           // e.g. 0 = due today, >0 = overdue by N days
    val isOverdue: Boolean,
    val goldenRule: String
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

    // Today's due and overdue tasks (classified into Essential & Good to Have)
    val todayTasks: StateFlow<List<TodayCareTask>> = allPlants.map { plants ->
        val now = System.currentTimeMillis()
        val oneDayMillis = 86_400_000L
        val tasks = mutableListOf<TodayCareTask>()

        for (plant in plants) {
            // 1. Watering: most important, not done on Day 1
            val lastWater = plant.lastWateredAt
            if (lastWater == null) {
                // Day 1: Watering is due immediately as Essential!
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        activity = PlantActivityType.WATER,
                        isEssential = true,
                        urgencyDays = 0,
                        isOverdue = false,
                        goldenRule = PlantActivityType.WATER.goldenRule
                    )
                )
            } else {
                val daysSinceWater = ((now - lastWater) / oneDayMillis).toInt()
                if (daysSinceWater >= plant.wateringIntervalDays) {
                    val overdue = daysSinceWater - plant.wateringIntervalDays
                    tasks.add(
                        TodayCareTask(
                            plant = plant,
                            activity = PlantActivityType.WATER,
                            isEssential = true,
                            urgencyDays = overdue,
                            isOverdue = overdue > 0,
                            goldenRule = PlantActivityType.WATER.goldenRule
                        )
                    )
                }
            }

            // 2. Rotate Pot (Weekly, 7 days)
            val lastRotate = plant.lastRotatedAt ?: plant.createdAt
            val daysSinceRotate = ((now - lastRotate) / oneDayMillis).toInt()
            if (daysSinceRotate >= plant.rotateIntervalDays) {
                val overdue = daysSinceRotate - plant.rotateIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        activity = PlantActivityType.ROTATE,
                        isEssential = overdue >= 3,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0,
                        goldenRule = PlantActivityType.ROTATE.goldenRule
                    )
                )
            }

            // 3. Pest Inspection (Weekly, 7 days)
            val lastInspect = plant.lastInspectedAt ?: plant.createdAt
            val daysSinceInspect = ((now - lastInspect) / oneDayMillis).toInt()
            if (daysSinceInspect >= plant.inspectIntervalDays) {
                val overdue = daysSinceInspect - plant.inspectIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        activity = PlantActivityType.INSPECT,
                        isEssential = overdue >= 3,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0,
                        goldenRule = PlantActivityType.INSPECT.goldenRule
                    )
                )
            }

            // 4. Feeding (30-90 days)
            val lastFert = plant.lastFertilizedAt ?: plant.createdAt
            val daysSinceFert = ((now - lastFert) / oneDayMillis).toInt()
            if (daysSinceFert >= plant.fertilizeIntervalDays) {
                val overdue = daysSinceFert - plant.fertilizeIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        activity = PlantActivityType.FEED,
                        isEssential = overdue >= 14,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0,
                        goldenRule = PlantActivityType.FEED.goldenRule
                    )
                )
            }

            // 5. Leaf Cleaning (14 days)
            val lastClean = plant.lastCleanedAt ?: plant.createdAt
            val daysSinceClean = ((now - lastClean) / oneDayMillis).toInt()
            if (daysSinceClean >= plant.cleanIntervalDays) {
                val overdue = daysSinceClean - plant.cleanIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        activity = PlantActivityType.CLEAN,
                        isEssential = false,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0,
                        goldenRule = PlantActivityType.CLEAN.goldenRule
                    )
                )
            }

            // 6. Removing Dead Leaves / Pruning (14 days)
            val lastPrune = plant.lastPrunedAt ?: plant.createdAt
            val daysSincePrune = ((now - lastPrune) / oneDayMillis).toInt()
            if (daysSincePrune >= plant.pruneIntervalDays) {
                val overdue = daysSincePrune - plant.pruneIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        activity = PlantActivityType.PRUNE,
                        isEssential = false,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0,
                        goldenRule = PlantActivityType.PRUNE.goldenRule
                    )
                )
            }

            // 7. Repotting (1-2 years)
            val lastRepot = plant.lastRepottedAt ?: plant.createdAt
            val daysSinceRepot = ((now - lastRepot) / oneDayMillis).toInt()
            if (daysSinceRepot >= plant.repotIntervalDays) {
                val overdue = daysSinceRepot - plant.repotIntervalDays
                tasks.add(
                    TodayCareTask(
                        plant = plant,
                        activity = PlantActivityType.REPOT,
                        isEssential = overdue >= 30,
                        urgencyDays = overdue,
                        isOverdue = overdue > 0,
                        goldenRule = PlantActivityType.REPOT.goldenRule
                    )
                )
            }
        }

        // Sort: Essential first, then overdue, then urgency
        tasks.sortedWith(
            compareByDescending<TodayCareTask> { it.isEssential }
                .thenByDescending { it.isOverdue }
                .thenByDescending { it.urgencyDays }
        )
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
        activity: PlantActivityType,
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
                careType = activity.name,
                timestamp = now,
                remark = remark,
                photoUri = photoUri
            )
            careLogDao.insertLog(log)

            // 2. Update plant's specific activity timestamp
            when (activity) {
                PlantActivityType.WATER -> plantDao.updateLastWatered(plant.id, now)
                PlantActivityType.ROTATE -> plantDao.updateLastRotated(plant.id, now)
                PlantActivityType.FEED -> plantDao.updateLastFertilized(plant.id, now)
                PlantActivityType.INSPECT -> plantDao.updateLastInspected(plant.id, now)
                PlantActivityType.CLEAN -> plantDao.updateLastCleaned(plant.id, now)
                PlantActivityType.PRUNE -> plantDao.updateLastPruned(plant.id, now)
                PlantActivityType.REPOT -> plantDao.updateLastRepotted(plant.id, now)
            }

            // 3. If photo attached, add to Plant Photo Journey
            if (!photoUri.isNullOrBlank()) {
                val photo = PlantPhotoEntity(
                    plantId = plant.id,
                    profileId = profileId,
                    photoUri = photoUri,
                    capturedAt = now,
                    caption = remark ?: "${activity.title} completed",
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
            val now = System.currentTimeMillis()

            // AT DAY 1: Assume all activities are done apart from watering!
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
                rotateIntervalDays = 7,
                inspectIntervalDays = 7,
                cleanIntervalDays = 14,
                pruneIntervalDays = 14,
                lastWateredAt = null, // DAY 1: Watering is due!
                lastFertilizedAt = now,
                lastSoilRefreshedAt = now,
                lastRepottedAt = now,
                lastRotatedAt = now,
                lastInspectedAt = now,
                lastCleanedAt = now,
                lastPrunedAt = now,
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
                        capturedAt = now,
                        caption = "Added to Garden (${plant.roomTag})"
                    )
                )
            }

            closeAddPlantDialog()
        }
    }

    fun calculatePlantActivities(plant: PlantEntity): List<PlantActivityStatus> {
        val now = System.currentTimeMillis()
        val oneDay = 86_400_000L

        fun calc(activity: PlantActivityType, intervalDays: Int, lastDone: Long?, isWater: Boolean = false): PlantActivityStatus {
            if (isWater && lastDone == null) {
                return PlantActivityStatus(
                    activity = activity,
                    intervalDays = intervalDays,
                    daysElapsed = 0,
                    daysRemaining = 0,
                    isDueNow = true,
                    isOverdue = false,
                    overdueDays = 0,
                    lastDoneAt = null
                )
            }
            val doneTime = lastDone ?: plant.createdAt
            val daysElapsed = ((now - doneTime) / oneDay).toInt()
            val remaining = intervalDays - daysElapsed
            val isDue = remaining <= 0
            val overdue = if (remaining < 0) -remaining else 0

            return PlantActivityStatus(
                activity = activity,
                intervalDays = intervalDays,
                daysElapsed = daysElapsed,
                daysRemaining = remaining,
                isDueNow = isDue,
                isOverdue = overdue > 0,
                overdueDays = overdue,
                lastDoneAt = lastDone
            )
        }

        return listOf(
            calc(PlantActivityType.WATER, plant.wateringIntervalDays, plant.lastWateredAt, isWater = true),
            calc(PlantActivityType.ROTATE, plant.rotateIntervalDays, plant.lastRotatedAt ?: plant.createdAt),
            calc(PlantActivityType.FEED, plant.fertilizeIntervalDays, plant.lastFertilizedAt ?: plant.createdAt),
            calc(PlantActivityType.INSPECT, plant.inspectIntervalDays, plant.lastInspectedAt ?: plant.createdAt),
            calc(PlantActivityType.CLEAN, plant.cleanIntervalDays, plant.lastCleanedAt ?: plant.createdAt),
            calc(PlantActivityType.PRUNE, plant.pruneIntervalDays, plant.lastPrunedAt ?: plant.createdAt),
            calc(PlantActivityType.REPOT, plant.repotIntervalDays, plant.lastRepottedAt ?: plant.createdAt)
        )
    }

    fun getPhotosForPlantFlow(plantId: String) = photoDao.getPhotosForPlantFlow(plantId)
    fun getCareLogsForPlantFlow(plantId: String) = careLogDao.getLogsForPlantFlow(plantId)

    fun getCommonProblemsForPlant(plant: PlantEntity): List<CommonProblem> {
        val masterId = plant.masterPlantId ?: return emptyList()
        return masterRepo.getPlantById(masterId)?.commonProblems ?: emptyList()
    }
}
