package com.hos.happy.plants.feature.garden

import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import com.hos.happy.plants.ui.theme.SurfaceWarm
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.feature.auth.AuthViewModel
import com.hos.happy.plants.feature.garden.components.AddPlantDialog
import com.hos.happy.plants.feature.garden.components.TodayActionsView
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.LeafBright
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTasksScreen(
    gardenViewModel: GardenViewModel,
    authViewModel: AuthViewModel,
    onSwitchProfile: () -> Unit,
    onNavigateToEnv: (String?) -> Unit = {},
    onNavigateToGarden: () -> Unit = {},
    onNavigateToResearch: () -> Unit = {},
    isHighAqi: Boolean = true
) {
    val uiState by gardenViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val allPlants by gardenViewModel.allPlants.collectAsState()
    val pendingDailyTasks by gardenViewModel.pendingDailyTasks.collectAsState()
    val completedDailyTasks by gardenViewModel.completedDailyTasks.collectAsState()
    val availableRooms by gardenViewModel.availableRoomTags.collectAsState()
    val showSwipeGestureHint by gardenViewModel.showSwipeGestureHint.collectAsState()

    LaunchedEffect(uiState.currentProfileId) {
        gardenViewModel.refreshDailyRules()
    }

    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val spinAngle by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        finishedListener = { isRefreshing = false },
        label = "spinAngle"
    )

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
            val profileName = authState.activeProfile?.name?.trim().orEmpty()
            val displayName = if (profileName.isNotBlank()) profileName else "Plant Parent"
            val userInitial = displayName.firstOrNull()?.uppercase() ?: "P"

            val todayDateFormatted = remember {
                SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
            }

            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = todayDateFormatted,
                            fontSize = 13.sp,
                            color = Bark,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "Hi, $displayName",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Forest,
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp
                        )
                    }
                },
                actions = {
                    // Add Plant Button (in front of refresh button)
                    Button(
                        onClick = { gardenViewModel.openAddPlantDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isGardenEmpty) LeafBright else Moss),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Plant",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Plant",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Circular Refresh Tasks Button (Engine Re-evaluation)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceWarm)
                            .border(1.dp, Sand, CircleShape)
                            .clickable {
                                isRefreshing = true
                                gardenViewModel.refreshDailyRules()
                                Toast.makeText(context, "Re-evaluating care rules...", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh & Re-evaluate Tasks",
                            tint = Forest,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(spinAngle)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Circular Profile Avatar (Tap to switch profile)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Moss)
                            .clickable(onClick = onSwitchProfile),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitial,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
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
            TodayActionsView(
                pendingTasks = pendingDailyTasks,
                completedTasks = completedDailyTasks,
                isHighAqi = isHighAqi,
                onCompleteTask = { gardenViewModel.completeDailyTask(it) },
                onSkipTask = { gardenViewModel.skipDailyTask(it) },
                onUndoTask = { gardenViewModel.undoCompleteDailyTask(it) },
                onMarkAllDoneForSpace = { type, space ->
                    gardenViewModel.markAllDoneForSpace(type, space)
                },
                onOpenSnapshotForSpace = { space ->
                    onNavigateToEnv(space)
                },
                onAddGardenPhoto = {
                    val firstPlant = gardenViewModel.allPlants.value.firstOrNull()
                    if (firstPlant != null) {
                        gardenViewModel.openPlantDetail(firstPlant.id)
                    }
                    onNavigateToGarden()
                },
                onNavigateToResearch = onNavigateToResearch,
                onNavigateToGarden = onNavigateToGarden,
                onRefreshTasks = { gardenViewModel.refreshDailyRules() },
                onAddPlant = { gardenViewModel.openAddPlantDialog() },
                isGardenEmpty = isGardenEmpty,
                showSwipeGestureHint = showSwipeGestureHint
            )
        }

        // Add Plant Dialog
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
    }
}
