package com.hos.happy.plants.feature.garden.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.data.repository.PlantMasterRepository
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddPlantDialog(
    masterRepo: PlantMasterRepository,
    availableRooms: List<String>,
    onAddRoom: (String) -> Unit,
    onDismiss: () -> Unit,
    initialMasterPlant: MasterPlant? = null,
    initialCommonName: String? = null,
    initialBotanicalName: String? = null,
    initialPhotoUri: String? = null,
    onConfirm: (
        nickname: String,
        commonName: String,
        botanicalName: String,
        roomTag: String,
        masterPlant: MasterPlant?,
        customPhotoUri: String?,
        wateringDays: Int,
        fertilizeDays: Int,
        refreshSoilDays: Int,
        repotDays: Int,
        notes: String?
    ) -> Unit
) {
    var selectedMasterPlant by remember { mutableStateOf<MasterPlant?>(initialMasterPlant) }
    var searchQuery by remember { mutableStateOf(initialMasterPlant?.commonName ?: initialCommonName ?: "") }
    var nicknameInput by remember { mutableStateOf(initialMasterPlant?.commonName ?: initialCommonName ?: "") }
    var botanicalNameInput by remember { mutableStateOf(initialMasterPlant?.botanicalName ?: initialBotanicalName ?: "") }
    var customPhotoUri by remember { mutableStateOf<String?>(initialPhotoUri) }
    var selectedRoom by remember { mutableStateOf(availableRooms.firstOrNull() ?: "Living") }
    var notesInput by remember { mutableStateOf("") }

    // Intervals
    var wateringDays by remember { mutableIntStateOf(initialMasterPlant?.watering?.hotDryMonths?.min ?: 7) }
    var fertilizeDays by remember { mutableIntStateOf(initialMasterPlant?.fertilizeEveryDays ?: 60) }
    var refreshSoilDays by remember { mutableIntStateOf(initialMasterPlant?.refreshTopSoilEveryDays ?: 180) }
    var repotDays by remember { mutableIntStateOf(initialMasterPlant?.repotEveryDays ?: 730) }

    // Add room dialog state
    var showInlineAddRoom by remember { mutableStateOf(false) }
    var newRoomName by remember { mutableStateOf("") }

    val searchResults = remember(searchQuery) {
        if (searchQuery.isNotBlank() && selectedMasterPlant == null) {
            masterRepo.searchPlants(searchQuery)
        } else {
            emptyList()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .border(1.dp, Sand, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Leaf.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Spa, contentDescription = null, tint = Forest, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add New Plant",
                            style = MaterialTheme.typography.titleLarge,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Mute)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Typeahead Search Field
                Text(
                    text = "Plant Species (Search Library)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Bark,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (selectedMasterPlant != null && it != selectedMasterPlant?.commonName) {
                            selectedMasterPlant = null
                        }
                    },
                    placeholder = { Text("e.g. Snake Plant, Pothos, Monstera...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Moss) },
                    trailingIcon = {
                        if (selectedMasterPlant != null) {
                            Icon(Icons.Default.Check, contentDescription = "Matched", tint = Moss)
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Moss,
                        focusedLabelColor = Moss,
                        cursorColor = Moss
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown suggestions
                if (searchResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .border(1.dp, Sand, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Paper)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            searchResults.take(5).forEach { plant ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedMasterPlant = plant
                                            searchQuery = plant.commonName
                                            if (nicknameInput.isBlank()) {
                                                nicknameInput = plant.commonName
                                            }
                                            botanicalNameInput = plant.botanicalName

                                            // Auto-fill intervals from master plant
                                            plant.watering?.hotDryMonths?.min?.let { wateringDays = it }
                                            plant.fertilizeEveryDays?.let { fertilizeDays = it }
                                            plant.refreshTopSoilEveryDays?.let { refreshSoilDays = it }
                                            plant.repotEveryDays?.let { repotDays = it }
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val imgUrl = plant.displayImageUrl ?: plant.image?.thumbnailUrl ?: plant.image?.directImageUrl
                                    if (imgUrl != null) {
                                        AsyncImage(
                                            model = imgUrl,
                                            contentDescription = plant.commonName,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Sand.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(plant.commonName.take(1), fontWeight = FontWeight.Bold, color = Forest)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(plant.commonName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Ink)
                                        Text(plant.botanicalName, fontSize = 11.sp, color = Mute)
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected Plant Preview Banner
                selectedMasterPlant?.let { plant ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Leaf.copy(alpha = 0.15f))
                            .border(1.dp, Leaf.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val imgUrl = plant.displayImageUrl ?: plant.image?.thumbnailUrl ?: plant.image?.directImageUrl
                            if (imgUrl != null) {
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = plant.commonName,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Column {
                                Text(
                                    text = "Auto-filled from Library",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest
                                )
                                Text(
                                    text = "Water every ~${wateringDays}d • Feeding ~${fertilizeDays}d",
                                    fontSize = 11.sp,
                                    color = Bark
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nickname Field
                Text(
                    text = "Plant Nickname",
                    style = MaterialTheme.typography.labelMedium,
                    color = Bark,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    placeholder = { Text("e.g. Living Room Greenie") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Moss,
                        cursorColor = Moss
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Room Selector Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Growing Space",
                        style = MaterialTheme.typography.labelMedium,
                        color = Bark,
                        fontWeight = FontWeight.SemiBold
                    )

                    TextButton(
                        onClick = { showInlineAddRoom = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Moss, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Add Space", color = Moss, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableRooms.forEach { room ->
                        FilterChip(
                            selected = selectedRoom == room,
                            onClick = { selectedRoom = room },
                            label = { Text(room, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Moss,
                                selectedLabelColor = SurfaceWarm,
                                containerColor = Paper
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedRoom == room,
                                borderColor = Sand
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Care Intervals Customization
                Text(
                    text = "Care Schedule (Editable)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Bark,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Watering Days
                    OutlinedTextField(
                        value = wateringDays.toString(),
                        onValueChange = { wateringDays = it.toIntOrNull() ?: wateringDays },
                        label = { Text("Water (days)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Moss)
                    )

                    // Feeding Days
                    OutlinedTextField(
                        value = fertilizeDays.toString(),
                        onValueChange = { fertilizeDays = it.toIntOrNull() ?: fertilizeDays },
                        label = { Text("Fertilize (d)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Moss)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Soil Refresh Days
                    OutlinedTextField(
                        value = refreshSoilDays.toString(),
                        onValueChange = { refreshSoilDays = it.toIntOrNull() ?: refreshSoilDays },
                        label = { Text("Topsoil (days)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Moss)
                    )

                    // Repot Days
                    OutlinedTextField(
                        value = repotDays.toString(),
                        onValueChange = { repotDays = it.toIntOrNull() ?: repotDays },
                        label = { Text("Repot (days)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Moss)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Mute)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val common = selectedMasterPlant?.commonName ?: searchQuery.ifBlank { "Custom Plant" }
                            val botanical = selectedMasterPlant?.botanicalName ?: botanicalNameInput
                            val name = nicknameInput.ifBlank { common }

                            onConfirm(
                                name,
                                common,
                                botanical,
                                selectedRoom,
                                selectedMasterPlant,
                                null, // customPhotoUri can be added later or taken with camera
                                wateringDays,
                                fertilizeDays,
                                refreshSoilDays,
                                repotDays,
                                notesInput
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Moss),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save to Garden", color = SurfaceWarm, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Inline Add Room Dialog
        if (showInlineAddRoom) {
            AlertDialog(
                onDismissRequest = {
                    showInlineAddRoom = false
                    newRoomName = ""
                },
                title = { Text("Add Space", color = Forest, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newRoomName,
                        onValueChange = { newRoomName = it },
                        placeholder = { Text("e.g. Patio, Terrace, Greenhouse") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Moss)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = newRoomName.trim()
                            if (trimmed.isNotBlank()) {
                                onAddRoom(trimmed)
                                selectedRoom = trimmed
                                newRoomName = ""
                                showInlineAddRoom = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Moss)
                    ) {
                        Text("Add", color = SurfaceWarm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showInlineAddRoom = false
                        newRoomName = ""
                    }) {
                        Text("Cancel", color = Mute)
                    }
                },
                containerColor = SurfaceWarm,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
