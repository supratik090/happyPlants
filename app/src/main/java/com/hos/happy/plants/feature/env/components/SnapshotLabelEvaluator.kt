package com.hos.happy.plants.feature.env.components

import androidx.compose.ui.graphics.Color
import com.hos.happy.plants.ui.theme.DustyTagBg
import com.hos.happy.plants.ui.theme.DustyTagText
import com.hos.happy.plants.ui.theme.HappyTagBg
import com.hos.happy.plants.ui.theme.HappyTagText
import com.hos.happy.plants.ui.theme.HungryTagBg
import com.hos.happy.plants.ui.theme.HungryTagText
import com.hos.happy.plants.ui.theme.ThirstyTagBg
import com.hos.happy.plants.ui.theme.ThirstyTagText
import java.util.Locale
import kotlin.math.abs

data class MetricLabel(
    val text: String,
    val textColor: Color,
    val backgroundColor: Color
)

object SnapshotLabelEvaluator {

    /**
     * Light (lux):
     * Dark: below 200
     * Dim: 200–1,000
     * Bright indirect: 1,000–5,000
     * Very bright: 5,000–20,000
     * Sunny: above 20,000
     */
    fun evaluateLight(lux: Float): MetricLabel {
        return when {
            lux < 200f -> MetricLabel(
                text = "Dark",
                textColor = Color(0xFF455A64),
                backgroundColor = Color(0xFFECEFF1)
            )
            lux <= 1000f -> MetricLabel(
                text = "Dim",
                textColor = HungryTagText,
                backgroundColor = HungryTagBg
            )
            lux <= 5000f -> MetricLabel(
                text = "Bright indirect",
                textColor = HappyTagText,
                backgroundColor = HappyTagBg
            )
            lux <= 20000f -> MetricLabel(
                text = "Very bright",
                textColor = Color(0xFF2E7D32),
                backgroundColor = Color(0xFFE8F5E9)
            )
            else -> MetricLabel(
                text = "Sunny",
                textColor = Color(0xFFB26A00),
                backgroundColor = Color(0xFFFFF8E1)
            )
        }
    }

    /**
     * Temperature (°C):
     * Cold: below 15
     * Cool: 15–20
     * Comfortable: 20–28
     * Warm: 28–35
     * Hot: above 35
     */
    fun evaluateTemperature(celsius: Float?): MetricLabel {
        if (celsius == null) {
            return MetricLabel("Moderate", HappyTagText, HappyTagBg)
        }
        return when {
            celsius < 15f -> MetricLabel(
                text = "Cold",
                textColor = Color(0xFF1565C0),
                backgroundColor = Color(0xFFE3F2FD)
            )
            celsius <= 20f -> MetricLabel(
                text = "Cool",
                textColor = Color(0xFF00796B),
                backgroundColor = Color(0xFFE0F2F1)
            )
            celsius <= 28f -> MetricLabel(
                text = "Comfortable",
                textColor = HappyTagText,
                backgroundColor = HappyTagBg
            )
            celsius <= 35f -> MetricLabel(
                text = "Warm",
                textColor = ThirstyTagText,
                backgroundColor = ThirstyTagBg
            )
            else -> MetricLabel(
                text = "Hot",
                textColor = Color(0xFFC62828),
                backgroundColor = Color(0xFFFFEBEE)
            )
        }
    }

    /**
     * Humidity (%):
     * Dry: below 40
     * Moderate: 40–60
     * Humid: 60–75
     * Very humid: above 75
     */
    fun evaluateHumidity(humidity: Float?): MetricLabel {
        if (humidity == null) {
            return MetricLabel("Moderate", HappyTagText, HappyTagBg)
        }
        return when {
            humidity < 40f -> MetricLabel(
                text = "Dry",
                textColor = ThirstyTagText,
                backgroundColor = ThirstyTagBg
            )
            humidity <= 60f -> MetricLabel(
                text = "Moderate",
                textColor = HappyTagText,
                backgroundColor = HappyTagBg
            )
            humidity <= 75f -> MetricLabel(
                text = "Humid",
                textColor = Color(0xFF0277BD),
                backgroundColor = Color(0xFFE1F5FE)
            )
            else -> MetricLabel(
                text = "Very humid",
                textColor = Color(0xFF1565C0),
                backgroundColor = Color(0xFFE3F2FD)
            )
        }
    }

    fun getSpaceHeadline(lux: Float, tempC: Float?, humidity: Float?): String {
        val light = evaluateLight(lux).text
        val temp = evaluateTemperature(tempC).text
        val hum = evaluateHumidity(humidity).text
        return "$light • $temp • $hum"
    }

    fun getSpaceAdvice(lux: Float, tempC: Float?, humidity: Float?): String {
        val light = evaluateLight(lux).text
        return when (light) {
            "Dark", "Dim" -> "Good for ferns and peace lilies. Too dim for flowering plants."
            "Bright indirect" -> "Ideal sweet spot for monsteras, philodendrons, and most tropical houseplants."
            "Very bright" -> "Great for ficus, hoyas, and herbs. Turn plants periodically for even growth."
            "Sunny" -> "Great for succulents, cacti, and sun lovers. Watch out for midday leaf scorching."
            else -> "Comfortable conditions for standard indoor plants."
        }
    }

    fun formatLightTrend(currentLux: Float, prevLux: Float): String {
        val diff = currentLux - prevLux
        return when {
            abs(diff) < 50f -> "Steady: similar to last snapshot"
            diff > 0 -> "Rising: up ${diff.toInt()} lux from last snapshot"
            else -> "Falling: down ${(-diff).toInt()} lux from last snapshot"
        }
    }

    fun formatTemperatureTrend(currentTemp: Float?, prevTemp: Float?): String {
        if (currentTemp == null || prevTemp == null) return "Steady temperature"
        val diff = currentTemp - prevTemp
        return when {
            abs(diff) < 0.8f -> "Steady between ${minOf(currentTemp, prevTemp).toInt()} and ${maxOf(currentTemp, prevTemp).toInt()}°C"
            diff > 0 -> "Rising: up ${String.format(Locale.US, "%.1f", diff)}°C from last snapshot"
            else -> "Falling: down ${String.format(Locale.US, "%.1f", -diff)}°C from last snapshot"
        }
    }

    fun formatHumidityTrend(currentHum: Float?, prevHum: Float?): String {
        if (currentHum == null || prevHum == null) return "Steady humidity"
        val diff = currentHum - prevHum
        return when {
            abs(diff) < 2f -> "Steady at ${currentHum.toInt()}%"
            diff > 0 -> "Rising: up ${diff.toInt()} points from last snapshot"
            else -> "Falling: down ${(-diff).toInt()} points from last snapshot"
        }
    }

    fun evaluateDustBadge(aqi: Int?): MetricLabel {
        val a = aqi ?: 100
        return when {
            a < 50 -> MetricLabel("Clean", HappyTagText, HappyTagBg)
            a <= 100 -> MetricLabel("Moderate", HungryTagText, HungryTagBg)
            else -> MetricLabel("Dusty", DustyTagText, DustyTagBg)
        }
    }

    fun getAqiCategory(aqi: Int): String {
        return when {
            aqi <= 50 -> "good"
            aqi <= 100 -> "moderate"
            aqi <= 200 -> "poor"
            aqi <= 300 -> "very poor"
            else -> "hazardous"
        }
    }
}
