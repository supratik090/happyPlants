package com.hos.happy.plants.data.local.util

import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import kotlin.math.exp

object EnvCalculators {

    /**
     * Calculates Vapor Pressure Deficit (VPD in kPa) using the Tetens equation:
     * VP_sat = 0.61078 * exp((17.27 * T) / (T + 237.3))
     * VPD = VP_sat * (1 - RH / 100)
     */
    fun calculateVpdKpa(temperatureCelsius: Float?, humidityPercentage: Float?): Float? {
        if (temperatureCelsius == null || humidityPercentage == null) return null
        val t = temperatureCelsius.toDouble()
        val rh = humidityPercentage.toDouble().coerceIn(0.0, 100.0)

        // Saturation Vapor Pressure in kPa
        val vpSat = 0.61078 * exp((17.27 * t) / (t + 237.3))
        val vpd = vpSat * (1.0 - (rh / 100.0))
        return vpd.toFloat().coerceAtLeast(0f)
    }

    /**
     * Calculates Daily Light Integral (DLI in mol/m²/day) from Lux readings and daylight hours:
     * 1. Lux to PPFD (μmol/m²/s) ≈ Lux / 54 (natural sunlight conversion)
     * 2. Daily Photoperiod in seconds = daylightHours * 3600 (default 12 hours if daylightHours is null)
     * 3. DLI = avg(PPFD) * photoperiodSeconds * 10^-6
     */
    fun calculateDliMol(luxReadings: List<Float>, daylightHours: Float?): Float? {
        if (luxReadings.isEmpty()) return null
        val avgLux = luxReadings.average().toFloat()
        // Natural sunlight conversion: 1 μmol/m²/s ≈ 54 lux
        val avgPpfd = (avgLux / 54f).coerceAtLeast(0f)
        val photoperiodHours = daylightHours ?: 12f
        val photoperiodSeconds = photoperiodHours * 3600f
        val dli = avgPpfd * photoperiodSeconds * 1e-6f
        return dli
    }

    /**
     * Calculates average VPD from a list of snapshots.
     */
    fun calculateAverageVpd(snapshots: List<RoomEnvSnapshotEntity>): Float? {
        val validVpds = snapshots.mapNotNull {
            calculateVpdKpa(it.temperatureCelsius, it.humidityPercentage)
        }
        return if (validVpds.isNotEmpty()) validVpds.average().toFloat() else null
    }
}
