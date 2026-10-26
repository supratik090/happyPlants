package com.hos.happy.plants.feature.research

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.data.model.PlantIdentificationResult
import com.hos.happy.plants.feature.garden.GardenViewModel
import com.hos.happy.plants.feature.garden.components.AddPlantDialog
import com.hos.happy.plants.feature.research.components.IdentifiedPlantCard
import com.hos.happy.plants.feature.research.components.LivePlantCameraDialog
import com.hos.happy.plants.feature.research.components.PlantDetailView
import com.hos.happy.plants.feature.research.components.PlantNetApiKeyDialog
import com.hos.happy.plants.feature.research.components.SpacePlantSuggestionsSection
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
import com.hos.happy.plants.ui.theme.Terracotta
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResearchScreen(
    researchViewModel: ResearchViewModel,
    gardenViewModel: GardenViewModel? = null,
    onSwitchProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by researchViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialogForPlant by remember { mutableStateOf<MasterPlant?>(null) }
    var showAddDialogForIdentified by remember { mutableStateOf<PlantIdentificationResult?>(null) }
    var showLiveCamera by remember { mutableStateOf(false) }
    val availableRooms by (gardenViewModel?.availableRoomTags?.collectAsState() ?: remember { mutableStateOf(listOf("Living", "Bed", "Balcony")) })

    // Gallery launcher for picking images
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                researchViewModel.identifyPlant(bitmap)
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Could not decode image from gallery.")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Plant Research",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Encyclopedia & AI Identifier",
                            style = MaterialTheme.typography.labelSmall,
                            color = Bark
                        )
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PaperDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Segmented 2 Options Bar: [ 🔍 Search ] vs [ 🌿 Browse Library ]
            SegmentedResearchTabBar(
                selectedMode = uiState.activeMode,
                onModeSelected = { researchViewModel.setMode(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // If a plant is currently selected, show its full encyclopedic detail view!
            if (uiState.selectedPlant != null) {
                PlantDetailView(
                    plant = uiState.selectedPlant!!,
                    onBack = { researchViewModel.clearSearch() },
                    onAddToGarden = { plant ->
                        if (gardenViewModel != null) {
                            showAddDialogForPlant = plant
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please open the Garden tab to add plants.")
                            }
                        }
                    }
                )
            } else {
                when (uiState.activeMode) {
                    ResearchMode.SEARCH -> {
                        // Option 1: Search Option (with highlighted search bar and Live Photo identification)
                        SearchOptionView(
                            searchQuery = uiState.searchQuery,
                            searchResults = uiState.searchResults,
                            popularPlants = uiState.popularPlants,
                            spaceSuggestions = uiState.spaceSuggestions,
                            isRefreshingSuggestions = uiState.isRefreshingSuggestions,
                            onRefreshSuggestions = { researchViewModel.refreshSuggestions() },
                            isIdentifying = uiState.isIdentifying,
                            identificationError = uiState.identificationError,
                            identificationResult = uiState.identificationResult,
                            remainingDailyIdentifications = uiState.remainingDailyIdentifications,
                            onQueryChanged = { researchViewModel.onSearchQueryChanged(it) },
                            onClearSearch = { researchViewModel.clearSearch() },
                            onSelectPlant = { researchViewModel.selectPlant(it) },
                            onTakePhoto = {
                                if (uiState.remainingDailyIdentifications <= 0) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Daily limit reached (5/5 photo identifications used). Resets at 12:00 AM IST.")
                                    }
                                } else {
                                    showLiveCamera = true
                                }
                            },
                            onPickGallery = {
                                if (uiState.remainingDailyIdentifications <= 0) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Daily limit reached (5/5 photo identifications used). Resets at 12:00 AM IST.")
                                    }
                                } else {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            },
                            onConfigureApiKey = { researchViewModel.showApiKeyDialog() },
                            onDismissIdentification = { researchViewModel.clearIdentification() },
                            onAddIdentifiedPlantToGarden = { identified ->
                                if (gardenViewModel != null) {
                                    showAddDialogForIdentified = identified
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please open the Garden tab to add plants.")
                                    }
                                }
                            }
                        )
                    }

                    ResearchMode.BROWSE -> {
                        // Option 2: Browse Encyclopedia Option
                        BrowseOptionView(
                            plants = uiState.browsePlants,
                            selectedCategory = uiState.selectedCategory,
                            onSelectCategory = { researchViewModel.selectCategory(it) },
                            onSelectPlant = { researchViewModel.selectPlant(it) }
                        )
                    }
                }
            }
        }
    }

    // Add Plant Dialog if triggered from Master Plant selection
    showAddDialogForPlant?.let { masterPlant ->
        if (gardenViewModel != null) {
            AddPlantDialog(
                masterRepo = gardenViewModel.masterRepo,
                availableRooms = availableRooms,
                onAddRoom = { gardenViewModel.addCustomRoom(it) },
                onDismiss = { showAddDialogForPlant = null },
                initialMasterPlant = masterPlant,
                onConfirm = { nickname, commonName, botanicalName, roomTag, mPlant, customPhotoUri, waterDays, fertDays, soilDays, repotDays, notes ->
                    gardenViewModel.createPlant(
                        nickname = nickname,
                        commonName = commonName,
                        botanicalName = botanicalName,
                        roomTag = roomTag,
                        masterPlant = mPlant ?: masterPlant,
                        customPhotoUri = customPhotoUri,
                        wateringDays = waterDays,
                        fertilizeDays = fertDays,
                        refreshSoilDays = soilDays,
                        repotDays = repotDays,
                        notes = notes
                    )
                    showAddDialogForPlant = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Added '$commonName' to your Garden!")
                    }
                }
            )
        }
    }

    // Add Plant Dialog if triggered from AI Identification Result
    showAddDialogForIdentified?.let { identified ->
        if (gardenViewModel != null) {
            AddPlantDialog(
                masterRepo = gardenViewModel.masterRepo,
                availableRooms = availableRooms,
                onAddRoom = { gardenViewModel.addCustomRoom(it) },
                onDismiss = { showAddDialogForIdentified = null },
                initialMasterPlant = identified.masterPlant,
                initialCommonName = identified.commonName,
                initialBotanicalName = identified.botanicalName,
                initialPhotoUri = identified.referenceImageUrl,
                onConfirm = { nickname, commonName, botanicalName, roomTag, mPlant, customPhotoUri, waterDays, fertDays, soilDays, repotDays, notes ->
                    gardenViewModel.createPlant(
                        nickname = nickname,
                        commonName = commonName,
                        botanicalName = botanicalName,
                        roomTag = roomTag,
                        masterPlant = mPlant ?: identified.masterPlant,
                        customPhotoUri = customPhotoUri ?: identified.referenceImageUrl,
                        wateringDays = waterDays,
                        fertilizeDays = fertDays,
                        refreshSoilDays = soilDays,
                        repotDays = repotDays,
                        notes = notes
                    )
                    showAddDialogForIdentified = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Added '$commonName' to your Garden!")
                    }
                }
            )
        }
    }

    // Pl@ntNet API Key Dialog
    if (uiState.showApiKeyDialog) {
        PlantNetApiKeyDialog(
            initialKey = uiState.currentApiKey,
            onDismiss = { researchViewModel.dismissApiKeyDialog() },
            onSaveKey = { researchViewModel.saveApiKey(it) }
        )
    }

    // Live Plant Camera Dialog (CameraX Viewfinder with Leaf Reticle)
    if (showLiveCamera) {
        LivePlantCameraDialog(
            onDismiss = { showLiveCamera = false },
            onPhotoCaptured = { bitmap ->
                showLiveCamera = false
                researchViewModel.identifyPlant(bitmap)
            }
        )
    }
}

@Composable
private fun SegmentedResearchTabBar(
    selectedMode: ResearchMode,
    onModeSelected: (ResearchMode) -> Unit
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
            // Option 1: Search (renamed from Typeahead Search)
            val isSearchSelected = selectedMode == ResearchMode.SEARCH
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSearchSelected) Moss else SurfaceWarm)
                    .clickable { onModeSelected(ResearchMode.SEARCH) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔍 Search",
                        fontSize = 13.sp,
                        fontWeight = if (isSearchSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSearchSelected) SurfaceWarm else Bark
                    )
                }
            }

            // Option 2: Browse Encyclopedia
            val isBrowseSelected = selectedMode == ResearchMode.BROWSE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isBrowseSelected) Moss else SurfaceWarm)
                    .clickable { onModeSelected(ResearchMode.BROWSE) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📚 Browse Library",
                        fontSize = 13.sp,
                        fontWeight = if (isBrowseSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isBrowseSelected) SurfaceWarm else Bark
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchOptionView(
    searchQuery: String,
    searchResults: List<MasterPlant>,
    popularPlants: List<MasterPlant>,
    spaceSuggestions: List<com.hos.happy.plants.feature.research.SpacePlantSuggestionItem>,
    isRefreshingSuggestions: Boolean,
    onRefreshSuggestions: () -> Unit,
    isIdentifying: Boolean,
    identificationError: String?,
    identificationResult: PlantIdentificationResult?,
    remainingDailyIdentifications: Int,
    onQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSelectPlant: (MasterPlant) -> Unit,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    onConfigureApiKey: () -> Unit,
    onDismissIdentification: () -> Unit,
    onAddIdentifiedPlantToGarden: (PlantIdentificationResult) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // 1. Highlighted, Active Search & Identification Container - ALWAYS AT THE VERY TOP
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Forest, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with icon, daily limit badge, and API key button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Leaf.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌿", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Find or Identify Any Plant",
                            style = MaterialTheme.typography.titleMedium,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Daily attempt count badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (remainingDailyIdentifications > 0) Leaf.copy(alpha = 0.3f) else Terracotta.copy(alpha = 0.2f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$remainingDailyIdentifications/5 left",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingDailyIdentifications > 0) Forest else Terracotta
                            )
                        }

                        IconButton(
                            onClick = onConfigureApiKey,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Pl@ntNet API Key",
                                tint = Bark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Highlighted Search Input Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChanged,
                    placeholder = { Text("Search by name, nursery term, or genus...", fontSize = 13.sp, color = Mute) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Forest) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Mute)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Forest,
                        unfocusedBorderColor = Sand,
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        cursorColor = Forest
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Visual Divider with Caption
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Sand, thickness = 0.5.dp)
                    Text(
                        text = "  OR IDENTIFY FROM PHOTO  ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Bark
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Sand, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Camera & Gallery Photo Buttons
                val isLimitReached = remainingDailyIdentifications <= 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTakePhoto,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLimitReached) Mute else Moss
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Live Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SurfaceWarm)
                    }

                    OutlinedButton(
                        onClick = onPickGallery,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isLimitReached) Mute else Moss),
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = if (isLimitReached) Mute else Moss, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("From Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isLimitReached) Mute else Moss)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Daily limit: 5 photo identifications • Resets at 12:00 AM IST",
                    fontSize = 10.sp,
                    color = Mute,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Identifying in progress state (displayed BELOW the search card)
        if (isIdentifying) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Moss, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Moss,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Identifying plant features...",
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Analyzing botanical genus, light needs & seasonal watering...",
                        fontSize = 11.sp,
                        color = Mute
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Error state if identification failed or limit reached (displayed BELOW the search card)
        if (!identificationError.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Terracotta.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Identification Note", fontWeight = FontWeight.Bold, color = Terracotta, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(identificationError, fontSize = 12.sp, color = Ink, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onConfigureApiKey,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Moss, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("API Key Settings", color = Moss, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(
                            onClick = onDismissIdentification,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Dismiss", color = Mute, fontSize = 11.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Identified Plant Result displayed BELOW find or identify section!
        if (identificationResult != null) {
            IdentifiedPlantCard(
                result = identificationResult,
                onDismiss = onDismissIdentification,
                onAddToGarden = onAddIdentifiedPlantToGarden
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Space Plant Suggestions (shown directly below Find or Identify)
        if (searchQuery.isBlank() && spaceSuggestions.isNotEmpty()) {
            SpacePlantSuggestionsSection(
                spaceSuggestions = spaceSuggestions,
                isRefreshing = isRefreshingSuggestions,
                onRefresh = onRefreshSuggestions,
                onSelectPlant = onSelectPlant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Autocomplete Results Dropdown / List
        if (searchResults.isNotEmpty()) {
            Text(
                text = "Matching Species (${searchResults.size}):",
                style = MaterialTheme.typography.titleSmall,
                color = Forest,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                searchResults.forEach { plant ->
                    PlantSearchResultCard(
                        plant = plant,
                        onClick = { onSelectPlant(plant) }
                    )
                }
            }
        } else if (searchQuery.isNotBlank()) {
            // No matches found in catalog
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No catalog matches found",
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching by English common name, botanical genus, or Indian nursery name — or use the Live Camera / Gallery buttons above to identify it with Pl@ntNet AI.",
                        color = Mute,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Empty search: show helpful quick-discovery cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Popular Indian Houseplants",
                        style = MaterialTheme.typography.titleSmall,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap any plant to inspect sunlight needs, watering frequency, and potting mix recipes:",
                        fontSize = 11.sp,
                        color = Mute
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularPlants.forEach { plant ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Paper)
                                    .border(1.dp, Sand, RoundedCornerShape(10.dp))
                                    .clickable { onSelectPlant(plant) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🌿", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = plant.commonName,
                                        fontSize = 12.sp,
                                        color = Ink,
                                        fontWeight = FontWeight.Medium
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BrowseOptionView(
    plants: List<MasterPlant>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onSelectPlant: (MasterPlant) -> Unit
) {
    val categories = listOf("All", "Low Light", "Moderate Light", "Bright Light", "Low Water")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Browse by Light & Care Category",
            style = MaterialTheme.typography.titleSmall,
            color = Forest,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onSelectCategory(category) },
                    label = { Text(category, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Moss,
                        selectedLabelColor = SurfaceWarm,
                        containerColor = Paper
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategory == category,
                        borderColor = Sand
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Showing ${plants.size} Plants:",
            fontSize = 12.sp,
            color = Bark,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            plants.forEach { plant ->
                PlantBrowseCard(
                    plant = plant,
                    onClick = { onSelectPlant(plant) }
                )
            }
        }
    }
}

@Composable
private fun PlantSearchResultCard(
    plant: MasterPlant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Sand, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imgUrl = plant.displayImageUrl ?: plant.image?.thumbnailUrl ?: plant.image?.directImageUrl
            if (imgUrl != null) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = plant.commonName,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Paper),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌿", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.commonName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Forest
                )
                Text(
                    text = plant.botanicalName,
                    fontSize = 11.sp,
                    color = Bark,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                plant.localNames?.values?.firstOrNull()?.let { local ->
                    Text(
                        text = "Regional: $local",
                        fontSize = 10.sp,
                        color = Mute
                    )
                }
            }

            // Watering interval badge
            plant.watering?.hotDryMonths?.let { hot ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Paper)
                        .border(1.dp, Sand, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "💧 ~${hot.min}d",
                        fontSize = 11.sp,
                        color = Moss,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantBrowseCard(
    plant: MasterPlant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Sand, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imgUrl = plant.displayImageUrl ?: plant.image?.thumbnailUrl ?: plant.image?.directImageUrl
            if (imgUrl != null) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = plant.commonName,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Paper),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌿", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.commonName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Forest
                )
                Text(
                    text = plant.botanicalName,
                    fontSize = 12.sp,
                    color = Bark,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    plant.watering?.hotDryMonths?.let { hot ->
                        Text(
                            text = "💧 Summer: ${hot.min}–${hot.max}d",
                            fontSize = 11.sp,
                            color = Moss,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    plant.lightLux?.idealMin?.let { minLux ->
                        Text(
                            text = "• ☀️ ${minLux.toInt()} lx",
                            fontSize = 11.sp,
                            color = Mute
                        )
                    }
                }
            }
        }
    }
}

/**
 * Decodes Bitmap from Uri using modern ImageDecoder (API 28+) or MediaStore (pre-28)
 */
private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
