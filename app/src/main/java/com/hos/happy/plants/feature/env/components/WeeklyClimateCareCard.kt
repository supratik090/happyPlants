package com.hos.happy.plants.feature.env.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.data.remote.model.WeeklyMacroClimate
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import java.util.Locale

@Composable
fun WeeklyClimateCareCard(
    weeklyData: WeeklyMacroClimate?,
    modifier: Modifier = Modifier
) {
    if (weeklyData == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Sand, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Forest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Leaf,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Last 7 Days Climate",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cumulative weather & adaptive plant care insights",
                        style = MaterialTheme.typography.labelSmall,
                        color = Mute
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Summary Metrics (Rain, Sunlight, Humidity, Dust)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyMiniPill(
                    icon = "🌧️",
                    label = "Rain",
                    value = String.format(Locale.US, "%.1f mm", weeklyData.totalRainMm),
                    modifier = Modifier.weight(1f)
                )
                WeeklyMiniPill(
                    icon = "☀️",
                    label = "Sunlight",
                    value = String.format(Locale.US, "%.1fh/d", weeklyData.avgSunlightHoursPerDay),
                    modifier = Modifier.weight(1f)
                )
                WeeklyMiniPill(
                    icon = "💧",
                    label = "Humidity",
                    value = String.format(Locale.US, "%.0f%%", weeklyData.avgHumidityPercent),
                    modifier = Modifier.weight(1f)
                )
                WeeklyMiniPill(
                    icon = "🌫️",
                    label = "Dust",
                    value = weeklyData.avgPm10?.let { String.format(Locale.US, "%.0fµg", it) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Adaptive Care Statements Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Moss,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Weekly Care Insights",
                    style = MaterialTheme.typography.labelMedium,
                    color = Bark,
                    fontWeight = FontWeight.Bold
                )
            }

            // Statements Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Paper)
                    .border(1.dp, Sand.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CareStatementItem(text = weeklyData.rainStatement)
                CareStatementItem(text = weeklyData.sunlightStatement)
                CareStatementItem(text = weeklyData.dustStatement)
                CareStatementItem(text = weeklyData.humidityStatement)
            }
        }
    }
}

@Composable
private fun WeeklyMiniPill(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Paper)
            .border(1.dp, Sand.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Forest,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = Mute
            )
        }
    }
}

@Composable
private fun CareStatementItem(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Ink,
        lineHeight = 17.sp
    )
}
