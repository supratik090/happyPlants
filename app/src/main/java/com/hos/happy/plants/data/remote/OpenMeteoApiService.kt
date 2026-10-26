package com.hos.happy.plants.data.remote

import com.google.gson.Gson
import com.hos.happy.plants.data.remote.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class OpenMeteoApiService {

    private val gson = Gson()

    suspend fun fetchLiveEnvironment(latitude: Double, longitude: Double): Result<LiveEnvSnapshot> = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                val weatherDeferred = async { fetchWeather(latitude, longitude) }
                val aqiDeferred = async { fetchAirQuality(latitude, longitude) }

                val weather = weatherDeferred.await()
                val aqi = aqiDeferred.await()

                var daylightHours: Float? = null
                var sunriseStr: String? = null
                var sunsetStr: String? = null

                val todayIndex = if ((weather?.daily?.sunrise?.size ?: 0) > 7) 7 else 0
                val sunriseRaw = weather?.daily?.sunrise?.getOrNull(todayIndex) ?: weather?.daily?.sunrise?.firstOrNull()
                val sunsetRaw = weather?.daily?.sunset?.getOrNull(todayIndex) ?: weather?.daily?.sunset?.firstOrNull()

                if (sunriseRaw != null && sunsetRaw != null) {
                    try {
                        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                        val displayFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

                        val sunriseDate = isoFormat.parse(sunriseRaw)
                        val sunsetDate = isoFormat.parse(sunsetRaw)

                        if (sunriseDate != null && sunsetDate != null) {
                            sunriseStr = displayFormat.format(sunriseDate)
                            sunsetStr = displayFormat.format(sunsetDate)
                            val diffMillis = sunsetDate.time - sunriseDate.time
                            daylightHours = (diffMillis / (1000f * 60f * 60f))
                        }
                    } catch (e: Exception) {
                        // Keep raw if parse fails
                        sunriseStr = sunriseRaw
                        sunsetStr = sunsetRaw
                    }
                }

                val finalAqi = aqi?.current?.usAqi ?: aqi?.current?.europeanAqi

                val currentRain = weather?.current?.rainMm ?: weather?.current?.precipitationMm
                val dailyRain = weather?.daily?.precipitationSum?.getOrNull(todayIndex)
                val rainProb = weather?.daily?.precipitationProbabilityMax?.getOrNull(todayIndex)

                // Compute 7-day past metrics and care statements
                val weeklyMacro = computeWeeklyMacro(weather, aqi)

                Result.success(
                    LiveEnvSnapshot(
                        temperatureCelsius = weather?.current?.temperature,
                        humidityPercentage = weather?.current?.humidity,
                        aqi = finalAqi,
                        uvIndex = weather?.current?.uvIndex,
                        sunriseTime = sunriseStr,
                        sunsetTime = sunsetStr,
                        daylightHours = daylightHours,
                        currentRainMm = currentRain,
                        dailyRainMm = dailyRain,
                        rainProbability = rainProb,
                        weeklyMacro = weeklyMacro
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun computeWeeklyMacro(weather: WeatherResponse?, aqi: AirQualityResponse?): WeeklyMacroClimate {
        // Past 7 completed days from Open-Meteo
        val past7RainList = weather?.daily?.precipitationSum?.take(7) ?: emptyList()
        val totalRain = past7RainList.sum()

        val past7Sunshine = weather?.daily?.sunshineDuration?.take(7) ?: emptyList()
        val totalSunshineHours = past7Sunshine.sum() / 3600f
        val avgSunlightPerDay = if (past7Sunshine.isNotEmpty()) totalSunshineHours / past7Sunshine.size else 4.5f

        val past7Humidity = weather?.daily?.relativeHumidityMean?.take(7) ?: emptyList()
        val avgHumidity = if (past7Humidity.isNotEmpty()) past7Humidity.average().toFloat() else (weather?.current?.humidity ?: 55f)

        // 168 hours = 24h * 7 days
        val past168Pm10 = aqi?.hourly?.pm10?.take(168)?.filterNotNull() ?: emptyList()
        val avgPm10 = if (past168Pm10.isNotEmpty()) past168Pm10.average().toFloat() else aqi?.current?.pm10

        val past168Aqi = aqi?.hourly?.europeanAqi?.take(168)?.filterNotNull() ?: emptyList()
        val avgAqi = if (past168Aqi.isNotEmpty()) past168Aqi.average().toInt() else (aqi?.current?.europeanAqi ?: aqi?.current?.usAqi)

        // 1. Rain & Watering statement
        val rainStatement = when {
            totalRain >= 25f -> "🌧️ Heavy rainfall this week (${String.format(Locale.US, "%.1f", totalRain)} mm). Outdoor and balcony soil is saturated — postpone manual watering."
            totalRain in 5f..24.9f -> "🌦️ Moderate rain this week (${String.format(Locale.US, "%.1f", totalRain)} mm). Check the top 2–3 cm of soil before watering."
            else -> "☀️ Dry week with minimal rain (${String.format(Locale.US, "%.1f", totalRain)} mm). Outdoor and balcony plants need your regular watering routine."
        }

        // 2. Sunlight statement
        val sunlightStatement = when {
            avgSunlightPerDay >= 6f -> "☀️ Abundant direct sunlight (~${String.format(Locale.US, "%.1f", avgSunlightPerDay)}h/day). Active growth! Turn pots a quarter turn weekly so stems grow evenly."
            avgSunlightPerDay in 3f..5.9f -> "⛅ Moderate light this week (~${String.format(Locale.US, "%.1f", avgSunlightPerDay)}h/day). Healthy steady growth for indoor foliage plants."
            else -> "☁️ Overcast / low-light week (~${String.format(Locale.US, "%.1f", avgSunlightPerDay)}h/day). Plants metabolize water slower in dim light; avoid overwatering."
        }

        // 3. Humidity statement
        val humidityStatement = when {
            avgHumidity >= 70f -> "💧 Tropical high humidity (${String.format(Locale.US, "%.0f", avgHumidity)}% avg). Perfect for Monsteras, Calatheas, and ferns. Ensure gentle air circulation."
            avgHumidity in 45f..69.9f -> "🌿 Balanced humidity (${String.format(Locale.US, "%.0f", avgHumidity)}% avg). Comfortable conditions for all common houseplants."
            else -> "🏜️ Dry air this week (${String.format(Locale.US, "%.0f", avgHumidity)}% avg). Leaf tips may crisp; consider misting or grouping plants on a pebble tray."
        }

        // 4. Dust & Air Quality statement
        val dustStatement = when {
            (avgPm10 != null && avgPm10 >= 45f) || (avgAqi != null && avgAqi >= 55) -> {
                val pmStr = avgPm10?.let { "${String.format(Locale.US, "%.0f", it)} µg/m³" } ?: "elevated"
                "🌫️ High dust & soot this week (PM10 avg $pmStr). Dust clogs leaf pores — wipe leaves with a soft damp cloth to restore breathing."
            }
            else -> "✨ Clean air this week. Leaf stomata are breathing freely and light absorption is optimal."
        }

        return WeeklyMacroClimate(
            totalRainMm = totalRain,
            avgSunlightHoursPerDay = avgSunlightPerDay,
            totalSunlightHours = totalSunshineHours,
            avgHumidityPercent = avgHumidity,
            avgPm10 = avgPm10,
            avgAqi = avgAqi,
            rainStatement = rainStatement,
            sunlightStatement = sunlightStatement,
            humidityStatement = humidityStatement,
            dustStatement = dustStatement
        )
    }

    private fun fetchWeather(lat: Double, lng: Double): WeatherResponse? {
        val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current=temperature_2m,relative_humidity_2m,uv_index,precipitation,rain&daily=sunrise,sunset,precipitation_sum,precipitation_probability_max,sunshine_duration,relative_humidity_2m_mean&past_days=7&timezone=auto"
        val json = executeGet(endpoint) ?: return null
        return runCatching { gson.fromJson(json, WeatherResponse::class.java) }.getOrNull()
    }

    private fun fetchAirQuality(lat: Double, lng: Double): AirQualityResponse? {
        val endpoint = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$lat&longitude=$lng&current=european_aqi,us_aqi,pm2_5,pm10&hourly=pm10,pm2_5,european_aqi&past_days=7&timezone=auto"
        val json = executeGet(endpoint) ?: return null
        return runCatching { gson.fromJson(json, AirQualityResponse::class.java) }.getOrNull()
    }

    private fun executeGet(urlString: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                    reader.readText()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
