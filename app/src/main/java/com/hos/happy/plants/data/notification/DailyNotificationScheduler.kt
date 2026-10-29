package com.hos.happy.plants.data.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object DailyNotificationScheduler {

    const val CHANNEL_ID = "daily_plant_duty"
    const val CHANNEL_NAME = "Daily Plant Duty"
    const val NOTIFICATION_ID = 1001
    const val EXTRA_NAVIGATE_TO = "navigate_to"
    const val DESTINATION_TODAY_TASKS = "today_tasks"
    const val ACTION_DAILY_DUTY = "com.hos.happy.plants.ACTION_DAILY_DUTY"

    private const val ALARM_REQUEST_CODE = 7001

    /**
     * Creates the Android notification channel (required for Android 8.0+ / API 26+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily morning reminder for pending plant care duties"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules an alarm to fire every day at 7:00 AM.
     * Uses setExactAndAllowWhileIdle where allowed, falling back to setAndAllowWhileIdle.
     */
    fun scheduleDailyDutyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, DailyDutyNotificationReceiver::class.java).apply {
            action = ACTION_DAILY_DUTY
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, flags)

        // Calculate next 7:00 AM
        val now = Calendar.getInstance()
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // If exact alarm permission is restricted on Android 12+, fall back to inexact
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                alarmTime.timeInMillis,
                pendingIntent
            )
        }
    }
}
