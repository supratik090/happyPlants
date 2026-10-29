package com.hos.happy.plants.feature.garden.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.data.model.WateringSchedule
import com.hos.happy.plants.feature.garden.GardenViewModel
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.Terracotta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScheduleOverviewSheet(
    plants: List<PlantEntity>,
    viewModel: GardenViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var editingPlant by remember { mutableStateOf<PlantEntity?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWarm,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🗓️ Garden Watering Schedules",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                    Text(
                        text = "${plants.size} plants · tap any plant to adjust watering days",
                        fontSize = 12.sp,
                        color = Mute
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Forest)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (plants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No plants in garden yet.", color = Mute, fontSize = 14.sp)
                }
            } else {
                val groupedByRoom = remember(plants) {
                    plants.groupBy { it.roomTag.ifBlank { "Other" } }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedByRoom.forEach { (roomName, roomPlants) ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🏠 $roomName (${roomPlants.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Mute
                            )

                            roomPlants.forEach { plant ->
                                val (scheduleLabel, nextDueStr) = remember(plant) {
                                    viewModel.getScheduleInfoForPlant(plant)
                                }
                                val hasCustom = plant.waterScheduleType != null

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, Sand, RoundedCornerShape(14.dp))
                                        .clickable { editingPlant = plant },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val photo = plant.photoUri?.takeIf { it.startsWith("file://") || it.startsWith("content://") }
                                                ?: plant.masterPlantId?.let { viewModel.masterRepo.getPlantById(it)?.displayImageUrl }
                                                ?: plant.photoUri

                                            if (!photo.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = photo,
                                                    contentDescription = plant.nickname,
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(RoundedCornerShape(10.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Paper),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("🪴", fontSize = 22.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = plant.nickname,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Forest
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "💧 $scheduleLabel",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (hasCustom) Moss else Mute
                                                )
                                                Text(
                                                    text = nextDueStr,
                                                    fontSize = 11.sp,
                                                    color = if (nextDueStr.contains("today", ignoreCase = true)) Terracotta else Mute
                                                )
                                            }
                                        }

                                        // Edit Button
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (hasCustom) Leaf.copy(alpha = 0.2f) else Paper)
                                                .clickable { editingPlant = plant }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit Schedule",
                                                    tint = Moss,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (hasCustom) "Custom" else "Default",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Moss
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct schedule editing sheet from garden view
    editingPlant?.let { plant ->
        val masterPlant = plant.masterPlantId?.let { viewModel.masterRepo.getPlantById(it) }
        val masterDays = masterPlant?.watering?.hotDryMonths?.max ?: masterPlant?.watering?.monsoonAndWinter?.max ?: plant.wateringIntervalDays
        WateringScheduleBottomSheet(
            plant = plant,
            masterIntervalDays = masterDays,
            onDismiss = { editingPlant = null },
            onSave = { newSchedule ->
                viewModel.saveWaterSchedule(plant, newSchedule)
                editingPlant = null
            }
        )
    }
}
