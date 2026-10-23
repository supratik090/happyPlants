package com.hos.happy.plants.feature.env.components

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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SnapshotHistorySection(
    snapshots: List<RoomEnvSnapshotEntity>,
    distinctRooms: List<String>,
    recordedTimestamps: List<Long>,
    selectedRoom: String?,
    selectedDateMillis: Long?,
    onSelectRoom: (String?) -> Unit,
    onSelectDate: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCalendarDialog by remember { mutableStateOf(false) }

    val dateDisplayStr = remember(selectedDateMillis) {
        if (selectedDateMillis != null) {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(selectedDateMillis))
        } else {
            null
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header: Shows auto-filtered room name & calendar trigger
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
                        style = MaterialTheme.typography.headlineSmall,
                        color = Forest
                    )
                    Text(
                        text = "Filtered for: ${selectedRoom ?: "All Rooms"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Bark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Calendar trigger button: opens calendar with only snapshot dates enabled
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { showCalendarDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedDateMillis != null) Moss else Sand),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedDateMillis != null) Leaf.copy(alpha = 0.2f) else Color.Transparent
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendar",
                        modifier = Modifier.size(16.dp),
                        tint = if (selectedDateMillis != null) Forest else Moss
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateDisplayStr ?: "Select Date",
                        fontSize = 12.sp,
                        color = if (selectedDateMillis != null) Forest else Bark,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (selectedDateMillis != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { onSelectDate(null) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear date filter",
                            tint = Mute,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Sand,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (recordedTimestamps.isEmpty()) "No snapshots recorded yet." else "No snapshot recorded for this date and room.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mute
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                snapshots.forEach { snapshot ->
                    SnapshotCardItem(snapshot = snapshot)
                }
            }
        }
    }

    // Material 3 DatePickerDialog with only snapshot dates enabled
    if (showCalendarDialog) {
        SnapshotCalendarModal(
            recordedTimestamps = recordedTimestamps,
            initialSelectedDateMillis = selectedDateMillis,
            onDateSelected = { timestamp ->
                onSelectDate(timestamp)
                showCalendarDialog = false
            },
            onDismiss = { showCalendarDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SnapshotCalendarModal(
    recordedTimestamps: List<Long>,
    initialSelectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Build set of calendar date keys ("YYYY-M-D") for fast lookup
    val snapshotDateKeys = remember(recordedTimestamps) {
        recordedTimestamps.map { ts ->
            val cal = Calendar.getInstance().apply { timeInMillis = ts }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
        }.toSet()
    }

    // SelectableDates: ONLY dates in snapshotDateKeys are enabled, rest are disabled!
    val selectableDates = remember(snapshotDateKeys) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = utcTimeMillis
                }
                val key = "${utcCal.get(Calendar.YEAR)}-${utcCal.get(Calendar.MONTH)}-${utcCal.get(Calendar.DAY_OF_MONTH)}"
                return snapshotDateKeys.contains(key)
            }

            override fun isSelectableYear(year: Int): Boolean = true
        }
    }

    // Calculate initial date in UTC for the picker
    val initialPickerDateMillis = remember(initialSelectedDateMillis, recordedTimestamps) {
        val targetTs = initialSelectedDateMillis ?: recordedTimestamps.firstOrNull() ?: System.currentTimeMillis()
        val localCal = Calendar.getInstance().apply { timeInMillis = targetTs }
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(localCal.get(Calendar.YEAR), localCal.get(Calendar.MONTH), localCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialPickerDateMillis,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = utcMillis
                        }
                        val localCal = Calendar.getInstance().apply {
                            set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateSelected(localCal.timeInMillis)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Select", color = SurfaceWarm, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Mute)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = SurfaceWarm
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Snapshot Dates",
                    style = MaterialTheme.typography.titleMedium,
                    color = Forest,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            },
            headline = {
                Text(
                    text = "Select a date with recorded data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute,
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = SurfaceWarm,
                titleContentColor = Forest,
                headlineContentColor = Forest,
                weekdayContentColor = Bark,
                subheadContentColor = Forest,
                yearContentColor = Forest,
                currentYearContentColor = Moss,
                selectedYearContentColor = SurfaceWarm,
                selectedYearContainerColor = Moss,
                dayContentColor = Ink,
                disabledDayContentColor = Sand.copy(alpha = 0.35f),
                selectedDayContentColor = SurfaceWarm,
                selectedDayContainerColor = Moss,
                todayContentColor = Leaf,
                todayDateBorderColor = Leaf
            )
        )
    }
}

@Composable
private fun SnapshotCardItem(snapshot: RoomEnvSnapshotEntity) {
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
            // Header: Room pill badge & recorded timestamp
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

                Text(
                    text = dateTimeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Mute
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
