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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tune
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
                                        snackbarHostState.showSnackbar("Daily limit reached (3/3 photo identifications used). Resets at 12:00 AM IST.")
                                    }
                                } else {
                                    showLiveCamera = true
                                }
                            },
                            onPickGallery = {
                                if (uiState.remainingDailyIdentifications <= 0) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Daily limit reached (3/3 photo identifications used). Resets at 12:00 AM IST.")
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
                            },
                            onQuickFilter = { filter ->
                                researchViewModel.selectCategory(filter)
                                researchViewModel.setMode(ResearchMode.BROWSE)
                            }
                        )
                    }

                    ResearchMode.BROWSE -> {
                        // Option 2: Browse Encyclopedia Option with Multidimensional Filters
                        BrowseOptionView(
                            plants = uiState.browsePlants,
                            totalCount = uiState.totalPlantCount,
                            filterCriteria = uiState.filterCriteria,
                            isLoading = uiState.isBrowseLoading,
                            onSelectTemperature = { researchViewModel.setTemperatureFilter(it) },
                            onSelectHumidity = { researchViewModel.setHumidityFilter(it) },
                            onSelectWaterCare = { researchViewModel.setWaterCareFilter(it) },
                            onSelectLight = { researchViewModel.setLightFilter(it) },
                            onClearAllFilters = { researchViewModel.clearAllFilters() },
                            onSelectPlant = { researchViewModel.selectPlant(it) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
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
    onAddIdentifiedPlantToGarden: (PlantIdentificationResult) -> Unit,
    onQuickFilter: (String) -> Unit = {}
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
            Column(modifier = Modifier.padding(18.dp)) {
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
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Leaf.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌿", fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Find or Identify Any Plant",
                            style = MaterialTheme.typography.titleMedium,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Daily attempt count badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (remainingDailyIdentifications > 0) Leaf.copy(alpha = 0.3f) else Terracotta.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$remainingDailyIdentifications/3 left",
                                fontSize = 11.sp,
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

                Spacer(modifier = Modifier.height(14.dp))

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

                Spacer(modifier = Modifier.height(14.dp))

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

                Spacer(modifier = Modifier.height(14.dp))

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
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
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
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = if (isLimitReached) Mute else Moss, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("From Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isLimitReached) Mute else Moss)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Daily limit: 3 photo identifications • Resets at 12:00 AM IST",
                    fontSize = 10.sp,
                    color = Mute,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        // Quick Filters bar in Search Mode
        if (searchQuery.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "Explore by Climate & Care",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "🔥 Warm Temp" to "Warm",
                        "❄️ Cold Tolerant" to "Cold",
                        "🌤️ Moderate Temp" to "Moderate Temp",
                        "🌵 Low Water" to "Low Water",
                        "🪴 Medium Water" to "Medium Water",
                        "💦 High Water" to "High Water",
                        "🏜️ Low Humidity" to "Low Humidity",
                        "🌧️ High Humidity" to "High Humidity",
                        "🌑 Low Light" to "Low Light",
                        "☀️ Bright Light" to "Bright Light"
                    ).forEach { (label, key) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceWarm)
                                .border(1.dp, Sand, RoundedCornerShape(10.dp))
                                .clickable { onQuickFilter(key) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(label, fontSize = 12.sp, color = Forest, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Space Plant Suggestions (shown directly below Find or Identify)
        if (searchQuery.isBlank() && spaceSuggestions.isNotEmpty()) {
            SpacePlantSuggestionsSection(
                spaceSuggestions = spaceSuggestions,
                isRefreshing = isRefreshingSuggestions,
                onRefresh = onRefreshSuggestions,
                onSelectPlant = onSelectPlant,
                modifier = Modifier.padding(bottom = 20.dp)
            )
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
                        text = "Try searching by common name, botanical genus, or regional nursery name — or use the Live Camera / Gallery buttons above to identify it with Pl@ntNet AI.",
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
                        text = "Popular Houseplants",
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
    totalCount: Int,
    filterCriteria: PlantFilterCriteria,
    isLoading: Boolean = false,
    onSelectTemperature: (String?) -> Unit,
    onSelectHumidity: (String?) -> Unit,
    onSelectWaterCare: (String?) -> Unit,
    onSelectLight: (String?) -> Unit,
    onClearAllFilters: () -> Unit,
    onSelectPlant: (MasterPlant) -> Unit
) {
    var isFilterExpanded by remember { mutableStateOf(filterCriteria.isActive) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Multi-dimensional Filter Card (Compact & Collapsible)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Sand, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Header with Toggle & Active Count & Reset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isFilterExpanded = !isFilterExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (filterCriteria.isActive) Moss else Paper),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = if (filterCriteria.isActive) SurfaceWarm else Forest,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Text(
                            text = "Filter Encyclopedia",
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            fontSize = 14.sp
                        )

                        if (filterCriteria.isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Leaf.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${filterCriteria.activeCount} active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (filterCriteria.isActive) {
                            Text(
                                text = "Reset",
                                fontSize = 12.sp,
                                color = Terracotta,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onClearAllFilters() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Icon(
                            imageVector = if (isFilterExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isFilterExpanded) "Collapse" else "Expand",
                            tint = Forest,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Active filters pill strip when collapsed
                if (!isFilterExpanded && filterCriteria.isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        filterCriteria.temperature?.let { temp ->
                            ActiveFilterChip("🌡️ $temp") { onSelectTemperature(null) }
                        }
                        filterCriteria.humidity?.let { hum ->
                            ActiveFilterChip("💨 $hum") { onSelectHumidity(null) }
                        }
                        filterCriteria.waterCare?.let { water ->
                            ActiveFilterChip("💧 $water") { onSelectWaterCare(null) }
                        }
                        filterCriteria.light?.let { light ->
                            ActiveFilterChip("☀️ $light") { onSelectLight(null) }
                        }
                    }
                }

                // Expanded Filter Options (Single horizontal scroll row per category)
                AnimatedVisibility(visible = isFilterExpanded) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = Sand.copy(alpha = 0.5f))

                        // 1. Temperature: Warm, Cold, Moderate
                        FilterCategoryRow(
                            icon = "🌡️",
                            title = "Temp",
                            options = listOf(
                                "Warm" to "🔥 Warm",
                                "Cold" to "❄️ Cold Tolerant",
                                "Moderate" to "🌤️ Moderate"
                            ),
                            selected = filterCriteria.temperature,
                            onSelect = onSelectTemperature
                        )

                        // 2. Humidity: Low, Moderate, High
                        FilterCategoryRow(
                            icon = "💨",
                            title = "Humidity",
                            options = listOf(
                                "Low" to "🏜️ Low (<40%)",
                                "Moderate" to "🍃 Moderate",
                                "High" to "🌧️ High (>60%)"
                            ),
                            selected = filterCriteria.humidity,
                            onSelect = onSelectHumidity
                        )

                        // 3. Water Care: Low, Medium, High
                        FilterCategoryRow(
                            icon = "💧",
                            title = "Water",
                            options = listOf(
                                "Low" to "🌵 Low (drought)",
                                "Medium" to "🪴 Medium",
                                "High" to "💦 High (frequent)"
                            ),
                            selected = filterCriteria.waterCare,
                            onSelect = onSelectWaterCare
                        )

                        // 4. Light Exposure: Low, Moderate, Bright
                        FilterCategoryRow(
                            icon = "☀️",
                            title = "Light",
                            options = listOf(
                                "Low" to "🌑 Low Light",
                                "Moderate" to "⛅ Moderate",
                                "Bright" to "☀️ Bright"
                            ),
                            selected = filterCriteria.light,
                            onSelect = onSelectLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Results count header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${plants.size} of $totalCount Plants:",
                fontSize = 14.sp,
                color = Forest,
                fontWeight = FontWeight.Bold
            )
            if (filterCriteria.isActive) {
                Text(
                    text = "Filtered",
                    fontSize = 12.sp,
                    color = Moss,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
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
                        .padding(vertical = 44.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = Forest,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading Plant Library...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preparing 220 botanical species & care guidelines",
                        fontSize = 12.sp,
                        color = Mute
                    )
                }
            }
        } else if (plants.isEmpty()) {
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌿", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No plants match all selected filters",
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try deselecting one of the filters or tap Reset All to view all species.",
                        color = Mute,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onClearAllFilters,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Reset All Filters", fontSize = 11.sp, color = Forest)
                    }
                }
            }
        } else {
            var visibleItemCount by remember(plants) { mutableStateOf(30) }
            val displayedPlants = plants.take(visibleItemCount)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                displayedPlants.forEach { plant ->
                    PlantBrowseCard(
                        plant = plant,
                        onClick = { onSelectPlant(plant) }
                    )
                }

                if (visibleItemCount < plants.size) {
                    val remaining = plants.size - visibleItemCount
                    val nextBatch = remaining.coerceAtMost(30)
                    Button(
                        onClick = { visibleItemCount += nextBatch },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Moss),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Load More Plants ($remaining remaining)",
                            color = SurfaceWarm,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterCategoryRow(
    icon: String,
    title: String,
    options: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left category label (compact fixed width)
        Row(
            modifier = Modifier.width(76.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Bark,
                maxLines = 1
            )
        }

        // Right compact horizontal scroll row of options
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All" chip
            val isAll = selected == null
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isAll) Moss else Paper)
                    .border(1.dp, if (isAll) Moss else Sand, RoundedCornerShape(8.dp))
                    .clickable { onSelect(null) }
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "All",
                    fontSize = 11.sp,
                    fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                    color = if (isAll) SurfaceWarm else Ink
                )
            }

            // Category options
            options.forEach { (key, label) ->
                val isSelected = selected == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Moss else Paper)
                        .border(1.dp, if (isSelected) Moss else Sand, RoundedCornerShape(8.dp))
                        .clickable { onSelect(if (isSelected) null else key) }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SurfaceWarm else Ink
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(
    label: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Leaf.copy(alpha = 0.25f))
            .border(1.dp, Leaf, RoundedCornerShape(8.dp))
            .clickable { onRemove() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Forest
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            tint = Forest,
            modifier = Modifier.size(12.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    plant.watering?.hotDryMonths?.let { hot ->
                        Text(
                            text = "💧 ${hot.min}–${hot.max}d",
                            fontSize = 11.sp,
                            color = Moss,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    plant.temperature?.let { temp ->
                        val minT = temp.idealMin ?: temp.tolerableMin
                        val maxT = temp.idealMax ?: temp.tolerableMax
                        if (minT != null && maxT != null) {
                            Text(
                                text = "• 🌡️ ${minT.toInt()}–${maxT.toInt()}°C",
                                fontSize = 11.sp,
                                color = Bark
                            )
                        }
                    }
                    plant.humidity?.let { hum ->
                        if (hum.min != null && hum.max != null) {
                            Text(
                                text = "• 💨 ${hum.min.toInt()}–${hum.max.toInt()}%",
                                fontSize = 11.sp,
                                color = Mute
                            )
                        }
                    }
                }
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
