package com.hos.happy.plants.data.preferences

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "happy_plants_prefs")

enum class LightMeterMode {
    SENSOR,
    CAMERA
}

data class LightMeterSettings(
    val hasHardwareSensor: Boolean,
    val preferredMode: LightMeterMode
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val KEY_SENSOR_CHECKED = booleanPreferencesKey("key_sensor_checked")
        val KEY_HAS_HARDWARE_LIGHT_SENSOR = booleanPreferencesKey("key_has_hardware_light_sensor")
        val KEY_PREFERRED_LIGHT_METER_MODE = stringPreferencesKey("key_preferred_light_meter_mode")
        val KEY_ACTIVE_PROFILE_ID = stringPreferencesKey("key_active_profile_id")
        val KEY_CUSTOM_ROOM_TAGS = stringSetPreferencesKey("key_custom_room_tags")
        val KEY_PLANTNET_API_KEY = stringPreferencesKey("key_plantnet_api_key")
    }

    /**
     * Inspects phone hardware once on first install, caches it in DataStore,
     * and avoids querying SensorManager on subsequent runs.
     */
    suspend fun getOrInitializeLightMeterSettings(): LightMeterSettings {
        val prefs = context.dataStore.data.first()
        val isChecked = prefs[PreferencesKeys.KEY_SENSOR_CHECKED] ?: false

        if (isChecked) {
            val hasSensor = prefs[PreferencesKeys.KEY_HAS_HARDWARE_LIGHT_SENSOR] ?: true
            val modeString = prefs[PreferencesKeys.KEY_PREFERRED_LIGHT_METER_MODE] ?: if (hasSensor) LightMeterMode.SENSOR.name else LightMeterMode.CAMERA.name
            val mode = runCatching { LightMeterMode.valueOf(modeString) }.getOrDefault(LightMeterMode.SENSOR)
            return LightMeterSettings(hasHardwareSensor = hasSensor, preferredMode = mode)
        }

        // First run check
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        val hasSensor = (lightSensor != null)
        val initialMode = if (hasSensor) LightMeterMode.SENSOR else LightMeterMode.CAMERA

        context.dataStore.edit { mutablePrefs ->
            mutablePrefs[PreferencesKeys.KEY_SENSOR_CHECKED] = true
            mutablePrefs[PreferencesKeys.KEY_HAS_HARDWARE_LIGHT_SENSOR] = hasSensor
            mutablePrefs[PreferencesKeys.KEY_PREFERRED_LIGHT_METER_MODE] = initialMode.name
        }

        return LightMeterSettings(hasHardwareSensor = hasSensor, preferredMode = initialMode)
    }

    val lightMeterSettingsFlow: Flow<LightMeterSettings> = context.dataStore.data.map { prefs ->
        val hasSensor = prefs[PreferencesKeys.KEY_HAS_HARDWARE_LIGHT_SENSOR] ?: true
        val modeString = prefs[PreferencesKeys.KEY_PREFERRED_LIGHT_METER_MODE] ?: if (hasSensor) LightMeterMode.SENSOR.name else LightMeterMode.CAMERA.name
        val mode = runCatching { LightMeterMode.valueOf(modeString) }.getOrDefault(LightMeterMode.SENSOR)
        LightMeterSettings(hasHardwareSensor = hasSensor, preferredMode = mode)
    }

    suspend fun setPreferredMode(mode: LightMeterMode) {
        context.dataStore.edit { mutablePrefs ->
            mutablePrefs[PreferencesKeys.KEY_PREFERRED_LIGHT_METER_MODE] = mode.name
        }
    }

    val activeProfileIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.KEY_ACTIVE_PROFILE_ID]
    }

    suspend fun setActiveProfileId(profileId: String?) {
        context.dataStore.edit { mutablePrefs ->
            if (profileId == null) {
                mutablePrefs.remove(PreferencesKeys.KEY_ACTIVE_PROFILE_ID)
            } else {
                mutablePrefs[PreferencesKeys.KEY_ACTIVE_PROFILE_ID] = profileId
            }
        }
    }

    val customRoomTagsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.KEY_CUSTOM_ROOM_TAGS] ?: emptySet()
    }

    suspend fun addCustomRoomTag(tag: String) {
        context.dataStore.edit { mutablePrefs ->
            val current = mutablePrefs[PreferencesKeys.KEY_CUSTOM_ROOM_TAGS] ?: emptySet()
            mutablePrefs[PreferencesKeys.KEY_CUSTOM_ROOM_TAGS] = current + tag.trim()
        }
    }

    suspend fun getPlantNetApiKey(): String {
        val prefs = context.dataStore.data.first()
        val customKey = prefs[PreferencesKeys.KEY_PLANTNET_API_KEY]?.trim()
        if (!customKey.isNullOrEmpty()) return customKey
        return com.hos.happy.plants.BuildConfig.PLANTNET_API_KEY.trim()
    }

    suspend fun setPlantNetApiKey(apiKey: String) {
        context.dataStore.edit { mutablePrefs ->
            if (apiKey.isBlank()) {
                mutablePrefs.remove(PreferencesKeys.KEY_PLANTNET_API_KEY)
            } else {
                mutablePrefs[PreferencesKeys.KEY_PLANTNET_API_KEY] = apiKey.trim()
            }
        }
    }
}
