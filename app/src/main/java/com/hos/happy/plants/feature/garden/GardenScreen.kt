package com.hos.happy.plants.feature.garden

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.feature.auth.AuthViewModel
import com.hos.happy.plants.feature.garden.components.AddPlantDialog
import com.hos.happy.plants.feature.garden.components.CareActionDialog
import com.hos.happy.plants.feature.garden.components.GardenListView
import com.hos.happy.plants.feature.garden.components.PlantCareDoctorView
import com.hos.happy.plants.feature.garden.components.PlantDetailScreen
import com.hos.happy.plants.feature.garden.components.PlantJourneySlideshow
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.LeafBright
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    gardenViewModel: GardenViewModel,
    authViewModel: AuthViewModel,
    onSwitchProfile: () -> Unit,
    onNavigateToEnv: (String?) -> Unit = {}
) {
    val uiState by gardenViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val allPlants by gardenViewModel.allPlants.collectAsState()
    val filteredGardenPlants by gardenViewModel.filteredGardenPlants.collectAsState()
    val availableRooms by gardenViewModel.availableRoomTags.collectAsState()
    val latestSnapshots by gardenViewModel.latestSnapshotByRoom.collectAsState()
    val selectedPlantDetail by gardenViewModel.selectedPlantDetail.collectAsState()

    // Handle back gesture to exit plant detail screen
    BackHandler(enabled = selectedPlantDetail != null) {
        gardenViewModel.closePlantDetail()
    }

    if (selectedPlantDetail != null) {
        PlantDetailScreen(
            plant = selectedPlantDetail!!,
            viewModel = gardenViewModel,
            onBack = { gardenViewModel.closePlantDetail() },
            onOpenDoctor = { gardenViewModel.openCareDoctor(selectedPlantDetail!!) }
        )
    } else {
        val isGardenEmpty = allPlants.isEmpty()
        val infiniteTransition = rememberInfiniteTransition(label = "flashAddPlant")

        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        val pulseColor by infiniteTransition.animateColor(
            initialValue = Moss,
            targetValue = LeafBright,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseColor"
        )

        val glowBorderColor by infiniteTransition.animateColor(
            initialValue = Color.Transparent,
            targetValue = Leaf,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowBorderColor"
        )

        val buttonScale = if (isGardenEmpty) pulseScale else 1f
        val buttonColor = if (isGardenEmpty) pulseColor else Moss
        val buttonBorder = if (isGardenEmpty) BorderStroke(2.dp, glowBorderColor) else null

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Garden",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Forest,
                                fontWeight = FontWeight.Bold
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
                        // Prominent Header Button: + Add Plant
                        Button(
                            onClick = { gardenViewModel.openAddPlantDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            shape = RoundedCornerShape(10.dp),
                            border = buttonBorder,
                            modifier = Modifier.scale(buttonScale),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGardenEmpty) "✨ Add Plant" else "Add Plant",
                                color = SurfaceWarm,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

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
            containerColor = PaperDark
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                GardenListView(
                    plants = filteredGardenPlants,
                    allPlants = allPlants,
                    availableRooms = availableRooms,
                    selectedRoom = uiState.selectedRoomFilter,
                    latestSnapshots = latestSnapshots,
                    onSelectRoom = { gardenViewModel.selectRoomFilter(it) },
                    onAddSpaceClicked = { gardenViewModel.openAddSpaceDialog() },
                    onPlantSelected = { plant ->
                        gardenViewModel.openPlantDetail(plant.id)
                    },
                    onNavigateToEnv = onNavigateToEnv,
                    viewModel = gardenViewModel
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add Space Dialog
    if (uiState.showAddSpaceDialog) {
        var spaceName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { gardenViewModel.closeAddSpaceDialog() },
            title = {
                Text(
                    text = "Add Growing Space",
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )
            },
            text = {
                Column {
                    Text(
                        text = "Name your space (e.g. kitchen, Patio, Dining, Study):",
                        fontSize = 12.sp,
                        color = Bark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = spaceName,
                        onValueChange = { spaceName = it },
                        placeholder = { Text("Space name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Moss,
                            unfocusedBorderColor = Sand,
                            focusedLabelColor = Moss
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (spaceName.isNotBlank()) {
                            gardenViewModel.addCustomRoom(spaceName.trim())
                        }
                        gardenViewModel.closeAddSpaceDialog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Moss)
                ) {
                    Text("Add", color = SurfaceWarm, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { gardenViewModel.closeAddSpaceDialog() },
                    border = BorderStroke(1.dp, Sand)
                ) {
                    Text("Cancel", color = Forest)
                }
            },
            containerColor = SurfaceWarm,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add Plant Dialog (with Typeahead search)
    if (uiState.showAddPlantDialog) {
        AddPlantDialog(
            masterRepo = gardenViewModel.masterRepo,
            availableRooms = availableRooms,
            onAddRoom = { gardenViewModel.addCustomRoom(it) },
            onDismiss = { gardenViewModel.closeAddPlantDialog() },
            onConfirm = { nickname, commonName, botanicalName, roomTag, masterPlant, customPhotoUri, waterDays, fertDays, soilDays, repotDays, notes ->
                gardenViewModel.createPlant(
                    nickname = nickname,
                    commonName = commonName,
                    botanicalName = botanicalName,
                    roomTag = roomTag,
                    masterPlant = masterPlant,
                    customPhotoUri = customPhotoUri,
                    wateringDays = waterDays,
                    fertilizeDays = fertDays,
                    refreshSoilDays = soilDays,
                    repotDays = repotDays,
                    notes = notes
                )
            }
        )
    }

    // Today Action Dialog (Remark & Photo)
    uiState.actionDialogTask?.let { task ->
        CareActionDialog(
            task = task,
            onDismiss = { gardenViewModel.closeActionDialog() },
            onConfirm = { remark, photoUri ->
                gardenViewModel.logCareAction(
                    plant = task.plant,
                    activity = task.activity,
                    remark = remark,
                    photoUri = photoUri
                )
            }
        )
    }

    // Care Doctor Dialog (Common Problems Diagnostic)
    uiState.activeDoctorPlant?.let { plant ->
        val problems = remember(plant.id) {
            gardenViewModel.getCommonProblemsForPlant(plant)
        }
        PlantCareDoctorView(
            plant = plant,
            problems = problems,
            onDismiss = { gardenViewModel.closeCareDoctor() }
        )
    }

    // Plant Journey Animated Slideshow
    uiState.activeJourneyPlant?.let { plant ->
        PlantJourneySlideshow(
            plant = plant,
            photosFlow = gardenViewModel.getPhotosForPlantFlow(plant.id),
            onDismiss = { gardenViewModel.closePlantJourney() }
        )
    }
}
