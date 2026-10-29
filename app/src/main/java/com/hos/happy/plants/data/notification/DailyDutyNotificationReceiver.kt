package com.hos.happy.plants.data.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.hos.happy.plants.MainActivity
import com.hos.happy.plants.R
import com.hos.happy.plants.data.local.AppDatabase
import com.hos.happy.plants.data.preferences.UserPreferencesRepository
import com.hos.happy.plants.data.repository.TaskRuleEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DailyDutyNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val prefs = UserPreferencesRepository(context)
                val activeProfileId = prefs.activeProfileIdFlow.firstOrNull()

                if (!activeProfileId.isNullOrBlank()) {
                    val ruleEngine = TaskRuleEngine(context)
                    // Evaluate today's rules so tasks are up-to-date
                    ruleEngine.evaluateDailyRules(activeProfileId)

                    val todayKey = TaskRuleEngine.getTodayDateKey()
                    val pendingTasks = db.dailyCareTaskDao().getPendingTasksForDate(activeProfileId, todayKey)

                    if (pendingTasks.isNotEmpty()) {
                        showClubbedDutyNotification(context, pendingTasks.size, pendingTasks.map { it.taskType })
                    }
                }
            } catch (e: Exception) {
                // Log and ignore to prevent receiver crash
            } finally {
                // Reschedule for next 7:00 AM
                DailyNotificationScheduler.scheduleDailyDutyAlarm(context)
                pendingResult.finish()
            }
        }
    }

    private fun showClubbedDutyNotification(context: Context, totalCount: Int, taskTypes: List<String>) {
        val waterCount = taskTypes.count { it == "WATER" }
        val cleanCount = taskTypes.count { it == "CLEAN" }
        val feedCount = taskTypes.count { it in listOf("FEED", "FEED_MANURE") }
        val snapshotCount = taskTypes.count { it == "SNAPSHOT" }
        val otherCount = totalCount - (waterCount + cleanCount + feedCount + snapshotCount)

        val parts = mutableListOf<String>()
        if (waterCount > 0) parts.add("$waterCount water")
        if (cleanCount > 0) parts.add("$cleanCount clean")
        if (feedCount > 0) parts.add("$feedCount feed")
        if (snapshotCount > 0) parts.add("$snapshotCount snapshot")
        if (otherCount > 0) parts.add("$otherCount care")

        val summaryDetails = if (parts.isNotEmpty()) parts.joinToString(" · ") else "$totalCount pending tasks"
        val contentText = "$summaryDetails. Tap to open your task list."

        // Deep-link intent to MainActivity -> Today Tasks
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(DailyNotificationScheduler.EXTRA_NAVIGATE_TO, DailyNotificationScheduler.DESTINATION_TODAY_TASKS)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, flags)

        val notification = NotificationCompat.Builder(context, DailyNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_metro_greens_icon)
            .setContentTitle("🌱 You have plant duty today!")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🌱 You have plant duty today!\n$summaryDetails.\nTap to review and complete your tasks.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DailyNotificationScheduler.NOTIFICATION_ID, notification)
    }
}
