package com.hos.happy.plants

import android.app.Application
import com.hos.happy.plants.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HappyPlantsApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Asynchronously check phone settings & sensor on install and save to DataStore
        // so it never needs to be checked again and again
        val prefs = UserPreferencesRepository(this)
        applicationScope.launch {
            prefs.getOrInitializeLightMeterSettings()
        }

        // Initialize daily duty notification channel and schedule 7:00 AM alarm
        com.hos.happy.plants.data.notification.DailyNotificationScheduler.createNotificationChannel(this)
        com.hos.happy.plants.data.notification.DailyNotificationScheduler.scheduleDailyDutyAlarm(this)

        // Initialize Firebase Telemetry & Session Lifecycle Tracker
        com.hos.happy.plants.data.telemetry.FirebaseTelemetryManager.initialize(this)
    }
}
