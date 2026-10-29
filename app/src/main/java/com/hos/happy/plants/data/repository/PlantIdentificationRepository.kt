package com.hos.happy.plants.data.repository

import android.content.Context
import com.hos.happy.plants.data.local.AppDatabase
import com.hos.happy.plants.data.local.entity.PlantIdentificationUsageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PlantIdentificationRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val usageDao = db.plantIdentificationUsageDao()

    companion object {
        const val DAILY_LIMIT = 3

        /**
         * Returns current date string formatted as "yyyy-MM-dd" in Indian Standard Time (IST, UTC+5:30).
         * Resets at 12:00 AM IST automatically.
         */
        fun getTodayIstDateKey(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            return sdf.format(Date())
        }
    }

    suspend fun getRemainingAttempts(): Int = withContext(Dispatchers.IO) {
        val today = getTodayIstDateKey()
        val record = usageDao.getUsageForDate(today)
        val used = record?.attemptCount ?: 0
        (DAILY_LIMIT - used).coerceAtLeast(0)
    }

    fun getRemainingAttemptsFlow(): Flow<Int> {
        val today = getTodayIstDateKey()
        return usageDao.getUsageFlowForDate(today).map { record ->
            val used = record?.attemptCount ?: 0
            (DAILY_LIMIT - used).coerceAtLeast(0)
        }
    }

    suspend fun canAttemptIdentification(): Boolean = withContext(Dispatchers.IO) {
        getRemainingAttempts() > 0
    }

    suspend fun recordAttempt(): Boolean = withContext(Dispatchers.IO) {
        val today = getTodayIstDateKey()
        val record = usageDao.getUsageForDate(today)
        val currentCount = record?.attemptCount ?: 0
        if (currentCount >= DAILY_LIMIT) {
            return@withContext false
        }
        val newRecord = PlantIdentificationUsageEntity(
            dateKey = today,
            attemptCount = currentCount + 1,
            lastAttemptAt = System.currentTimeMillis()
        )
        usageDao.insertOrUpdate(newRecord)
        true
    }
}
