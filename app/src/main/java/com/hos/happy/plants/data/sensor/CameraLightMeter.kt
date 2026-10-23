package com.hos.happy.plants.data.sensor

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class CameraLightMeter(
    private val onLightCalculated: (LightReading) -> Unit
) : ImageAnalysis.Analyzer {

    private var lastCalculationTime = 0L

    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        // Throttle updates to ~150ms for smooth UI performance
        if (currentTime - lastCalculationTime < 150) {
            image.close()
            return
        }
        lastCalculationTime = currentTime

        try {
            val planes = image.planes
            if (planes.isNotEmpty()) {
                val buffer: ByteBuffer = planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)

                // Sample every 4th pixel for speed
                var sum = 0L
                var count = 0
                val step = 4
                for (i in data.indices step step) {
                    sum += (data[i].toInt() and 0xFF)
                    count++
                }

                val avgLuminance = if (count > 0) (sum.toFloat() / count) else 0f
                // Normalizing avgLuminance (0 - 255) to realistic indoor/window lux range (~50 to 12,000 lx)
                // Using non-linear perceived brightness curve: Lux = base * (Y/255)^2.2
                val normalized = (avgLuminance / 255f).coerceIn(0f, 1f)
                val estimatedLux = kotlin.math.pow(normalized.toDouble(), 2.2).toFloat() * 12000f + (normalized * 80f)

                onLightCalculated(
                    LightReading(
                        lux = estimatedLux,
                        footCandles = estimatedLux * 0.092903f,
                        source = "CAMERA"
                    )
                )
            }
        } catch (e: Exception) {
            // Keep stream alive
        } finally {
            image.close()
        }
    }
}
