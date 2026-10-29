package com.hos.happy.plants.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.TimeZone

/**
 * Represents the three custom watering schedule patterns the user can choose per plant.
 * Stored in PlantEntity as (waterScheduleType, waterScheduleValue) columns.
 */
sealed class WateringSchedule {

    /** Water every [intervalDays] days from today (fresh start anchor). */
    data class Interval(val intervalDays: Int) : WateringSchedule()

    /**
     * Water on specific days of the week.
     * [weekdays]: 1=Monday … 7=Sunday (ISO-8601 week day convention).
     */
    data class Weekdays(val weekdays: List<Int>) : WateringSchedule()

    /**
     * Water on specific dates of the month (1..28).
     * [dates]: sorted list of calendar dates, e.g. [1, 10, 20].
     */
    data class Monthly(val dates: List<Int>) : WateringSchedule()

    companion object {
        private val gson = Gson()

        fun fromEntity(type: String?, value: String?): WateringSchedule? {
            if (type == null || value == null) return null
            return runCatching {
                when (type) {
                    "INTERVAL" -> {
                        val map: Map<String, Double> = gson.fromJson(value, object : TypeToken<Map<String, Double>>() {}.type)
                        Interval((map["days"] ?: 7.0).toInt())
                    }
                    "WEEKDAYS" -> {
                        val map: Map<String, List<Double>> = gson.fromJson(value, object : TypeToken<Map<String, List<Double>>>() {}.type)
                        Weekdays((map["weekdays"] ?: emptyList()).map { it.toInt() })
                    }
                    "MONTHLY" -> {
                        val map: Map<String, List<Double>> = gson.fromJson(value, object : TypeToken<Map<String, List<Double>>>() {}.type)
                        Monthly((map["dates"] ?: emptyList()).map { it.toInt() })
                    }
                    else -> null
                }
            }.getOrNull()
        }

        fun Interval.toJson(): String = gson.toJson(mapOf("days" to intervalDays))
        fun Weekdays.toJson(): String = gson.toJson(mapOf("weekdays" to weekdays))
        fun Monthly.toJson(): String = gson.toJson(mapOf("dates" to dates))

        fun WateringSchedule.typeString(): String = when (this) {
            is Interval -> "INTERVAL"
            is Weekdays -> "WEEKDAYS"
            is Monthly -> "MONTHLY"
        }

        fun WateringSchedule.toJson(): String = when (this) {
            is Interval -> toJson()
            is Weekdays -> toJson()
            is Monthly -> toJson()
        }

        /** Returns a human-readable label shown in Garden schedule list. */
        fun WateringSchedule.scheduleLabel(): String = when (this) {
            is Interval -> "Every ${intervalDays}d"
            is Weekdays -> {
                val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                weekdays.sorted().joinToString(", ") { names.getOrElse(it - 1) { "?" } }
            }
            is Monthly -> {
                fun ordinal(n: Int) = when {
                    n in 11..13 -> "${n}th"
                    n % 10 == 1 -> "${n}st"
                    n % 10 == 2 -> "${n}nd"
                    n % 10 == 3 -> "${n}rd"
                    else -> "${n}th"
                }
                dates.sorted().joinToString(", ") { ordinal(it) }
            }
        }

        fun WateringSchedule?.scheduleLabel(default: String = ""): String = this?.scheduleLabel() ?: default

        fun WateringSchedule.nextWateringEpoch(
            nowMillis: Long,
            lastWateredAt: Long?,
            scheduleSetAt: Long?
        ): Long {
            val days = daysUntilNext(nowMillis, lastWateredAt, scheduleSetAt)
            return nowMillis + (days.toLong() * 86_400_000L)
        }

        private val istTz = TimeZone.getTimeZone("Asia/Kolkata")

        /**
         * Given today's epoch millis, calculates how many days until the next
         * watering under this schedule. Returns 0 if due today or overdue.
         */
        fun WateringSchedule.daysUntilNext(
            nowMillis: Long,
            lastWateredAt: Long?,
            scheduleSetAt: Long?
        ): Int {
            val cal = Calendar.getInstance(istTz).apply { timeInMillis = nowMillis }
            return when (this) {
                is Interval -> {
                    val anchor = scheduleSetAt ?: lastWateredAt ?: nowMillis
                    val daysSince = ((nowMillis - anchor) / 86_400_000L).toInt()
                    maxOf(0, intervalDays - daysSince)
                }
                is Weekdays -> {
                    if (weekdays.isEmpty()) return Int.MAX_VALUE
                    // ISO weekday: 1=Mon…7=Sun. Calendar.DAY_OF_WEEK: 1=Sun…7=Sat
                    val todayIso = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1 // convert
                    val sorted = weekdays.sorted()
                    val next = sorted.firstOrNull { it >= todayIso } ?: sorted.first()
                    if (next == todayIso) 0
                    else if (next > todayIso) next - todayIso
                    else 7 - todayIso + next
                }
                is Monthly -> {
                    if (dates.isEmpty()) return Int.MAX_VALUE
                    val todayDate = cal.get(Calendar.DAY_OF_MONTH)
                    val sorted = dates.sorted()
                    val nextDate = sorted.firstOrNull { it >= todayDate } ?: sorted.first()
                    if (nextDate == todayDate) 0
                    else if (nextDate > todayDate) nextDate - todayDate
                    else {
                        // next occurrence is in following month
                        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        daysInMonth - todayDate + nextDate
                    }
                }
            }
        }

        /**
         * Returns true if the plant should be watered TODAY under this schedule.
         * For Interval: computes from anchor. For Weekdays/Monthly: checks today's date.
         */
        fun WateringSchedule.isDueToday(
            nowMillis: Long,
            lastWateredAt: Long?,
            scheduleSetAt: Long?
        ): Boolean = daysUntilNext(nowMillis, lastWateredAt, scheduleSetAt) == 0
    }
}
