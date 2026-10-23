package com.hos.happy.plants.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class LightReading(
    val lux: Float,
    val footCandles: Float = lux * 0.092903f,
    val source: String = "SENSOR" // "SENSOR" or "CAMERA"
)

class LightSensorManager(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    val isSensorAvailable: Boolean
        get() = lightSensor != null

    fun getLightReadingFlow(): Flow<LightReading> = callbackFlow {
        if (sensorManager == null || lightSensor == null) {
            // No hardware light sensor
            trySend(LightReading(lux = 0f, source = "NONE"))
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LIGHT && event.values.isNotEmpty()) {
                    val rawLux = event.values[0]
                    trySend(LightReading(lux = rawLux, source = "SENSOR"))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No-op for light sensor
            }
        }

        val registered = sensorManager.registerListener(
            listener,
            lightSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        if (!registered) {
            close()
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
