package com.hos.happy.plants.feature.garden

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.feature.auth.AuthViewModel
import com.hos.happy.plants.feature.garden.components.AddPlantDialog
import com.hos.happy.plants.feature.garden.components.CareActionDialog
import com.hos.happy.plants.feature.garden.components.GardenListView
import com.hos.happy.plants.feature.garden.components.PlantCareDoctorView
import com.hos.happy.plants.feature.garden.components.PlantJourneySlideshow
import com.hos.happy.plants.feature.garden.components.TodayActionsView
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    gardenViewModel: GardenViewModel,
    authViewModel: AuthViewModel,
    onSwitchProfile: () -> Unit
) {
    val uiState by gardenViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val allPlants by gardenViewModel.allPlants.collectAsState()
    val todayTasks by gardenViewModel.todayTasks.collectAsState()
    val filteredGardenPlants by gardenViewModel.filteredGardenPlants.collectAsState()
    val availableRooms by gardenViewModel.availableRoomTags.collectAsState()

    var showAddRoomDialog by remember { mutableStateOf(false) }

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
                        colors = ButtonDefaults.buttonColors(containerColor = Moss),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Plant", color = SurfaceWarm, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { gardenViewModel.openAddPlantDialog() },
                containerColor = Moss,
                contentColor = SurfaceWarm,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Plant", fontWeight = FontWeight.Bold) }
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
            // Segmented Tab Switcher (Today's Action vs Garden)
            SegmentedGardenTabBar(
                selectedTab = uiState.activeTab,
                todayCount = todayTasks.size,
                gardenCount = allPlants.size,
                onTabSelected = { gardenViewModel.selectTab(it) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            when (uiState.activeTab) {
                GardenTab.TODAY -> {
                    TodayActionsView(
                        tasks = todayTasks,
                        onActionClicked = { gardenViewModel.openActionDialog(it) }
                    )
                }

                GardenTab.GARDEN -> {
                    GardenListView(
                        plants = filteredGardenPlants,
                        availableRooms = availableRooms,
                        selectedRoom = uiState.selectedRoomFilter,
                        expandedPlantId = uiState.expandedPlantId,
                        onSelectRoom = { gardenViewModel.selectRoomFilter(it) },
                        onAddRoomClicked = { gardenViewModel.openAddPlantDialog() },
                        onToggleExpand = { gardenViewModel.togglePlantExpanded(it) },
                        onOpenCareDoctor = { gardenViewModel.openCareDoctor(it) },
                        onOpenJourney = { gardenViewModel.openPlantJourney(it) },
                        viewModel = gardenViewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp)) // Padding for FAB
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
                        careType = task.careType,
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
}

@Composable
private fun SegmentedGardenTabBar(
    selectedTab: GardenTab,
    todayCount: Int,
    gardenCount: Int,
    onTabSelected: (GardenTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceWarm)
            .border(1.dp, Sand, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tab 1: Today's Action
            val isTodaySelected = selectedTab == GardenTab.TODAY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isTodaySelected) Moss else SurfaceWarm)
                    .clickable { onTabSelected(GardenTab.TODAY) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📋 Today's Action",
                        fontSize = 13.sp,
                        fontWeight = if (isTodaySelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTodaySelected) SurfaceWarm else Bark
                    )
                    if (todayCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isTodaySelected) SurfaceWarm else Leaf)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = todayCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTodaySelected) Forest else SurfaceWarm
                            )
                        }
                    }
                }
            }

            // Tab 2: Garden
            val isGardenSelected = selectedTab == GardenTab.GARDEN
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isGardenSelected) Moss else SurfaceWarm)
                    .clickable { onTabSelected(GardenTab.GARDEN) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🪴 Garden",
                        fontSize = 13.sp,
                        fontWeight = if (isGardenSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isGardenSelected) SurfaceWarm else Bark
                    )
                    if (gardenCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isGardenSelected) SurfaceWarm else Sand)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = gardenCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGardenSelected) Forest else Bark
                            )
                        }
                    }
                }
            }
        }
    }
}
