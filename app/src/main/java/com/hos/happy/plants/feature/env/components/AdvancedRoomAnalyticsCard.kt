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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.ui.theme.AmberLight
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

data class DaySlotStatus(
    val title: String,        // "Morning", "Noon", "Afternoon"
    val timeRange: String,    // "Before 12 PM", "12 – 4 PM", "After 4 PM"
    val isRecorded: Boolean,
    val recordedTimeStr: String? = null
)

data class RoomDailyAnalytics(
    val roomTag: String,
    val totalTodaySnapshots: Int,
    val isUnlockedToday: Boolean,
    val todayVpdKpa: Float?,
    val todayDliMol: Float?,
    val slots: List<DaySlotStatus>,
    val lastSavedDateStr: String? = null,
    val lastSavedVpdKpa: Float? = null,
    val lastSavedDliMol: Float? = null
)

@Composable
fun AdvancedRoomAnalyticsCard(
    analytics: RoomDailyAnalytics?,
    onShowMetricInfo: (MetricType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (analytics == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Sand, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with Lock / Unlock Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (analytics.isUnlockedToday) Leaf.copy(alpha = 0.25f) else Sand.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (analytics.isUnlockedToday) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (analytics.isUnlockedToday) Forest else Bark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "VPD & DLI Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Room: ${analytics.roomTag}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Bark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (analytics.isUnlockedToday) Leaf.copy(alpha = 0.25f) else Paper)
                        .border(1.dp, if (analytics.isUnlockedToday) Moss else Sand, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (analytics.isUnlockedToday) "Unlocked" else "${analytics.totalTodaySnapshots}/3 Snapshots",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (analytics.isUnlockedToday) Forest else Mute
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Time Slots Stepper: Morning, Noon, Afternoon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                analytics.slots.forEach { slot ->
                    TimingSlotItem(slot = slot, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (analytics.isUnlockedToday) {
                // UNLOCKED TODAY: Display Calculated VPD & DLI
                MetricsDisplaySection(
                    title = "Today's Unlocked Values",
                    vpdKpa = analytics.todayVpdKpa,
                    dliMol = analytics.todayDliMol,
                    onShowMetricInfo = onShowMetricInfo
                )
            } else {
                // LOCKED TODAY: Explanatory banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Paper)
                        .border(1.dp, Sand.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Bark,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Take 3 snapshots of ${analytics.roomTag} today (Morning, Noon, and Afternoon) to unlock accurate daily VPD & DLI.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = Ink
                        )
                    }
                }

                // If this room has a previously saved unlocked day, show it!
                if (analytics.lastSavedVpdKpa != null && analytics.lastSavedDliMol != null && analytics.lastSavedDateStr != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    MetricsDisplaySection(
                        title = "Last Saved Values (${analytics.lastSavedDateStr})",
                        vpdKpa = analytics.lastSavedVpdKpa,
                        dliMol = analytics.lastSavedDliMol,
                        onShowMetricInfo = onShowMetricInfo
                    )
                }
            }
        }
    }
}

@Composable
private fun TimingSlotItem(
    slot: DaySlotStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (slot.isRecorded) Leaf.copy(alpha = 0.2f) else Paper
    val borderColor = if (slot.isRecorded) Moss else Sand.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (slot.isRecorded) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Recorded",
                        tint = Moss,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = slot.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (slot.isRecorded) Forest else Ink
                )
            }

            Text(
                text = slot.timeRange,
                fontSize = 10.sp,
                color = Mute,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (slot.recordedTimeStr != null) {
                Text(
                    text = slot.recordedTimeStr,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Forest,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    text = "Pending",
                    fontSize = 10.sp,
                    color = Sand,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricsDisplaySection(
    title: String,
    vpdKpa: Float?,
    dliMol: Float?,
    onShowMetricInfo: (MetricType) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Forest,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // VPD Metric Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Sand.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = Moss,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VPD (kPa)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Mute
                            )
                        }
                        InfoHelpButton(metricType = MetricType.VPD, onShowInfo = onShowMetricInfo)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = vpdKpa?.let { String.format(Locale.US, "%.2f", it) } ?: "--",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Forest
                    )

                    Text(
                        text = "Vapor Pressure Deficit",
                        fontSize = 10.sp,
                        color = Bark
                    )
                }
            }

            // DLI Metric Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Sand.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = AmberLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DLI (mol/m²/d)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Mute
                            )
                        }
                        InfoHelpButton(metricType = MetricType.DLI, onShowInfo = onShowMetricInfo)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = dliMol?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Forest
                    )

                    Text(
                        text = "Daily Light Integral",
                        fontSize = 10.sp,
                        color = Bark
                    )
                }
            }
        }
    }
}
