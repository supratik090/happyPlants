package com.hos.happy.plants.feature.env.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SnapshotHistorySection(
    snapshots: List<RoomEnvSnapshotEntity>,
    distinctRooms: List<String>,
    selectedRoom: String?,
    selectedDateMillis: Long?,
    onSelectRoom: (String?) -> Unit,
    onSelectDate: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val dateDisplayStr = remember(selectedDateMillis) {
        if (selectedDateMillis != null) {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(selectedDateMillis))
        } else {
            "All Dates"
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
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
                Text(
                    text = "Room Snapshots History",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Forest
                )
            }

            // Date Picker trigger
            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    if (selectedDateMillis != null) {
                        cal.timeInMillis = selectedDateMillis
                    }
                    val datePicker = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val chosen = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth, 0, 0, 0)
                            }
                            onSelectDate(chosen.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                },
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Sand),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Bark
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = dateDisplayStr,
                    fontSize = 12.sp,
                    color = Bark,
                    fontWeight = FontWeight.Medium
                )
                if (selectedDateMillis != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { onSelectDate(null) },
                        modifier = Modifier.size(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear date",
                            tint = Mute,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Room filter chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedRoom == null,
                onClick = { onSelectRoom(null) },
                label = { Text("All Rooms", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Moss,
                    selectedLabelColor = SurfaceWarm
                )
            )

            distinctRooms.forEach { room ->
                FilterChip(
                    selected = selectedRoom == room,
                    onClick = { onSelectRoom(if (selectedRoom == room) null else room) },
                    label = { Text(room, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Moss,
                        selectedLabelColor = SurfaceWarm
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Snapshot cards or Empty State
        if (snapshots.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No snapshot recorded for this date and room.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mute
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                snapshots.forEach { snapshot ->
                    SnapshotCardItem(snapshot = snapshot)
                }
            }
        }
    }
}

@Composable
private fun SnapshotCardItem(snapshot: RoomEnvSnapshotEntity) {
    val dateTimeStr = remember(snapshot.recordedAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(snapshot.recordedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Sand, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Room tag & recorded date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Moss,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = snapshot.roomTag,
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = dateTimeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Snapshot metric readings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Light Level",
                        style = MaterialTheme.typography.labelSmall,
                        color = Mute
                    )
                    Text(
                        text = String.format(Locale.US, "%,.0f lx (%.1f fc)", snapshot.lightLux, snapshot.lightFootCandles),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                    Text(
                        text = "Source: ${snapshot.lightSource}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Sand
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    snapshot.temperatureCelsius?.let { temp ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Temp", style = MaterialTheme.typography.labelSmall, color = Mute)
                            Text(
                                text = String.format(Locale.US, "%.1f°C", temp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Bark
                            )
                        }
                    }

                    snapshot.humidityPercentage?.let { hum ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Humidity", style = MaterialTheme.typography.labelSmall, color = Mute)
                            Text(
                                text = String.format(Locale.US, "%.0f%%", hum),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )
                        }
                    }

                    snapshot.aqi?.let { aqi ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "AQI", style = MaterialTheme.typography.labelSmall, color = Mute)
                            Text(
                                text = "$aqi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    }
                }
            }

            snapshot.note?.let { noteText ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Paper)
                        .padding(8.dp)
                ) {
                    Text(
                        text = noteText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink
                    )
                }
            }
        }
    }
}
