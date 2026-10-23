package com.hos.happy.plants.feature.env

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hos.happy.plants.data.preferences.LightMeterMode
import com.hos.happy.plants.feature.auth.AuthViewModel
import com.hos.happy.plants.feature.env.components.AdvancedRoomAnalyticsCard
import com.hos.happy.plants.feature.env.components.CameraMeterPreview
import com.hos.happy.plants.feature.env.components.LightMeterGauge
import com.hos.happy.plants.feature.env.components.LiveWeatherCards
import com.hos.happy.plants.feature.env.components.MetricInfoModal
import com.hos.happy.plants.feature.env.components.MetricType
import com.hos.happy.plants.feature.env.components.SnapshotHistorySection
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EnvScreen(
    envViewModel: EnvViewModel,
    authViewModel: AuthViewModel,
    onSwitchProfile: () -> Unit
) {
    val context = LocalContext.current
    val uiState by envViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val roomTags by envViewModel.availableRoomTags.collectAsState()
    val distinctRooms by envViewModel.distinctRecordedRooms.collectAsState()
    val recordedTimestamps by envViewModel.recordedSnapshotTimestamps.collectAsState()
    val roomAnalytics by envViewModel.roomAnalytics.collectAsState()
    val filteredSnapshots by envViewModel.filteredSnapshots.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedMetricInfo by remember { mutableStateOf<MetricType?>(null) }
    var noteInput by remember { mutableStateOf("") }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var newRoomInput by remember { mutableStateOf("") }

    // Location permission handling for Live Weather
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasLocationPermission = granted
        envViewModel.refreshLiveEnvironment(hasLocationPermission = granted)
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            envViewModel.refreshLiveEnvironment(hasLocationPermission = true)
        }
    }

    LaunchedEffect(uiState.snapshotSavedSuccess) {
        if (uiState.snapshotSavedSuccess) {
            snackbarHostState.showSnackbar("Room snapshot saved successfully!")
            noteInput = ""
            envViewModel.resetSnapshotSavedSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Environment",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Forest
                        )
                        authState.activeProfile?.let { profile ->
                            Text(
                                text = "Profile: ${profile.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Bark
                            )
                        }
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = onSwitchProfile,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Sand),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profiles",
                            tint = Moss,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch", color = Moss, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperDark)
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Forest,
                    contentColor = SurfaceWarm,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = PaperDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Master Section: Active Room Selector (Auto-filters entire screen)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Moss, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Room", style = MaterialTheme.typography.titleMedium, color = Forest, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { showAddRoomDialog = true },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Moss, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Room", color = Moss, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        roomTags.forEach { tag ->
                            FilterChip(
                                selected = uiState.selectedRoomTag == tag,
                                onClick = { envViewModel.selectRoomTag(tag) },
                                label = { Text(tag, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Moss,
                                    selectedLabelColor = SurfaceWarm,
                                    containerColor = SurfaceWarm
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = uiState.selectedRoomTag == tag,
                                    borderColor = Sand
                                )
                            )
                        }

                        // Inline quick add chip
                        FilterChip(
                            selected = false,
                            onClick = { showAddRoomDialog = true },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Moss, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 12.sp, color = Moss, fontWeight = FontWeight.Medium)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Paper
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = Sand
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Live Macro-climate (Temperature, Humidity, AQI, UV, Daylight)
            LiveWeatherCards(
                snapshot = uiState.weatherSnapshot,
                locationName = uiState.locationName,
                isLoading = uiState.isWeatherLoading,
                onRefresh = {
                    envViewModel.refreshLiveEnvironment(hasLocationPermission)
                },
                onShowMetricInfo = { selectedMetricInfo = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Light Meter Gauge (Sensor + Camera Fallback)
            Text(
                text = "Light Meter",
                style = MaterialTheme.typography.headlineSmall,
                color = Forest
            )
            Spacer(modifier = Modifier.height(8.dp))

            LightMeterGauge(
                settings = uiState.lightSettings,
                currentReading = uiState.liveLightReading,
                isFrozen = uiState.isLightFrozen,
                onToggleMode = { envViewModel.toggleLightMeterMode() },
                onToggleFreeze = { envViewModel.toggleLightFreeze() },
                onShowMetricInfo = { selectedMetricInfo = it }
            )

            // If Camera mode is active, render CameraX preview analyzer
            if (uiState.lightSettings.preferredMode == LightMeterMode.CAMERA) {
                Spacer(modifier = Modifier.height(14.dp))
                CameraMeterPreview(
                    onLightCalculated = { reading ->
                        envViewModel.onCameraLightCalculated(reading)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Advanced Room Analytics (VPD & DLI for selected room)
            AdvancedRoomAnalyticsCard(
                analytics = roomAnalytics,
                onShowMetricInfo = { selectedMetricInfo = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Capture Snapshot for Selected Room
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Take Snapshot — ${uiState.selectedRoomTag}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Records live light, temperature, humidity and AQI for this room.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mute,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Optional note (e.g. sunny morning, sheer blinds)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Moss,
                            focusedLabelColor = Moss
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { envViewModel.saveSnapshot(noteInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = Moss),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = SurfaceWarm)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Snapshot for ${uiState.selectedRoomTag}",
                            color = SurfaceWarm,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 5: Historical Snapshots Lookup (Auto-filtered for selected room)
            SnapshotHistorySection(
                snapshots = filteredSnapshots,
                distinctRooms = distinctRooms,
                recordedTimestamps = recordedTimestamps,
                selectedRoom = uiState.historyFilterRoom,
                selectedDateMillis = uiState.historyFilterDateMillis,
                onSelectRoom = { envViewModel.setHistoryFilterRoom(it) },
                onSelectDate = { envViewModel.setHistoryFilterDate(it) }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Metric Info Help Modal (auto-closes in 5 seconds or on dismiss)
        MetricInfoModal(
            metricType = selectedMetricInfo,
            onDismiss = { selectedMetricInfo = null }
        )

        // Add New Room Dialog
        if (showAddRoomDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddRoomDialog = false
                    newRoomInput = ""
                },
                title = {
                    Text(
                        text = "Add New Room",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Name your room or growing spot (e.g. Study, Patio, Kitchen, Office).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mute,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newRoomInput,
                            onValueChange = { newRoomInput = it },
                            placeholder = { Text("Room name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Moss,
                                focusedLabelColor = Moss,
                                cursorColor = Moss
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = newRoomInput.trim()
                            if (trimmed.isNotBlank()) {
                                envViewModel.addCustomRoomTag(trimmed)
                                newRoomInput = ""
                                showAddRoomDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Moss),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add Room", color = SurfaceWarm, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddRoomDialog = false
                        newRoomInput = ""
                    }) {
                        Text("Cancel", color = Mute)
                    }
                },
                containerColor = SurfaceWarm,
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}
