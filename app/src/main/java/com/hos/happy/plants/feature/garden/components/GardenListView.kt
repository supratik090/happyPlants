package com.hos.happy.plants.feature.garden.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.feature.garden.GardenViewModel
import com.hos.happy.plants.feature.garden.PlantActivityStatus
import com.hos.happy.plants.feature.garden.PlantActivityType
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
import com.hos.happy.plants.ui.theme.Terracotta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GardenListView(
    plants: List<PlantEntity>,
    availableRooms: List<String>,
    selectedRoom: String,
    expandedPlantId: String?,
    onSelectRoom: (String) -> Unit,
    onAddRoomClicked: () -> Unit,
    onToggleExpand: (String) -> Unit,
    onOpenCareDoctor: (PlantEntity) -> Unit,
    onOpenJourney: (PlantEntity) -> Unit,
    viewModel: GardenViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Room Filter Chips Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Moss, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Filter by Room",
                    style = MaterialTheme.typography.titleSmall,
                    color = Forest,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onAddRoomClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Paper),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Sand)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Moss, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Room", fontSize = 11.sp, color = Moss, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val allFilterOptions = listOf("All") + availableRooms

            allFilterOptions.forEach { room ->
                FilterChip(
                    selected = selectedRoom == room,
                    onClick = { onSelectRoom(room) },
                    label = { Text(room, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Moss,
                        selectedLabelColor = SurfaceWarm,
                        containerColor = SurfaceWarm
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedRoom == room,
                        borderColor = Sand
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (plants.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🪴", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (selectedRoom == "All") "No plants in your garden yet" else "No plants in $selectedRoom",
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ Add Plant' at the top to add your first green companion.",
                        color = Mute,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                plants.forEach { plant ->
                    val isExpanded = plant.id == expandedPlantId

                    PlantGardenCard(
                        plant = plant,
                        isExpanded = isExpanded,
                        onCardClicked = { onToggleExpand(plant.id) },
                        onOpenCareDoctor = { onOpenCareDoctor(plant) },
                        onOpenJourney = { onOpenJourney(plant) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantGardenCard(
    plant: PlantEntity,
    isExpanded: Boolean,
    onCardClicked: () -> Unit,
    onOpenCareDoctor: () -> Unit,
    onOpenJourney: () -> Unit,
    viewModel: GardenViewModel
) {
    val activityStatuses = remember(plant) {
        viewModel.calculatePlantActivities(plant)
    }

    val dueActivities = activityStatuses.filter { it.isDueNow }
    val hasWaterDue = activityStatuses.any { it.activity == PlantActivityType.WATER && it.isDueNow }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isExpanded) Moss else Sand,
                RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Main Card Summary (Always visible, clickable to toggle expansion)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCardClicked),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (plant.photoUri != null) {
                        AsyncImage(
                            model = plant.photoUri,
                            contentDescription = plant.nickname,
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Paper),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🪴", fontSize = 26.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = plant.nickname,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${plant.roomTag} • ${plant.botanicalName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Bark,
                            fontSize = 11.sp,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status Pills Row based on calculated activities
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasWaterDue) {
                                StatusPill(text = "💧 Thirsty", color = Terracotta)
                            }
                            dueActivities.filter { it.activity != PlantActivityType.WATER }.take(2).forEach { status ->
                                StatusPill(
                                    text = "${status.activity.icon} ${status.activity.title}",
                                    color = AmberLight
                                )
                            }
                            if (dueActivities.isEmpty()) {
                                StatusPill(text = "🌿 All Good", color = Moss)
                            }
                        }
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Moss,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Inline Expanded Details Section (No Modal!)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Sand.copy(alpha = 0.6f))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Care Doctor & Plant Journey
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenCareDoctor,
                            colors = ButtonDefaults.buttonColors(containerColor = Moss),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                        ) {
                            Text("🩺", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Care Doctor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SurfaceWarm)
                        }

                        Button(
                            onClick = onOpenJourney,
                            colors = ButtonDefaults.buttonColors(containerColor = Forest),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                        ) {
                            Text("🌱", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Plant Journey", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SurfaceWarm)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Soil Mix Guidance
                    plant.soilSimpleWords?.let { soilWords ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Sand.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Paper)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "🪴 Soil Guidance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = soilWords,
                                    fontSize = 11.sp,
                                    color = Ink,
                                    lineHeight = 15.sp
                                )
                                plant.soilMixRecipe?.let { recipe ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Mix: $recipe",
                                        fontSize = 10.sp,
                                        color = Bark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 7 Activities Calculated Schedule & Status
                    Text(
                        text = "⏱️ Activity Schedule & Calculated Days",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activityStatuses.forEach { status ->
                            ActivityRowItem(
                                status = status,
                                onMarkDone = {
                                    viewModel.logCareAction(
                                        plant = plant,
                                        activity = status.activity,
                                        remark = "Logged from Garden",
                                        photoUri = null
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recent Care Logs
                    val careLogs by viewModel.getCareLogsForPlantFlow(plant.id).collectAsState(initial = emptyList())
                    if (careLogs.isNotEmpty()) {
                        Text(
                            text = "Recent Care History",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        careLogs.take(4).forEach { log ->
                            val logDate = remember(log.timestamp) {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(log.timestamp))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• ${log.careType}: ${log.remark ?: "Completed"}", fontSize = 11.sp, color = Ink)
                                Text(logDate, fontSize = 10.sp, color = Mute)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRowItem(
    status: PlantActivityStatus,
    onMarkDone: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (status.isDueNow) (if (status.activity == PlantActivityType.WATER) Terracotta.copy(alpha = 0.5f) else AmberLight.copy(alpha = 0.5f)) else Sand.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (status.isDueNow) Paper else SurfaceWarm
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(status.activity.icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = status.activity.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                        Text(
                            text = "Every ${status.intervalDays} days",
                            fontSize = 10.sp,
                            color = Mute
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Calculated Days Status Badge
                    val badgeColor = when {
                        status.isOverdue -> Terracotta
                        status.isDueNow -> if (status.activity == PlantActivityType.WATER) Terracotta else AmberLight
                        else -> Moss
                    }
                    val badgeText = when {
                        status.isOverdue -> "Overdue ${status.overdueDays}d"
                        status.isDueNow -> "Due Today"
                        else -> "Due in ${status.daysRemaining}d"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onMarkDone,
                        colors = ButtonDefaults.buttonColors(containerColor = Moss),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Done", fontSize = 10.sp, color = SurfaceWarm, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Golden Rule / Advice
            Text(
                text = "💡 ${status.activity.goldenRule}",
                fontSize = 10.sp,
                color = Ink,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
