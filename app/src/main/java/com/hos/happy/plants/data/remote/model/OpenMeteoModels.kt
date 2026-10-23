package com.hos.happy.plants.data.remote.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("current")
    val current: CurrentWeather? = null,
    @SerializedName("daily")
    val daily: DailyWeather? = null
)

data class CurrentWeather(
    @SerializedName("temperature_2m")
    val temperature: Float? = null,
    @SerializedName("relative_humidity_2m")
    val humidity: Float? = null,
    @SerializedName("uv_index")
    val uvIndex: Float? = null
)

data class DailyWeather(
    @SerializedName("sunrise")
    val sunrise: List<String>? = null,
    @SerializedName("sunset")
    val sunset: List<String>? = null
)

data class AirQualityResponse(
    @SerializedName("current")
    val current: CurrentAirQuality? = null
)

data class CurrentAirQuality(
    @SerializedName("us_aqi")
    val usAqi: Int? = null,
    @SerializedName("european_aqi")
    val europeanAqi: Int? = null,
    @SerializedName("pm2_5")
    val pm25: Float? = null,
    @SerializedName("pm10")
    val pm10: Float? = null
)

data class LiveEnvSnapshot(
    val temperatureCelsius: Float?,
    val humidityPercentage: Float?,
    val aqi: Int?,
    val uvIndex: Float?,
    val sunriseTime: String?,
    val sunsetTime: String?,
    val daylightHours: Float?,
    val locationName: String? = null
)
