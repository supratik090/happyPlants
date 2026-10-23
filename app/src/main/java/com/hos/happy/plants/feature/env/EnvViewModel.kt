package com.hos.happy.plants.feature.env

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.hos.happy.plants.data.local.AppDatabase
import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import com.hos.happy.plants.data.preferences.LightMeterMode
import com.hos.happy.plants.data.preferences.LightMeterSettings
import com.hos.happy.plants.data.preferences.UserPreferencesRepository
import com.hos.happy.plants.data.remote.OpenMeteoApiService
import com.hos.happy.plants.data.remote.model.LiveEnvSnapshot
import com.hos.happy.plants.data.sensor.LightReading
import com.hos.happy.plants.data.sensor.LightSensorManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale

data class EnvUiState(
    val currentProfileId: String? = null,
    val lightSettings: LightMeterSettings = LightMeterSettings(hasHardwareSensor = true, preferredMode = LightMeterMode.SENSOR),
    val liveLightReading: LightReading = LightReading(lux = 0f, source = "SENSOR"),
    val isLightFrozen: Boolean = false,
    val frozenLightReading: LightReading? = null,
    val weatherSnapshot: LiveEnvSnapshot? = null,
    val isWeatherLoading: Boolean = false,
    val weatherErrorMessage: String? = null,
    val locationName: String? = null,
    val selectedRoomTag: String = "Living Room",
    val snapshotSavedSuccess: Boolean = false,
    
    // History filtering
    val historyFilterRoom: String? = null, // null means "All Rooms"
    val historyFilterDateMillis: Long? = null // timestamp representing start of day
)

class EnvViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val snapshotDao = db.roomEnvSnapshotDao()
    private val prefs = UserPreferencesRepository(application)
    private val openMeteoApi = OpenMeteoApiService()
    private val sensorManager = LightSensorManager(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(EnvUiState())
    val uiState: StateFlow<EnvUiState> = _uiState.asStateFlow()

    private var sensorJob: Job? = null

    // Room tags
    private val defaultTags = listOf("Living Room", "Balcony", "Bedroom", "Study Desk", "Kitchen", "Patio")
    val availableRoomTags: StateFlow<List<String>> = prefs.customRoomTagsFlow
        .map { customTags -> (defaultTags + customTags).distinct() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            defaultTags
        )

    // Distinct rooms with recorded snapshots
    private val _historyRoomFilter = MutableStateFlow<String?>(null)
    private val _historyDateFilter = MutableStateFlow<Long?>(null)

    val distinctRecordedRooms: StateFlow<List<String>> = _uiState.flatMapLatest { state ->
        val profileId = state.currentProfileId ?: ""
        snapshotDao.getDistinctRoomsFlow(profileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recordedSnapshotTimestamps: StateFlow<List<Long>> = _uiState.flatMapLatest { state ->
        val profileId = state.currentProfileId ?: ""
        val roomTag = state.historyFilterRoom
        snapshotDao.getRecordedTimestampsFlow(profileId, roomTag)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSnapshots: StateFlow<List<RoomEnvSnapshotEntity>> = _uiState.flatMapLatest { state ->
        val profileId = state.currentProfileId ?: ""
        val roomTag = state.historyFilterRoom
        val dateMillis = state.historyFilterDateMillis

        val (startOfDay, endOfDay) = if (dateMillis != null) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis
            Pair(start, end)
        } else {
            Pair(null, null)
        }

        snapshotDao.filterSnapshotsFlow(
            profileId = profileId,
            roomTag = roomTag,
            startTimeMillis = startOfDay,
            endTimeMillis = endOfDay
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            // Read or initialize settings once
            val initialSettings = prefs.getOrInitializeLightMeterSettings()
            _uiState.value = _uiState.value.copy(lightSettings = initialSettings)

            // Start ambient sensor if SENSOR mode is preferred
            if (initialSettings.preferredMode == LightMeterMode.SENSOR && initialSettings.hasHardwareSensor) {
                startSensorListening()
            }

            // Collect changes to preferences
            launch {
                prefs.lightMeterSettingsFlow.collectLatest { settings ->
                    _uiState.value = _uiState.value.copy(lightSettings = settings)
                    if (settings.preferredMode == LightMeterMode.SENSOR && settings.hasHardwareSensor) {
                        startSensorListening()
                    } else {
                        stopSensorListening()
                    }
                }
            }

            // Collect active profile
            launch {
                prefs.activeProfileIdFlow.collectLatest { profileId ->
                    _uiState.value = _uiState.value.copy(currentProfileId = profileId)
                }
            }
        }
    }

    fun toggleLightMeterMode() {
        viewModelScope.launch {
            val currentMode = _uiState.value.lightSettings.preferredMode
            val newMode = if (currentMode == LightMeterMode.SENSOR) {
                LightMeterMode.CAMERA
            } else {
                // If user toggles back to SENSOR, make sure hardware sensor exists
                if (_uiState.value.lightSettings.hasHardwareSensor) LightMeterMode.SENSOR else LightMeterMode.CAMERA
            }
            prefs.setPreferredMode(newMode)
        }
    }

    private fun startSensorListening() {
        sensorJob?.cancel()
        sensorJob = viewModelScope.launch {
            sensorManager.getLightReadingFlow().collect { reading ->
                if (!_uiState.value.isLightFrozen) {
                    _uiState.value = _uiState.value.copy(liveLightReading = reading)
                }
            }
        }
    }

    private fun stopSensorListening() {
        sensorJob?.cancel()
        sensorJob = null
    }

    fun onCameraLightCalculated(reading: LightReading) {
        if (_uiState.value.lightSettings.preferredMode == LightMeterMode.CAMERA && !_uiState.value.isLightFrozen) {
            _uiState.value = _uiState.value.copy(liveLightReading = reading)
        }
    }

    fun toggleLightFreeze() {
        val currentlyFrozen = _uiState.value.isLightFrozen
        if (currentlyFrozen) {
            _uiState.value = _uiState.value.copy(
                isLightFrozen = false,
                frozenLightReading = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLightFrozen = true,
                frozenLightReading = _uiState.value.liveLightReading
            )
        }
    }

    fun selectRoomTag(tag: String) {
        _uiState.value = _uiState.value.copy(selectedRoomTag = tag)
    }

    fun addCustomRoomTag(tag: String) {
        if (tag.isNotBlank()) {
            viewModelScope.launch {
                prefs.addCustomRoomTag(tag.trim())
                _uiState.value = _uiState.value.copy(selectedRoomTag = tag.trim())
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshLiveEnvironment(hasLocationPermission: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWeatherLoading = true, weatherErrorMessage = null)

            var lat = 12.9716 // Default fallback (e.g. Bangalore)
            var lng = 77.5946
            var locName = "Default Location"

            if (hasLocationPermission) {
                try {
                    val location: Location? = fusedLocationClient.lastLocation.await()
                    if (location != null) {
                        lat = location.latitude
                        lng = location.longitude

                        val geocoder = Geocoder(getApplication(), Locale.getDefault())
                        val addresses = runCatching {
                            geocoder.getFromLocation(lat, lng, 1)
                        }.getOrNull()

                        val city = addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea
                        val country = addresses?.firstOrNull()?.countryCode
                        locName = if (city != null) "$city, $country" else "Current Location"
                    }
                } catch (e: Exception) {
                    // Fall back to default coordinates
                }
            }

            openMeteoApi.fetchLiveEnvironment(lat, lng)
                .onSuccess { snapshot ->
                    _uiState.value = _uiState.value.copy(
                        weatherSnapshot = snapshot.copy(locationName = locName),
                        locationName = locName,
                        isWeatherLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isWeatherLoading = false,
                        weatherErrorMessage = error.localizedMessage ?: "Failed to fetch live environment data"
                    )
                }
        }
    }

    fun saveSnapshot(note: String?) {
        val state = _uiState.value
        val profileId = state.currentProfileId ?: return
        val activeReading = if (state.isLightFrozen && state.frozenLightReading != null) {
            state.frozenLightReading
        } else {
            state.liveLightReading
        }

        viewModelScope.launch {
            val entity = RoomEnvSnapshotEntity(
                profileId = profileId,
                roomTag = state.selectedRoomTag,
                recordedAt = System.currentTimeMillis(),
                lightLux = activeReading.lux,
                lightFootCandles = activeReading.footCandles,
                lightSource = activeReading.source,
                temperatureCelsius = state.weatherSnapshot?.temperatureCelsius,
                humidityPercentage = state.weatherSnapshot?.humidityPercentage,
                aqi = state.weatherSnapshot?.aqi,
                uvIndex = state.weatherSnapshot?.uvIndex,
                daylightHours = state.weatherSnapshot?.daylightHours,
                note = note?.takeIf { it.isNotBlank() }
            )

            snapshotDao.insertSnapshot(entity)
            _uiState.value = _uiState.value.copy(snapshotSavedSuccess = true)
        }
    }

    fun resetSnapshotSavedSuccess() {
        _uiState.value = _uiState.value.copy(snapshotSavedSuccess = false)
    }

    fun setHistoryFilterRoom(room: String?) {
        _uiState.value = _uiState.value.copy(historyFilterRoom = room)
    }

    fun setHistoryFilterDate(dateMillis: Long?) {
        _uiState.value = _uiState.value.copy(historyFilterDateMillis = dateMillis)
    }

    override fun onCleared() {
        super.onCleared()
        stopSensorListening()
    }
}
