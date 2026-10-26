package com.hos.happy.plants.feature.env.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.data.preferences.LightMeterMode
import com.hos.happy.plants.data.preferences.LightMeterSettings
import com.hos.happy.plants.data.sensor.LightReading
import com.hos.happy.plants.ui.theme.AmberLight
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Sand
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.graphics.graphicsLayer
import com.hos.happy.plants.ui.theme.LeafBright
import com.hos.happy.plants.ui.theme.SurfaceWarm
import java.util.Locale

@Composable
fun LightMeterGauge(
    settings: LightMeterSettings,
    currentReading: LightReading,
    isFrozen: Boolean,
    onToggleMode: () -> Unit,
    onToggleFreeze: () -> Unit,
    onShowMetricInfo: (MetricType) -> Unit = {},
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Normalizing lux to an arc progress (0 to 12,000 lux max scale for indoor plants)
    val maxLuxScale = 12000f
    val progress = (currentReading.lux / maxLuxScale).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350),
        label = "luxProgress"
    )

    val arcColor by animateColorAsState(
        targetValue = when {
            currentReading.lux < 1000f -> Sand
            currentReading.lux < 2500f -> Leaf
            currentReading.lux < 10000f -> Moss
            else -> AmberLight
        },
        label = "arcColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Sand, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Sensor/Camera badge + Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Leaf.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (settings.preferredMode == LightMeterMode.SENSOR) Icons.Default.Sensors else Icons.Default.Cameraswitch,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (settings.preferredMode == LightMeterMode.SENSOR) "Ambient Light Sensor" else "Camera Light Meter",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Toggle Mode button (Sensor ⇄ Camera)
                OutlinedButton(
                    onClick = onToggleMode,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Sand),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Bark),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Toggle",
                        modifier = Modifier.size(16.dp),
                        tint = Moss
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (settings.preferredMode == LightMeterMode.SENSOR) "Use Camera" else "Use Sensor",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Arc Gauge
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background track arc (240 degrees from 150° to 390°)
                    drawArc(
                        color = Sand.copy(alpha = 0.35f),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active reading arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Sand, Leaf, Moss, AmberLight)
                        ),
                        startAngle = 150f,
                        sweepAngle = 240f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%,.0f", currentReading.lux),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Forest
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LUX (lx)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Mute,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        InfoHelpButton(metricType = MetricType.LIGHT_LUX, onShowInfo = onShowMetricInfo)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f fc", currentReading.footCandles),
                        fontSize = 13.sp,
                        color = Bark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val infiniteTransition = rememberInfiniteTransition(label = "holdHighlight")
            val holdPulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = tween(750, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "holdPulseScale"
            )
            val holdGlowBorder by infiniteTransition.animateColor(
                initialValue = Moss,
                targetValue = LeafBright,
                animationSpec = infiniteRepeatable(
                    animation = tween(750, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "holdGlowBorder"
            )
            val holdBgColor by infiniteTransition.animateColor(
                initialValue = Leaf.copy(alpha = 0.12f),
                targetValue = Leaf.copy(alpha = 0.32f),
                animationSpec = infiniteRepeatable(
                    animation = tween(750, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "holdBgColor"
            )

            // Hold / Freeze Button
            OutlinedButton(
                onClick = onToggleFreeze,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isHighlighted && !isFrozen) 2.dp else 1.dp,
                    color = when {
                        isFrozen -> AmberLight
                        isHighlighted -> holdGlowBorder
                        else -> Sand
                    }
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = when {
                        isFrozen -> AmberLight.copy(alpha = 0.15f)
                        isHighlighted -> holdBgColor
                        else -> Color.Transparent
                    }
                ),
                modifier = if (isHighlighted && !isFrozen) {
                    Modifier.graphicsLayer {
                        scaleX = holdPulseScale
                        scaleY = holdPulseScale
                    }
                } else Modifier
            ) {
                Icon(
                    imageVector = if (isFrozen) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = when {
                        isFrozen -> AmberLight
                        isHighlighted -> Forest
                        else -> Moss
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFrozen) "Reading Locked (Tap to Unlock)" else "Hold Reading",
                    color = when {
                        isFrozen -> Bark
                        isHighlighted -> Forest
                        else -> Moss
                    },
                    fontWeight = if (isHighlighted && !isFrozen) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}
