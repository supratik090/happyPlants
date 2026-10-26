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
    val uvIndex: Float? = null,
    @SerializedName("precipitation")
    val precipitationMm: Float? = null,
    @SerializedName("rain")
    val rainMm: Float? = null
)

data class DailyWeather(
    @SerializedName("sunrise")
    val sunrise: List<String>? = null,
    @SerializedName("sunset")
    val sunset: List<String>? = null,
    @SerializedName("precipitation_sum")
    val precipitationSum: List<Float>? = null,
    @SerializedName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int>? = null,
    @SerializedName("sunshine_duration")
    val sunshineDuration: List<Float>? = null,
    @SerializedName("relative_humidity_2m_mean")
    val relativeHumidityMean: List<Float>? = null
)

data class AirQualityResponse(
    @SerializedName("current")
    val current: CurrentAirQuality? = null,
    @SerializedName("hourly")
    val hourly: HourlyAirQuality? = null
)

data class HourlyAirQuality(
    @SerializedName("pm10")
    val pm10: List<Float?>? = null,
    @SerializedName("pm2_5")
    val pm25: List<Float?>? = null,
    @SerializedName("european_aqi")
    val europeanAqi: List<Int?>? = null
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
    val locationName: String? = null,
    val currentRainMm: Float? = null,
    val dailyRainMm: Float? = null,
    val rainProbability: Int? = null,
    val weeklyMacro: WeeklyMacroClimate? = null
)
