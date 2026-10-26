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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.feature.garden.GardenViewModel
import com.hos.happy.plants.feature.garden.PlantGardenTag
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.CameraAlt
import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import com.hos.happy.plants.feature.env.components.MetricLabel
import com.hos.happy.plants.feature.env.components.SnapshotLabelEvaluator
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.DustyTagBg
import com.hos.happy.plants.ui.theme.DustyTagText
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.HappyTagBg
import com.hos.happy.plants.ui.theme.HappyTagText
import com.hos.happy.plants.ui.theme.HungryTagBg
import com.hos.happy.plants.ui.theme.HungryTagText
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.ThirstyTagBg
import com.hos.happy.plants.ui.theme.ThirstyTagText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GardenListView(
    plants: List<PlantEntity>,
    allPlants: List<PlantEntity> = plants,
    availableRooms: List<String>,
    selectedRoom: String,
    latestSnapshots: Map<String, RoomEnvSnapshotEntity> = emptyMap(),
    onSelectRoom: (String) -> Unit,
    onAddSpaceClicked: () -> Unit,
    onPlantSelected: (PlantEntity) -> Unit,
    onNavigateToEnv: (String) -> Unit = {},
    viewModel: GardenViewModel,
    modifier: Modifier = Modifier
) {
    val isAllSelected = selectedRoom.equals("All", ignoreCase = true)
    var collapsedRooms by remember { mutableStateOf(emptySet<String>()) }

    val roomsWithPlants = remember(allPlants) {
        allPlants.map { it.roomTag }.filter { it.isNotBlank() }.distinct()
    }

    LaunchedEffect(roomsWithPlants, selectedRoom) {
        if (!isAllSelected && selectedRoom !in roomsWithPlants) {
            onSelectRoom("All")
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Growing Space Header Row with "+ Space" Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Forest,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Growing Space",
                    style = MaterialTheme.typography.titleMedium,
                    color = Forest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Button(
                onClick = onAddSpaceClicked,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceWarm),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Sand)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Moss,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Space",
                    fontSize = 12.sp,
                    color = Forest,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Room Filter Chips Bar ("All", plus spaces that have plants)
        val allFilterOptions = remember(roomsWithPlants) {
            listOf("All") + roomsWithPlants
        }

        if (allFilterOptions.size > 1) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                allFilterOptions.forEach { room ->
                    FilterChip(
                        selected = selectedRoom.equals(room, ignoreCase = true),
                        onClick = { onSelectRoom(room) },
                        label = {
                            Text(
                                text = room,
                                fontSize = 12.sp,
                                fontWeight = if (selectedRoom.equals(room, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Forest,
                            selectedLabelColor = SurfaceWarm,
                            containerColor = SurfaceWarm,
                            labelColor = Forest
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedRoom.equals(room, ignoreCase = true),
                            borderColor = Sand
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 4. Plant Groups by Room / Space (only spaces with plants)
        val roomsToShow = remember(roomsWithPlants, selectedRoom, isAllSelected) {
            if (isAllSelected) {
                roomsWithPlants
            } else {
                roomsWithPlants.filter { it.equals(selectedRoom, ignoreCase = true) }
            }
        }

        if (roomsToShow.isEmpty()) {
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
                    Text("🪴", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isAllSelected) "No plants in your garden yet" else "No plants in $selectedRoom",
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ Add Plant' at the top to add your green companion.",
                        color = Mute,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                roomsToShow.forEach { roomName ->
                    val plantsInRoom = remember(plants, roomName) {
                        plants.filter { it.roomTag.equals(roomName, ignoreCase = true) }
                    }
                    val isCollapsed = collapsedRooms.contains(roomName)
                    val lastSnapshot = latestSnapshots[roomName]

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Space Header Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                            border = BorderStroke(1.dp, Sand)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Top Row: Space name + plant count + Expand/Collapse
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            collapsedRooms = if (isCollapsed) collapsedRooms - roomName else collapsedRooms + roomName
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = Moss,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = roomName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Forest,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${plantsInRoom.size} ${if (plantsInRoom.size == 1) "plant" else "plants"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Bark,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                        contentDescription = if (isCollapsed) "Expand" else "Collapse",
                                        tint = Forest,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Snapshot status row: Show labels of last snapshot or button to take snapshot
                                if (lastSnapshot != null) {
                                    val lightLabel = remember(lastSnapshot.lightLux) {
                                        SnapshotLabelEvaluator.evaluateLight(lastSnapshot.lightLux)
                                    }
                                    val tempLabel = remember(lastSnapshot.temperatureCelsius) {
                                        SnapshotLabelEvaluator.evaluateTemperature(lastSnapshot.temperatureCelsius)
                                    }
                                    val humLabel = remember(lastSnapshot.humidityPercentage) {
                                        SnapshotLabelEvaluator.evaluateHumidity(lastSnapshot.humidityPercentage)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onNavigateToEnv(roomName) },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            MetricPillSmall(label = lightLabel)
                                            MetricPillSmall(label = tempLabel)
                                            MetricPillSmall(label = humLabel)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "Env",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Moss
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = "View Live Environment",
                                                tint = Moss,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "No snapshot available",
                                            fontSize = 12.sp,
                                            color = Mute,
                                            fontStyle = FontStyle.Italic
                                        )

                                        Button(
                                            onClick = { onNavigateToEnv(roomName) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Moss),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                tint = SurfaceWarm,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Take Snapshot",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SurfaceWarm
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Plants in this Room
                        AnimatedVisibility(
                            visible = !isCollapsed,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (plantsInRoom.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    plantsInRoom.forEach { plant ->
                                        PlantGardenCardItem(
                                            plant = plant,
                                            viewModel = viewModel,
                                            onClick = { onPlantSelected(plant) }
                                        )
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .border(1.dp, Sand.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = PaperDark.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "No plants in $roomName yet.",
                                            fontSize = 12.sp,
                                            color = Mute
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

@Composable
private fun PlantGardenCardItem(
    plant: PlantEntity,
    viewModel: GardenViewModel,
    onClick: () -> Unit
) {
    val tags = remember(plant) { viewModel.getTagsForPlant(plant) }

    val resolvedPhoto = plant.photoUri?.takeIf { it.startsWith("file://") || it.startsWith("content://") }
        ?: plant.masterPlantId?.let { viewModel.masterRepo.getPlantById(it)?.displayImageUrl }
        ?: plant.photoUri

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Sand, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!resolvedPhoto.isNullOrBlank()) {
                    AsyncImage(
                        model = resolvedPhoto,
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
                        Text("🪴", fontSize = 28.sp)
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plant.botanicalName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Bark,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status Tags Row (e.g. Happy, Thirsty, Dusty, Hungry)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tags.forEach { tag ->
                            PlantStatusTagChipSmall(tag = tag)
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = Forest,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun PlantStatusTagChipSmall(tag: PlantGardenTag) {
    val bg = when (tag) {
        PlantGardenTag.HAPPY -> HappyTagBg
        PlantGardenTag.THIRSTY -> ThirstyTagBg
        PlantGardenTag.DUSTY -> DustyTagBg
        PlantGardenTag.HUNGRY -> HungryTagBg
    }
    val textColor = when (tag) {
        PlantGardenTag.HAPPY -> HappyTagText
        PlantGardenTag.THIRSTY -> ThirstyTagText
        PlantGardenTag.DUSTY -> DustyTagText
        PlantGardenTag.HUNGRY -> HungryTagText
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${tag.icon} ${tag.label}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun MetricPillSmall(label: MetricLabel) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(label.backgroundColor)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = label.text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = label.textColor
        )
    }
}

