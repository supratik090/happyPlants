package com.hos.happy.plants.data.remote

import com.google.gson.Gson
import com.hos.happy.plants.data.remote.model.AirQualityResponse
import com.hos.happy.plants.data.remote.model.LiveEnvSnapshot
import com.hos.happy.plants.data.remote.model.WeatherResponse
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

                val sunriseRaw = weather?.daily?.sunrise?.firstOrNull()
                val sunsetRaw = weather?.daily?.sunset?.firstOrNull()

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

                Result.success(
                    LiveEnvSnapshot(
                        temperatureCelsius = weather?.current?.temperature,
                        humidityPercentage = weather?.current?.humidity,
                        aqi = finalAqi,
                        uvIndex = weather?.current?.uvIndex,
                        sunriseTime = sunriseStr,
                        sunsetTime = sunsetStr,
                        daylightHours = daylightHours
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchWeather(lat: Double, lng: Double): WeatherResponse? {
        val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current=temperature_2m,relative_humidity_2m,uv_index&daily=sunrise,sunset&timezone=auto"
        val json = executeGet(endpoint) ?: return null
        return runCatching { gson.fromJson(json, WeatherResponse::class.java) }.getOrNull()
    }

    private fun fetchAirQuality(lat: Double, lng: Double): AirQualityResponse? {
        val endpoint = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$lat&longitude=$lng&current=european_aqi,us_aqi,pm2_5,pm10&timezone=auto"
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
