package com.hos.happy.plants.data.telemetry

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hos.happy.plants.data.local.AppDatabase
import com.hos.happy.plants.data.local.entity.AppSessionEntity
import com.hos.happy.plants.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirebaseTelemetryManager private constructor(private val context: Context) : DefaultLifecycleObserver {

    private val db = AppDatabase.getInstance(context)
    private val sessionDao = db.appSessionDao()
    private val profileDao = db.userProfileDao()
    private val plantDao = db.plantDao()
    private val photoDao = db.plantPhotoDao()
    private val prefs = UserPreferencesRepository(context)

    private val telemetryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null

    private var currentSessionId: String? = null
    private var currentSessionStartTime: Long = 0L

    private var currentTabName: String? = null
    private var currentTabStartTime: Long = 0L

    companion object {
        private const val TAG = "FirebaseTelemetry"
        private const val SYNC_DELAY_MS = 5000L // 5 seconds after opening app
        val KNOWN_TABS = listOf("Task", "Garden", "Research", "Env")

        @Volatile
        private var INSTANCE: FirebaseTelemetryManager? = null

        fun initialize(context: Context): FirebaseTelemetryManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseTelemetryManager(context.applicationContext)
                INSTANCE = instance
                ProcessLifecycleOwner.get().lifecycle.addObserver(instance)
                instance
            }
        }

        fun getInstance(): FirebaseTelemetryManager? = INSTANCE
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onAppForegrounded()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        onAppBackgrounded()
    }

    /**
     * Triggered every time the app comes into the foreground.
     * Records the launch date & time, starts a new session in Room DB,
     * and schedules a sync to Firestore 5 seconds later.
     */
    fun onAppForegrounded() {
        val startTime = System.currentTimeMillis()
        val sessionId = UUID.randomUUID().toString()
        currentSessionId = sessionId
        currentSessionStartTime = startTime

        if (currentTabName != null) {
            currentTabStartTime = startTime
        }

        telemetryScope.launch {
            try {
                val activeProfileId = runCatching { prefs.activeProfileIdFlow.first() }.getOrNull() ?: "anonymous"
                val newSession = AppSessionEntity(
                    id = sessionId,
                    profileId = activeProfileId,
                    startTime = startTime,
                    endTime = null,
                    durationSeconds = 0L,
                    isSynced = false
                )
                sessionDao.insertSession(newSession)
                Log.d(TAG, "Recorded app launch session $sessionId at $startTime (profile: $activeProfileId)")

                // Schedule Firestore sync 5 seconds after opening app
                scheduleSyncAfterDelay(SYNC_DELAY_MS)
            } catch (e: Exception) {
                Log.e(TAG, "Error recording app launch session", e)
            }
        }
    }

    /**
     * Triggered when the app goes into the background.
     * Computes session duration and persists it locally and to Firestore.
     */
    fun onAppBackgrounded() {
        val sessionId = currentSessionId ?: return
        val startTime = currentSessionStartTime
        val endTime = System.currentTimeMillis()
        val durationSeconds = ((endTime - startTime) / 1000).coerceAtLeast(1L)

        // Record time spent on current tab before app is backgrounded
        val activeTab = currentTabName
        val activeTabStart = currentTabStartTime
        if (activeTab != null && activeTabStart > 0) {
            val tabDuration = ((endTime - activeTabStart) / 1000).coerceAtLeast(0L)
            if (tabDuration > 0) {
                recordTabTime(activeTab, tabDuration)
            }
            currentTabStartTime = 0L
        }

        telemetryScope.launch {
            try {
                sessionDao.updateSessionEndTime(
                    id = sessionId,
                    endTime = endTime,
                    durationSeconds = durationSeconds
                )
                Log.d(TAG, "App backgrounded. Session $sessionId duration: ${durationSeconds}s")

                // Update session duration in Firestore if active profile exists
                val activeProfileId = runCatching { prefs.activeProfileIdFlow.first() }.getOrNull()
                if (!activeProfileId.isNullOrEmpty() && activeProfileId != "anonymous") {
                    syncSingleSessionToFirestore(activeProfileId, sessionId, startTime, endTime, durationSeconds)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating app background session", e)
            }
        }
    }

    /**
     * Records when a user navigates to a tab.
     */
    fun onTabEntered(tabName: String) {
        val now = System.currentTimeMillis()
        val prevTab = currentTabName
        val prevStart = currentTabStartTime

        if (prevTab != null && prevStart > 0 && prevTab != tabName) {
            val elapsed = ((now - prevStart) / 1000).coerceAtLeast(0L)
            if (elapsed > 0) {
                recordTabTime(prevTab, elapsed)
            }
        }

        currentTabName = tabName
        currentTabStartTime = now
        Log.d(TAG, "Entered tab: $tabName")
    }

    /**
     * Records when a user navigates away from a tab.
     */
    fun onTabExited(tabName: String) {
        val now = System.currentTimeMillis()
        if (currentTabName == tabName && currentTabStartTime > 0) {
            val elapsed = ((now - currentTabStartTime) / 1000).coerceAtLeast(0L)
            if (elapsed > 0) {
                recordTabTime(tabName, elapsed)
            }
            currentTabStartTime = 0L
            currentTabName = null
        }
    }

    private fun recordTabTime(tabName: String, seconds: Long) {
        telemetryScope.launch {
            try {
                val activeProfileId = runCatching { prefs.activeProfileIdFlow.first() }.getOrNull()
                if (!activeProfileId.isNullOrEmpty() && activeProfileId != "anonymous") {
                    prefs.addTabDurationSeconds(activeProfileId, tabName, seconds)
                    Log.d(TAG, "Recorded ${seconds}s spent on tab: $tabName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error recording tab duration", e)
            }
        }
    }

    /**
     * Called when a profile is created or activated.
     * Re-claims any anonymous sessions and starts an immediate sync.
     */
    fun onProfileActivated(profileId: String) {
        telemetryScope.launch {
            try {
                sessionDao.claimAnonymousSessions(profileId)
                triggerSyncNow()
            } catch (e: Exception) {
                Log.e(TAG, "Error claiming sessions for profile $profileId", e)
            }
        }
    }

    fun recordPlantIdentityCall() {
        telemetryScope.launch {
            try {
                val activeProfileId = runCatching { prefs.activeProfileIdFlow.first() }.getOrNull()
                if (!activeProfileId.isNullOrEmpty() && activeProfileId != "anonymous") {
                    prefs.incrementPlantIdentityCalls(activeProfileId)
                    Log.d(TAG, "Recorded plant identity call for $activeProfileId")
                    triggerSyncNow()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error recording plant identity call", e)
            }
        }
    }

    fun recordPlantDiseaseCall() {
        telemetryScope.launch {
            try {
                val activeProfileId = runCatching { prefs.activeProfileIdFlow.first() }.getOrNull()
                if (!activeProfileId.isNullOrEmpty() && activeProfileId != "anonymous") {
                    prefs.incrementPlantDiseaseCalls(activeProfileId)
                    Log.d(TAG, "Recorded plant disease call for $activeProfileId")
                    triggerSyncNow()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error recording plant disease call", e)
            }
        }
    }

    fun recordPlantPhotoUploaded() {
        telemetryScope.launch {
            try {
                Log.d(TAG, "Plant photo uploaded; triggering telemetry sync")
                triggerSyncNow()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling photo upload sync", e)
            }
        }
    }

    fun triggerSyncNow() {
        telemetryScope.launch {
            syncToFirestore()
        }
    }

    private fun scheduleSyncAfterDelay(delayMs: Long) {
        syncJob?.cancel()
        syncJob = telemetryScope.launch {
            delay(delayMs)
            syncToFirestore()
        }
    }

    /**
     * Synchronizes user info, device specs, garden plants, usage summary,
     * and individual launch session history to Firebase Cloud Firestore.
     */
    private suspend fun syncToFirestore() {
        try {
            val activeProfileId = runCatching { prefs.activeProfileIdFlow.first() }.getOrNull()

            // Only sync after profile is created / active
            if (activeProfileId.isNullOrEmpty() || activeProfileId == "anonymous") {
                Log.d(TAG, "Sync skipped: No active user profile created or selected yet.")
                return
            }

            // Claim any sessions that were started while anonymous
            sessionDao.claimAnonymousSessions(activeProfileId)

            val profile = profileDao.getProfileById(activeProfileId)
            if (profile == null) {
                Log.d(TAG, "Sync skipped: Profile entity with ID $activeProfileId not found.")
                return
            }

            val plants = plantDao.getPlantsForProfile(activeProfileId)

            // Usage analytics (last 30 days & all-time)
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

            val opensLastMonth = sessionDao.getLaunchCountSince(activeProfileId, thirtyDaysAgo)
            val totalOpens = sessionDao.getTotalLaunchCount(activeProfileId)
            val timeSpentLastMonthSeconds = sessionDao.getTotalDurationSecondsSince(activeProfileId, thirtyDaysAgo) ?: 0L
            val totalTimeSpentSeconds = sessionDao.getTotalDurationSeconds(activeProfileId) ?: 0L

            val lastLaunchTime = currentSessionStartTime.takeIf { it > 0 } ?: now

            // Time spent on each tab
            val tabDurations = prefs.getAllTabDurations(activeProfileId, KNOWN_TABS)
            val tabTimes = hashMapOf<String, Any>()
            tabDurations.forEach { (tab, secs) ->
                tabTimes[tab] = hashMapOf(
                    "seconds" to secs,
                    "formatted" to formatDuration(secs)
                )
            }

            val identityCalls = prefs.getPlantIdentityCalls(activeProfileId)
            val diseaseCalls = prefs.getPlantDiseaseCalls(activeProfileId)
            val photosUploaded = photoDao.getTotalPhotoCount(activeProfileId)

            val rootData = hashMapOf<String, Any>(
                "userEmail" to profile.email,
                "profileId" to profile.id,
                "plantIdentityDetectionCalls" to identityCalls,
                "plantDiseaseDetectionCalls" to diseaseCalls,
                "plantPhotosUploaded" to photosUploaded,
                "featureUsage" to hashMapOf(
                    "plantIdentityDetectionCalls" to identityCalls,
                    "plantDiseaseDetectionCalls" to diseaseCalls,
                    "plantPhotosUploaded" to photosUploaded
                ),
                "timeSpent" to hashMapOf(
                    "totalTimeSpentSeconds" to totalTimeSpentSeconds,
                    "timeSpentLastMonthSeconds" to timeSpentLastMonthSeconds,
                    "totalTimeSpentFormatted" to formatDuration(totalTimeSpentSeconds),
                    "lastActiveTimestamp" to now,
                    "lastActiveFormatted" to formatDateTime(now),
                    "byTab" to tabTimes
                ),
                "timeSpentByTab" to tabTimes,
                "garden" to hashMapOf(
                    "plantCount" to plants.size,
                    "plants" to plants.map { plant ->
                        hashMapOf(
                            "id" to plant.id,
                            "nickname" to plant.nickname,
                            "commonName" to plant.commonName,
                            "botanicalName" to plant.botanicalName,
                            "roomTag" to plant.roomTag,
                            "wateringIntervalDays" to plant.wateringIntervalDays,
                            "waterScheduleType" to (plant.waterScheduleType ?: "INTERVAL")
                        )
                    }
                ),
                "deviceInfo" to FieldValue.delete(),
                "updatedAt" to now
            )

            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(profile.id)

            // 1. Write user summary document
            userDocRef.set(rootData, SetOptions.merge()).await()
            Log.d(TAG, "Successfully synced user email, plants, and time spent to Firestore (UUID: ${profile.id})")

            // 2. Sync individual sessions to subcollection users/{profileId}/sessions/{sessionId}
            val unsyncedSessions = sessionDao.getUnsyncedSessions(activeProfileId)
            if (unsyncedSessions.isNotEmpty()) {
                val batch = firestore.batch()
                val syncedIds = mutableListOf<String>()

                for (s in unsyncedSessions) {
                    val sessionDocRef = userDocRef.collection("sessions").document(s.id)
                    val sessionData = hashMapOf<String, Any?>(
                        "sessionId" to s.id,
                        "profileId" to s.profileId,
                        "startTime" to s.startTime,
                        "startTimeFormatted" to formatDateTime(s.startTime),
                        "endTime" to s.endTime,
                        "endTimeFormatted" to (s.endTime?.let { formatDateTime(it) }),
                        "durationSeconds" to s.durationSeconds,
                        "syncedAt" to now
                    )
                    batch.set(sessionDocRef, sessionData, SetOptions.merge())
                    syncedIds.add(s.id)
                }

                batch.commit().await()
                sessionDao.markSessionsAsSynced(syncedIds)
                Log.d(TAG, "Successfully synced ${syncedIds.size} individual sessions to Firestore")
            }
        } catch (e: Exception) {
            // Firestore handles offline caching automatically, but log any exception safely
            Log.w(TAG, "Firestore telemetry sync encountered an issue: ${e.message}")
        }
    }

    private suspend fun syncSingleSessionToFirestore(
        profileId: String,
        sessionId: String,
        startTime: Long,
        endTime: Long,
        durationSeconds: Long
    ) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(profileId)
            val sessionDocRef = userDocRef.collection("sessions").document(sessionId)

            val now = System.currentTimeMillis()
            val sessionData = hashMapOf<String, Any?>(
                "sessionId" to sessionId,
                "profileId" to profileId,
                "startTime" to startTime,
                "startTimeFormatted" to formatDateTime(startTime),
                "endTime" to endTime,
                "endTimeFormatted" to formatDateTime(endTime),
                "durationSeconds" to durationSeconds,
                "syncedAt" to now
            )
            sessionDocRef.set(sessionData, SetOptions.merge()).await()
            sessionDao.markSessionsAsSynced(listOf(sessionId))

            // Also refresh overall time spent on the user root doc
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
            val timeSpentLastMonthSeconds = sessionDao.getTotalDurationSecondsSince(profileId, thirtyDaysAgo) ?: 0L
            val totalTimeSpentSeconds = sessionDao.getTotalDurationSeconds(profileId) ?: 0L

            val tabDurations = prefs.getAllTabDurations(profileId, KNOWN_TABS)
            val tabTimes = hashMapOf<String, Any>()
            tabDurations.forEach { (tab, secs) ->
                tabTimes[tab] = hashMapOf(
                    "seconds" to secs,
                    "formatted" to formatDuration(secs)
                )
            }

            val identityCalls = prefs.getPlantIdentityCalls(profileId)
            val diseaseCalls = prefs.getPlantDiseaseCalls(profileId)
            val photosUploaded = photoDao.getTotalPhotoCount(profileId)

            userDocRef.set(
                hashMapOf(
                    "plantIdentityDetectionCalls" to identityCalls,
                    "plantDiseaseDetectionCalls" to diseaseCalls,
                    "plantPhotosUploaded" to photosUploaded,
                    "featureUsage" to hashMapOf(
                        "plantIdentityDetectionCalls" to identityCalls,
                        "plantDiseaseDetectionCalls" to diseaseCalls,
                        "plantPhotosUploaded" to photosUploaded
                    ),
                    "timeSpent" to hashMapOf(
                        "totalTimeSpentSeconds" to totalTimeSpentSeconds,
                        "timeSpentLastMonthSeconds" to timeSpentLastMonthSeconds,
                        "totalTimeSpentFormatted" to formatDuration(totalTimeSpentSeconds),
                        "lastActiveTimestamp" to now,
                        "lastActiveFormatted" to formatDateTime(now),
                        "byTab" to tabTimes
                    ),
                    "timeSpentByTab" to tabTimes,
                    "deviceInfo" to FieldValue.delete(),
                    "updatedAt" to now
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync single session to Firestore: ${e.message}")
        }
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%dh %dm %ds", hours, minutes, secs)
        } else if (minutes > 0) {
            String.format(Locale.getDefault(), "%dm %ds", minutes, secs)
        } else {
            "${secs}s"
        }
    }

    private fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
