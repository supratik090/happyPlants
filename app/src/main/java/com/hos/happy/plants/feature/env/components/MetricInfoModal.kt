package com.hos.happy.plants.feature.env.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import kotlinx.coroutines.delay

enum class MetricType {
    TEMPERATURE,
    HUMIDITY,
    AQI,
    UV_INDEX,
    DAYLIGHT,
    LIGHT_LUX,
    VPD,
    DLI
}

data class MetricExplanation(
    val title: String,
    val icon: ImageVector,
    val simpleExplanation: String,
    val plantTip: String
)

object MetricInfoProvider {
    fun getExplanation(type: MetricType): MetricExplanation {
        return when (type) {
            MetricType.TEMPERATURE -> MetricExplanation(
                title = "Temperature",
                icon = Icons.Default.Thermostat,
                simpleExplanation = "Warmth controls how quickly your plant drinks and grows. Most indoor plants love comfortable room temps between 18°C and 28°C.",
                plantTip = "Keep plants away from cold air conditioners and hot direct heater drafts."
            )
            MetricType.HUMIDITY -> MetricExplanation(
                title = "Relative Humidity",
                icon = Icons.Default.WaterDrop,
                simpleExplanation = "Moisture in the air prevents brown leaf edges. Tropical foliage plants (like Monsteras, Pothos, and Calatheas) thrive in 50% to 70% humidity.",
                plantTip = "If indoor air is dry, mist leaves occasionally or place plants on a pebble water tray."
            )
            MetricType.AQI -> MetricExplanation(
                title = "Air Quality (AQI)",
                icon = Icons.Default.Air,
                simpleExplanation = "Indoor dust and airborne soot settle on leaf surfaces, clogging pores and reducing sunlight absorption.",
                plantTip = "Gently wipe leaves with a soft, damp cloth every few weeks to keep them breathing."
            )
            MetricType.UV_INDEX -> MetricExplanation(
                title = "UV Index",
                icon = Icons.Default.WbSunny,
                simpleExplanation = "Indicates the intensity of harsh rays. Strong afternoon sun entering windows can scorch and sunburn delicate indoor foliage.",
                plantTip = "Use sheer curtains to filter harsh midday glare into gentle, bright indirect light."
            )
            MetricType.DAYLIGHT -> MetricExplanation(
                title = "Natural Daylight",
                icon = Icons.Default.WbSunny,
                simpleExplanation = "Total hours between sunrise and sunset. Plants use daylight as natural fuel to produce food and new leaves.",
                plantTip = "In winter when days are shorter, move plants closer to windows to capture available light."
            )
            MetricType.LIGHT_LUX -> MetricExplanation(
                title = "Light Level (Lux / fc)",
                icon = Icons.Default.LightMode,
                simpleExplanation = "Light is plant food! Snake Plants tolerate low light (under 1,000 lx), while Monsteras and Ficus need bright indirect light (over 2,500 lx).",
                plantTip = "Hold your phone right at leaf level with screen pointing up to measure what your plant actually receives."
            )
            MetricType.VPD -> MetricExplanation(
                title = "VPD (Plant Breathing Rate)",
                icon = Icons.Default.Spa,
                simpleExplanation = "Vapor Pressure Deficit measures how comfortably your plant can pull water from its roots and release vapor through its leaves.",
                plantTip = "A balanced VPD (0.8 to 1.2 kPa) means ideal growth. High VPD means air is too dry; low VPD means air is stagnant."
            )
            MetricType.DLI -> MetricExplanation(
                title = "DLI (Daily Light 'Meals')",
                icon = Icons.Default.WbSunny,
                simpleExplanation = "Daily Light Integral is the total amount of light energy accumulated by your plant across the entire day, from morning to evening.",
                plantTip = "3 daily checks (Morning, Noon, Afternoon) calculate your room's full daily light score."
            )
        }
    }
}

@Composable
fun InfoHelpButton(
    metricType: MetricType,
    onShowInfo: (MetricType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .clickable { onShowInfo(metricType) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = "Help",
            tint = Mute,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
fun MetricInfoModal(
    metricType: MetricType?,
    onDismiss: () -> Unit
) {
    if (metricType == null) return
    val info = remember(metricType) { MetricInfoProvider.getExplanation(metricType) }

    var remainingSeconds by remember(metricType) { mutableIntStateOf(5) }

    LaunchedEffect(metricType) {
        remainingSeconds = 5
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.5.dp, Sand, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header with icon, title, and auto-close countdown chip
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
                            Icon(info.icon, contentDescription = null, tint = Forest, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = info.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Paper)
                            .border(1.dp, Sand.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Auto-closes ${remainingSeconds}s",
                            fontSize = 10.sp,
                            color = Mute,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = info.simpleExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Paper)
                        .border(1.dp, Sand.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🌿 Plant Tip: ${info.plantTip}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Bark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss", color = Moss, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
