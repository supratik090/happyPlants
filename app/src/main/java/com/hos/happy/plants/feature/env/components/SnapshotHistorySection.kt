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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import com.hos.happy.plants.ui.theme.AmberLight
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SkyBlue
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.Terracotta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SnapshotHistorySection(
    snapshots: List<RoomEnvSnapshotEntity>,
    onDeleteSnapshot: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header: Room Snapshots (Last snapshot per space)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Forest,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Room Snapshots",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Last recorded snapshot per space",
                        style = MaterialTheme.typography.labelSmall,
                        color = Bark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Snapshot cards or Empty State
        if (snapshots.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Sand,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No snapshots recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mute
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                snapshots.forEach { snapshot ->
                    SnapshotCardItem(
                        snapshot = snapshot,
                        onDeleteSnapshot = onDeleteSnapshot
                    )
                }
            }
        }
    }
}


@Composable
private fun SnapshotCardItem(
    snapshot: RoomEnvSnapshotEntity,
    onDeleteSnapshot: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateTimeStr = remember(snapshot.recordedAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(snapshot.recordedAt))
    }

    // Determine Light Band based on lux
    val (lightCategory, categoryColor) = remember(snapshot.lightLux) {
        when {
            snapshot.lightLux < 1000f -> Pair("Low Light", Sand)
            snapshot.lightLux < 2500f -> Pair("Medium Indirect", Leaf)
            snapshot.lightLux < 10000f -> Pair("Bright Indirect", Moss)
            else -> Pair("Direct Sun", AmberLight)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Sand, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Room pill badge & recorded timestamp + Delete action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Forest.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = snapshot.roomTag,
                            style = MaterialTheme.typography.labelLarge,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateTimeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Mute
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete snapshot",
                            tint = Mute,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(
                            text = "Delete snapshot?",
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                    },
                    text = {
                        Text(
                            text = "If deleted, the previous snapshot recorded for ${snapshot.roomTag} will automatically become the active snapshot.",
                            color = Ink,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                onDeleteSnapshot(snapshot.id)
                            }
                        ) {
                            Text("Delete", color = Terracotta, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel", color = Forest)
                        }
                    },
                    containerColor = SurfaceWarm,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Highlight: Light Measurement
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Paper),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(Locale.US, "%,.0f lx", snapshot.lightLux),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Forest
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(Locale.US, "(%.1f fc)", snapshot.lightFootCandles),
                                fontSize = 12.sp,
                                color = Bark
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (snapshot.lightSource == "SENSOR") Icons.Default.Sensors else Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Mute,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (snapshot.lightSource == "SENSOR") "Ambient Sensor" else "Camera Meter",
                                style = MaterialTheme.typography.labelSmall,
                                color = Mute
                            )
                        }
                    }

                    // Category Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(categoryColor.copy(alpha = 0.2f))
                            .border(1.dp, categoryColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = lightCategory,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Forest
                        )
                    }
                }
            }

            // Atmosphere Metrics (Temp, Humidity, AQI, UV)
            if (snapshot.temperatureCelsius != null || snapshot.humidityPercentage != null || snapshot.aqi != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    snapshot.temperatureCelsius?.let { temp ->
                        MiniMetricBadge(
                            icon = Icons.Default.Thermostat,
                            iconColor = Bark,
                            text = String.format(Locale.US, "%.1f°C", temp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    snapshot.humidityPercentage?.let { hum ->
                        MiniMetricBadge(
                            icon = Icons.Default.WaterDrop,
                            iconColor = SkyBlue,
                            text = String.format(Locale.US, "%.0f%%", hum),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    snapshot.aqi?.let { aqi ->
                        MiniMetricBadge(
                            icon = Icons.Default.Air,
                            iconColor = Moss,
                            text = "AQI $aqi",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    snapshot.uvIndex?.let { uv ->
                        MiniMetricBadge(
                            icon = Icons.Default.WbSunny,
                            iconColor = AmberLight,
                            text = String.format(Locale.US, "UV %.1f", uv),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Optional Observation Note
            snapshot.note?.let { noteText ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Paper.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = Bark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = noteText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniMetricBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceWarm)
            .border(1.dp, Sand.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink
            )
        }
    }
}
