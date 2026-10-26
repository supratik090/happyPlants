package com.hos.happy.plants.data.repository

import android.content.Context
import com.hos.happy.plants.data.local.AppDatabase
import com.hos.happy.plants.data.local.entity.DailyCareTaskEntity
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.feature.garden.PlantActivityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class TaskRuleEngine(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val taskDao = db.dailyCareTaskDao()
    private val plantDao = db.plantDao()
    private val snapshotDao = db.roomEnvSnapshotDao()
    private val masterDao = db.masterPlantDao()
    private val photoDao = db.plantPhotoDao()

    companion object {
        private val istTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = istTimeZone
        }

        fun getTodayDateKey(): String {
            return dateFormat.format(System.currentTimeMillis())
        }
    }

    fun getTodayDateKey(): String {
        return Companion.getTodayDateKey()
    }

    fun observeTasksForToday(profileId: String): Flow<List<DailyCareTaskEntity>> {
        val todayKey = getTodayDateKey()
        return taskDao.getTasksForDateFlow(profileId, todayKey)
    }

    fun observeIncompleteTasksForToday(profileId: String): Flow<List<DailyCareTaskEntity>> {
        val todayKey = getTodayDateKey()
        return taskDao.getIncompleteTasksForDateFlow(profileId, todayKey)
    }

    /**
     * Evaluates daily rules for the profile:
     * - Checked once per day as app is opened.
     * - If day rolled over, uncompleted tasks from prior day are cleared from today's deck,
     *   and tasks are re-evaluated purely from scratch based on latest conditions.
     */
    suspend fun evaluateDailyRules(profileId: String, forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (profileId.isBlank()) return@withContext

        val todayKey = getTodayDateKey()
        val existingCount = taskDao.getTaskCountForDate(profileId, todayKey)

        if (existingCount > 0 && !forceRefresh) {
            // Already evaluated for today! Sync weekly tasks state (e.g. if snapshot or photo was taken today)
            checkAndSyncWeeklyTasks(profileId, todayKey)
            return@withContext
        }

        // Clean slate for today's incomplete tasks to re-evaluate from scratch
        taskDao.deleteIncompleteTasksForDate(profileId, todayKey)

        val now = System.currentTimeMillis()
        val oneDayMillis = 86_400_000L
        val sevenDaysMillis = 7 * oneDayMillis

        val plants = plantDao.getPlantsForProfile(profileId)
        val createdTasks = mutableListOf<DailyCareTaskEntity>()

        // Current month to detect summer (March to June in India: Calendar month 2..5)
        val cal = Calendar.getInstance(istTimeZone).apply { timeInMillis = now }
        val month = cal.get(Calendar.MONTH)
        val isSummer = month in Calendar.MARCH..Calendar.JUNE

        // --- RULE 1: Environment Snapshot ---
        // For every space that has >= 1 assigned plant, snapshot needed weekly (7 days).
        // Empty spaces don't need snapshots at all.
        val occupiedSpaces = plants.map { it.roomTag.trim() }.filter { it.isNotBlank() }.distinct()

        for (space in occupiedSpaces) {
            val assignedCount = plants.count { it.roomTag.equals(space, ignoreCase = true) }
            val latestSnapshot = snapshotDao.getLatestSnapshotForRoom(profileId, space)
            val needsSnapshot = latestSnapshot == null || (now - latestSnapshot.recordedAt) >= sevenDaysMillis

            if (needsSnapshot) {
                val daysElapsed = if (latestSnapshot != null) {
                    ((now - latestSnapshot.recordedAt) / oneDayMillis).toInt()
                } else 7
                val urgency = (daysElapsed - 7).coerceAtLeast(0)

                createdTasks.add(
                    DailyCareTaskEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        dateKey = todayKey,
                        taskType = "SNAPSHOT",
                        targetId = space,
                        targetTitle = space,
                        targetSubtitle = "$assignedCount plant${if (assignedCount > 1) "s" else ""} in space",
                        targetPhotoUri = null,
                        title = "Take Environment Snapshot",
                        description = "Weekly snapshot due for $space. Record light, humidity, and temperature.",
                        priority = 2, // Medium priority
                        urgencyDays = urgency,
                        spaceName = space,
                        isCompleted = false
                    )
                )
            }
        }

        // --- RULE 1b: Weekly Garden Photo ---
        // If garden has plants, and no photo was added for any plant in the last 7 days,
        // create a weekly task: "Add a photo of your garden".
        if (plants.isNotEmpty()) {
            val recentPhotoCount = photoDao.getPhotoCountSince(profileId, now - sevenDaysMillis)
            if (recentPhotoCount == 0) {
                createdTasks.add(
                    DailyCareTaskEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        dateKey = todayKey,
                        taskType = "GARDEN_PHOTO",
                        targetId = plants.first().id,
                        targetTitle = "Add a photo of your garden",
                        targetSubtitle = "Weekly · due today",
                        targetPhotoUri = null,
                        title = "Add a photo of your garden",
                        description = "Weekly photo of your plants to track progress",
                        priority = 2,
                        urgencyDays = 0,
                        spaceName = "",
                        isCompleted = false
                    )
                )
            }
        }

        // --- RULE 2: Watering & Plant Care Activities ---
        // Plants that need watering as per season, lastWateredAt, and min water intervals.
        // If plant added today (or lastWateredAt == null), watering is created today as Priority 1.
        for (plant in plants) {
            val masterPlant = plant.masterPlantId?.let { id -> masterDao.getPlantById(id) }
            val summerInterval = masterPlant?.wateringIntervalSummerDays ?: plant.wateringIntervalDays
            val winterInterval = masterPlant?.wateringIntervalWinterDays ?: plant.wateringIntervalDays
            val seasonalWateringInterval = if (isSummer) summerInterval else winterInterval

            // Check Watering (Priority 1 - Mandatory)
            val lastWater = plant.lastWateredAt
            if (lastWater == null) {
                // Day 1 rule: Plant added with no water log -> Due today as Priority 1!
                createdTasks.add(
                    DailyCareTaskEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        dateKey = todayKey,
                        taskType = PlantActivityType.WATER.name,
                        targetId = plant.id,
                        targetTitle = plant.nickname,
                        targetSubtitle = "${plant.botanicalName} • ${plant.roomTag}",
                        targetPhotoUri = plant.photoUri,
                        title = "Water",
                        description = "Every $seasonalWateringInterval days · newly added to garden",
                        priority = 1, // High / Mandatory
                        urgencyDays = 0,
                        spaceName = plant.roomTag,
                        isCompleted = false
                    )
                )
            } else {
                val daysSinceWater = ((now - lastWater) / oneDayMillis).toInt()
                if (daysSinceWater >= seasonalWateringInterval) {
                    val overdue = daysSinceWater - seasonalWateringInterval
                    val lastWateredStr = if (daysSinceWater == 0) "today" else "$daysSinceWater days ago"
                    createdTasks.add(
                        DailyCareTaskEntity(
                            id = UUID.randomUUID().toString(),
                            profileId = profileId,
                            dateKey = todayKey,
                            taskType = PlantActivityType.WATER.name,
                            targetId = plant.id,
                            targetTitle = plant.nickname,
                            targetSubtitle = "${plant.botanicalName} • ${plant.roomTag}",
                            targetPhotoUri = plant.photoUri,
                            title = "Water",
                            description = "Every $seasonalWateringInterval days · last watered $lastWateredStr",
                            priority = 1, // High / Mandatory
                            urgencyDays = overdue,
                            spaceName = plant.roomTag,
                            isCompleted = false
                        )
                    )
                }
            }

            // Routine Plant Activities
            // Rule 3: Rotate Pot (Weekly, default 7 days) -> Priority 4 (Plant Care)
            val lastRotate = plant.lastRotatedAt ?: plant.createdAt
            val daysSinceRotate = ((now - lastRotate) / oneDayMillis).toInt()
            if (daysSinceRotate >= plant.rotateIntervalDays) {
                val overdue = daysSinceRotate - plant.rotateIntervalDays
                createdTasks.add(
                    DailyCareTaskEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        dateKey = todayKey,
                        taskType = PlantActivityType.ROTATE.name,
                        targetId = plant.id,
                        targetTitle = plant.nickname,
                        targetSubtitle = "${plant.botanicalName} • ${plant.roomTag}",
                        targetPhotoUri = plant.photoUri,
                        title = PlantActivityType.ROTATE.title,
                        description = "Turn pot 1/4 turn · every ${plant.rotateIntervalDays} days",
                        priority = 4, // Priority 4 (Plant Care)
                        urgencyDays = overdue,
                        spaceName = plant.roomTag,
                        isCompleted = false
                    )
                )
            }

            // Rule 4: Pest Inspection (Weekly, default 7 days) -> Priority 3 (Plant Care)
            val lastInspect = plant.lastInspectedAt ?: plant.createdAt
            val daysSinceInspect = ((now - lastInspect) / oneDayMillis).toInt()
            if (daysSinceInspect >= plant.inspectIntervalDays) {
                val overdue = daysSinceInspect - plant.inspectIntervalDays
                createdTasks.add(
                    DailyCareTaskEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        dateKey = todayKey,
                        taskType = PlantActivityType.INSPECT.name,
                        targetId = plant.id,
                        targetTitle = plant.nickname,
                        targetSubtitle = "${plant.botanicalName} • ${plant.roomTag}",
                        targetPhotoUri = plant.photoUri,
                        title = PlantActivityType.INSPECT.title,
                        description = PlantActivityType.INSPECT.goldenRule,
                        priority = 3, // Priority 3 (Plant Care)
                        urgencyDays = overdue,
                        spaceName = plant.roomTag,
                        isCompleted = false
                    )
                )
            }

            // Rule 5: Feeding Manure (30-90 days) -> Priority 3 (Dedicated Feeding Manure Section)
            val lastFert = plant.lastFertilizedAt ?: plant.createdAt
            val daysSinceFert = ((now - lastFert) / oneDayMillis).toInt()
            if (daysSinceFert >= plant.fertilizeIntervalDays) {
                val overdue = daysSinceFert - plant.fertilizeIntervalDays
                val lastFedStr = if (plant.lastFertilizedAt == null) "not yet fed" else "$daysSinceFert days ago"
                createdTasks.add(
                    DailyCareTaskEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        dateKey = todayKey,
                        taskType = "FEED_MANURE",
                        targetId = plant.id,
                        targetTitle = plant.nickname,
                        targetSubtitle = "${plant.roomTag} · ${if (overdue > 0) "$overdue days overdue" else "due today"}",
                        targetPhotoUri = plant.photoUri,
                        title = "Feed manure",
                        description = "Every ${plant.fertilizeIntervalDays} days · last fed $lastFedStr",
                        priority = 3, // Priority 3 (Feeding Manure)
                        urgencyDays = overdue,
                        spaceName = plant.roomTag,
                        isCompleted = false
                    )
                )
            }

            // Rule 6: Leaf Cleaning (14 days) -> Priority 3 (Clean Leaves Section)
            val lastClean = plant.lastCleanedAt ?: plant.createdAt
            val daysSinceClean = ((now - lastClean) / oneDayMillis).toInt()
            if (daysSinceClean >= plant.cleanIntervalDays) {
                val overdue = daysSinceClean - plant.cleanIntervalDays
                createdTasks.add(
                    DailyCareTaskEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        dateKey = todayKey,
                        taskType = PlantActivityType.CLEAN.name,
                        targetId = plant.id,
                        targetTitle = plant.nickname,
                        targetSubtitle = "${plant.botanicalName} • ${plant.roomTag}",
                        targetPhotoUri = plant.photoUri,
                        title = "Clean leaves",
                        description = "Wipe leaves · every ${plant.cleanIntervalDays} days",
                        priority = 3, // Priority 3 (Clean Leaves)
                        urgencyDays = overdue,
                        spaceName = plant.roomTag,
                        isCompleted = false
                    )
                )
            }

            // Rule 7: Dead leaves / Pruning (14 days) -> Priority 4 (Plant Care)
            val lastPrune = plant.lastPrunedAt ?: plant.createdAt
            val daysSincePrune = ((now - lastPrune) / oneDayMillis).toInt()
            if (daysSincePrune >= plant.pruneIntervalDays) {
                val overdue = daysSincePrune - plant.pruneIntervalDays
                createdTasks.add(
                    DailyCareTaskEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        dateKey = todayKey,
                        taskType = PlantActivityType.PRUNE.name,
                        targetId = plant.id,
                        targetTitle = "Remove dead leaves · ${plant.nickname}",
                        targetSubtitle = "${plant.roomTag} · ${if (overdue > 0) "$overdue days overdue" else "due today"}",
                        targetPhotoUri = plant.photoUri,
                        title = "Pruning",
                        description = "${plant.roomTag} · ${if (overdue > 0) "$overdue days overdue" else "due today"}",
                        priority = 4, // Priority 4 (Plant Care)
                        urgencyDays = overdue,
                        spaceName = plant.roomTag,
                        isCompleted = false
                    )
                )
            }

            // Repotting Rule: Repotting -> Priority 4 (Plant Care)
            val lastRepot = plant.lastRepottedAt ?: plant.createdAt
            val daysSinceRepot = ((now - lastRepot) / oneDayMillis).toInt()
            if (daysSinceRepot >= plant.repotIntervalDays) {
                val overdue = daysSinceRepot - plant.repotIntervalDays
                createdTasks.add(
                    DailyCareTaskEntity(
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        dateKey = todayKey,
                        taskType = PlantActivityType.REPOT.name,
                        targetId = plant.id,
                        targetTitle = "Repot · ${plant.nickname}",
                        targetSubtitle = "${plant.roomTag} · ${if (overdue > 0) "$overdue days overdue" else "due today"}",
                        targetPhotoUri = plant.photoUri,
                        title = "Repotting",
                        description = "${plant.roomTag} · every ${plant.repotIntervalDays} days",
                        priority = 4, // Priority 4 (Plant Care)
                        urgencyDays = overdue,
                        spaceName = plant.roomTag,
                        isCompleted = false
                    )
                )
            }
        }

        if (createdTasks.isNotEmpty()) {
            taskDao.insertTasks(createdTasks)
        }
    }

    /**
     * Called immediately when a new plant is added to the user's garden.
     * Creates today's Day 1 watering task for this plant.
     */
    suspend fun onPlantAdded(plant: PlantEntity) = withContext(Dispatchers.IO) {
        val todayKey = getTodayDateKey()
        val task = DailyCareTaskEntity(
            id = UUID.randomUUID().toString(),
            profileId = plant.profileId,
            dateKey = todayKey,
            taskType = PlantActivityType.WATER.name,
            targetId = plant.id,
            targetTitle = plant.nickname,
            targetSubtitle = "${plant.botanicalName} • ${plant.roomTag}",
            targetPhotoUri = plant.photoUri,
            title = "Water Plant",
            description = PlantActivityType.WATER.goldenRule,
            priority = 1, // Mandatory
            urgencyDays = 0,
            spaceName = plant.roomTag,
            isCompleted = false
        )
        taskDao.insertTask(task)
    }

    suspend fun completeTask(taskId: String) = withContext(Dispatchers.IO) {
        taskDao.markTaskCompleted(taskId, System.currentTimeMillis())
    }

    suspend fun undoCompleteTask(taskId: String) = withContext(Dispatchers.IO) {
        taskDao.unmarkTaskCompleted(taskId)
    }

    suspend fun skipTask(taskId: String) = withContext(Dispatchers.IO) {
        taskDao.markTaskSkipped(taskId)
    }

    suspend fun markAllCompletedForSpace(profileId: String, taskType: String, spaceName: String) = withContext(Dispatchers.IO) {
        taskDao.markAllCompletedForSpace(profileId, getTodayDateKey(), taskType, spaceName)
    }

    fun observePendingTasksForToday(profileId: String): Flow<List<DailyCareTaskEntity>> {
        return taskDao.getPendingTasksForDateFlow(profileId, getTodayDateKey())
    }

    fun observeCompletedTasksForToday(profileId: String): Flow<List<DailyCareTaskEntity>> {
        return taskDao.getCompletedTasksForDateFlow(profileId, getTodayDateKey())
    }

    suspend fun checkAndSyncWeeklyTasks(profileId: String, todayKey: String = getTodayDateKey()) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val sevenDaysMillis = 7 * 86_400_000L
        val plants = plantDao.getPlantsForProfile(profileId)
        val todayTasks = taskDao.getTasksForDate(profileId, todayKey)

        // 1. Sync SNAPSHOT: if snapshot exists within 7 days, mark completed
        val pendingSnapshots = todayTasks.filter { it.taskType == "SNAPSHOT" && !it.isCompleted && !it.isSkipped }
        for (snapTask in pendingSnapshots) {
            val latestSnapshot = snapshotDao.getLatestSnapshotForRoom(profileId, snapTask.targetId)
            if (latestSnapshot != null && (now - latestSnapshot.recordedAt) < sevenDaysMillis) {
                taskDao.markTaskCompleted(snapTask.id, latestSnapshot.recordedAt)
            }
        }

        // 2. Sync GARDEN_PHOTO:
        val hasGardenPhotoTask = todayTasks.any { it.taskType == "GARDEN_PHOTO" }
        val recentPhotoCount = photoDao.getPhotoCountSince(profileId, now - sevenDaysMillis)
        if (hasGardenPhotoTask) {
            if (recentPhotoCount > 0) {
                todayTasks.filter { it.taskType == "GARDEN_PHOTO" && !it.isCompleted && !it.isSkipped }
                    .forEach { taskDao.markTaskCompleted(it.id, now) }
            }
        } else if (plants.isNotEmpty() && recentPhotoCount == 0) {
            // Need to insert GARDEN_PHOTO task for today
            taskDao.insertTask(
                DailyCareTaskEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    dateKey = todayKey,
                    taskType = "GARDEN_PHOTO",
                    targetId = plants.first().id,
                    targetTitle = "Add a photo of your garden",
                    targetSubtitle = "Weekly · due today",
                    targetPhotoUri = null,
                    title = "Add a photo of your garden",
                    description = "Weekly photo of your plants to track progress",
                    priority = 2,
                    urgencyDays = 0,
                    spaceName = "",
                    isCompleted = false
                )
            )
        }
    }
}
