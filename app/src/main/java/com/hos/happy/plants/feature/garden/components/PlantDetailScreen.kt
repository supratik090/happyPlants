package com.hos.happy.plants.feature.garden.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.platform.LocalContext
import com.hos.happy.plants.feature.research.components.LivePlantCameraDialog
import java.io.File
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.local.entity.PlantCareLogEntity
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.data.local.entity.PlantPhotoEntity
import com.hos.happy.plants.feature.garden.GardenViewModel
import com.hos.happy.plants.feature.garden.PlantGardenTag
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.DustyTagBg
import com.hos.happy.plants.ui.theme.DustyTagText
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.HappyTagBg
import com.hos.happy.plants.ui.theme.HappyTagText
import com.hos.happy.plants.ui.theme.HungryTagBg
import com.hos.happy.plants.ui.theme.HungryTagText
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.LeafBright
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.Terracotta
import com.hos.happy.plants.ui.theme.ThirstyTagBg
import com.hos.happy.plants.ui.theme.ThirstyTagText
import com.hos.happy.plants.data.model.WateringSchedule
import com.hos.happy.plants.data.model.WateringSchedule.Companion.fromEntity
import com.hos.happy.plants.data.model.WateringSchedule.Companion.isDueToday
import com.hos.happy.plants.data.model.WateringSchedule.Companion.daysUntilNext
import com.hos.happy.plants.data.model.WateringSchedule.Companion.scheduleLabel
import com.hos.happy.plants.data.model.WateringSchedule.Companion.nextWateringEpoch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlantDetailScreen(
    plant: PlantEntity,
    viewModel: GardenViewModel,
    onBack: () -> Unit,
    onOpenDoctor: () -> Unit,
    onOpenDiseaseScan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val careLogs by viewModel.getCareLogsForPlantFlow(plant.id).collectAsState(initial = emptyList())
    val photos by viewModel.getPhotosForPlantFlow(plant.id).collectAsState(initial = emptyList())
    val tags = remember(plant) { viewModel.getTagsForPlant(plant) }
    val context = LocalContext.current

    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var showLiveCamera by remember { mutableStateOf(false) }
    var pendingPhotoUri by remember { mutableStateOf<String?>(null) }
    var pendingPhotoNote by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showDeletePhotoDialog by remember { mutableStateOf(false) }
    var showScheduleSheet by remember { mutableStateOf(false) }
    var showEditNicknameDialog by remember { mutableStateOf(false) }
    var editNicknameInput by remember { mutableStateOf(plant.nickname) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            try {
                val photosDir = File(context.filesDir, "plant_photos")
                if (!photosDir.exists()) photosDir.mkdirs()
                val photoFile = File(photosDir, "plant_${plant.id}_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    photoFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                pendingPhotoUri = Uri.fromFile(photoFile).toString()
            } catch (e: Exception) {
                pendingPhotoUri = sourceUri.toString()
            }
            pendingPhotoNote = ""
            showAddNoteDialog = true
        }
    }

    val now = System.currentTimeMillis()
    val oneDayMillis = 86_400_000L
    val daysSinceCreated = remember(plant.createdAt, now) {
        maxOf(1, ((now - plant.createdAt) / oneDayMillis).toInt())
    }
    val plantCreationDateFormatted = remember(plant.createdAt) {
        SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date(plant.createdAt))
    }

    val daysSinceWater = plant.lastWateredAt?.let { ((now - it) / oneDayMillis).toInt() }
    val daysSinceClean = (plant.lastCleanedAt ?: plant.createdAt).let { ((now - it) / oneDayMillis).toInt() }
    val daysSinceFeed = (plant.lastFertilizedAt ?: plant.createdAt).let { ((now - it) / oneDayMillis).toInt() }
    val customSchedule = remember(plant.waterScheduleType, plant.waterScheduleValue) {
        fromEntity(plant.waterScheduleType, plant.waterScheduleValue)
    }
    val nextWaterRemaining = if (customSchedule != null) {
        customSchedule.daysUntilNext(now, plant.lastWateredAt, plant.waterScheduleSetAt)
    } else {
        plant.wateringIntervalDays - (daysSinceWater ?: plant.wateringIntervalDays)
    }

    // Care timeline active filter
    var selectedTimelineFilter by remember { mutableStateOf("All") }

    // Growth journey slideshow state
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }
    var isPlayingSlideshow by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(isPlayingSlideshow, photos.size, selectedPhotoIndex, playbackSpeed) {
        if (isPlayingSlideshow && photos.size > 1) {
            val delayMs = (2500 / playbackSpeed).toLong()
            delay(delayMs)
            selectedPhotoIndex = (selectedPhotoIndex + 1) % photos.size
        }
    }

    Scaffold(
        containerColor = PaperDark
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Top Header with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceWarm)
                        .border(1.dp, Sand, RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Forest,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Plant details",
                    style = MaterialTheme.typography.titleLarge,
                    color = Forest,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. Hero Card (Thumbnail, Name, Room, Tags, Action Buttons)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val latestUserPhoto = photos.lastOrNull()?.photoUri
                        val defaultPhoto = plant.photoUri?.takeIf { it.startsWith("file://") || it.startsWith("content://") }
                            ?: plant.masterPlantId?.let { viewModel.masterRepo.getPlantById(it)?.displayImageUrl }
                            ?: plant.photoUri
                        val resolvedPhoto = latestUserPhoto?.takeIf { it.isNotBlank() } ?: defaultPhoto

                        if (!resolvedPhoto.isNullOrBlank()) {
                            AsyncImage(
                                model = resolvedPhoto,
                                contentDescription = plant.nickname,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Paper),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🪴", fontSize = 32.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = plant.nickname,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest,
                                    fontSize = 20.sp,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                IconButton(
                                    onClick = {
                                        editNicknameInput = plant.nickname
                                        showEditNicknameDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit nickname",
                                        tint = Moss,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val plantSubtitle = if (!plant.nickname.trim().equals(plant.commonName.trim(), ignoreCase = true)) {
                                "${plant.roomTag} • ${plant.commonName} (${plant.botanicalName})"
                            } else {
                                "${plant.roomTag} • ${plant.botanicalName}"
                            }
                            Text(
                                text = plantSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Bark,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Tag Pills
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                tags.forEach { tag ->
                                    PlantStatusTagChip(tag = tag)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Button: "Add photo"
                    OutlinedButton(
                        onClick = { showPhotoSourceDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Sand),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWarm),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = Moss,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add photo",
                            color = Forest,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 3. Care so far (Stats grid: dynamically show ONLY known metrics)
            val knownCareStats = remember(plant, careLogs, daysSinceWater, daysSinceClean, daysSinceFeed) {
                val items = mutableListOf<CareStatItem>()

                // 1. Watered - only if known and performed (> 0)
                val waterCount = careLogs.count { it.careType == "WATER" }
                if (waterCount > 0) {
                    val sub = daysSinceWater?.let { formatDaysAgo(it) } ?: "Recorded"
                    items.add(CareStatItem("💧", "$waterCount", "Watered", sub))
                }

                // 2. Cleaned - only if known and performed (> 0)
                val cleanCount = careLogs.count { it.careType == "CLEAN" }
                if (cleanCount > 0) {
                    val days = plant.lastCleanedAt?.let { ((now - it) / oneDayMillis).toInt() }
                    val sub = days?.let { "$it days ago" } ?: "Recorded"
                    items.add(CareStatItem("✦", "$cleanCount", "Cleaned", sub))
                }

                // 3. Fed - only if known and performed (> 0)
                val feedCount = careLogs.count { it.careType in listOf("FEED", "FEED_MANURE", "FERTILIZE") }
                if (feedCount > 0) {
                    val days = plant.lastFertilizedAt?.let { ((now - it) / oneDayMillis).toInt() }
                    val sub = days?.let { "$it days ago" } ?: "Recorded"
                    items.add(CareStatItem("🪣", "$feedCount", "Fed", sub))
                }

                // 4. Repotted - only if known and performed (> 0)
                val repotCount = careLogs.count { it.careType == "REPOT" }
                if (repotCount > 0) {
                    val sub = plant.lastRepottedAt?.let { formatShortDate(it) } ?: "Recorded"
                    items.add(CareStatItem("🪴", "$repotCount", "Repotted", sub))
                }

                // 5. Pruned - only if known and performed (> 0)
                val pruneCount = careLogs.count { it.careType == "PRUNE" }
                if (pruneCount > 0) {
                    val sub = plant.lastPrunedAt?.let { formatShortDate(it) } ?: "Recorded"
                    items.add(CareStatItem("✂", "$pruneCount", "Pruned", sub))
                }

                // 6. Next watering
                if (plant.wateringIntervalDays > 0) {
                    val schedSubtext = customSchedule?.scheduleLabel() ?: "every ${plant.wateringIntervalDays} days"
                    items.add(
                        CareStatItem(
                            icon = "💧",
                            countText = if (nextWaterRemaining <= 0) "Today" else "In ${nextWaterRemaining}d",
                            label = "Next watering",
                            subtext = schedSubtext,
                            isHighlighted = true
                        )
                    )
                }

                items
            }

            if (knownCareStats.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Care so far",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Since added on $plantCreationDateFormatted ($daysSinceCreated days)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Bark,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val chunkedStats = knownCareStats.chunked(3)
                    chunkedStats.forEachIndexed { rowIndex, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { item ->
                                CareStatGridCard(
                                    modifier = Modifier.weight(1f),
                                    icon = item.icon,
                                    countText = item.countText,
                                    label = item.label,
                                    subtext = item.subtext,
                                    isHighlighted = item.isHighlighted
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        if (rowIndex < chunkedStats.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // 4. Coming up (Schedule list)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Coming up",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Forest,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

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
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Water
                        val (waterScheduleLabel, nextWaterDueStr) = remember(plant) {
                            viewModel.getScheduleInfoForPlant(plant)
                        }
                        val customWaterSchedule = remember(plant) {
                            fromEntity(plant.waterScheduleType, plant.waterScheduleValue)
                        }
                        val isWaterDue = if (customWaterSchedule != null) {
                            customWaterSchedule.isDueToday(now, plant.lastWateredAt, plant.waterScheduleSetAt)
                        } else {
                            (daysSinceWater ?: plant.wateringIntervalDays) >= plant.wateringIntervalDays
                        }
                        val waterLastStr = formatShortDate(plant.lastWateredAt)
                        val waterSubtitle = if (customWaterSchedule != null) {
                            "$waterScheduleLabel · custom schedule"
                        } else if (plant.lastWateredAt != null) {
                            "Last $waterLastStr • every ${plant.wateringIntervalDays} days"
                        } else {
                            "Day 1 • every ${plant.wateringIntervalDays} days"
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ComingUpItemRow(
                                icon = "💧",
                                iconColor = SkyBlueColor,
                                title = "Water",
                                subtitle = waterSubtitle,
                                badgeTop = if (isWaterDue) "Today" else nextWaterDueStr,
                                badgeBottom = if (isWaterDue) "Due now" else "Tap to edit",
                                isUrgent = isWaterDue,
                                onClick = { showScheduleSheet = true }
                            )

                            // Quick Edit Schedule Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Leaf.copy(alpha = 0.15f))
                                    .clickable { showScheduleSheet = true }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🗓️", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Schedule: $waterScheduleLabel",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Forest
                                    )
                                }
                                Text(
                                    text = "Edit schedule ✏️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Moss
                                )
                            }
                        }

                        // Clean leaves
                        if (plant.cleanIntervalDays > 0) {
                            val cleanRemaining = daysSinceClean?.let { maxOf(0, plant.cleanIntervalDays - it) }
                            ComingUpItemRow(
                                icon = "✦",
                                iconColor = DustyPurpleColor,
                                title = "Clean leaves",
                                subtitle = if (plant.lastCleanedAt != null) "Last ${formatShortDate(plant.lastCleanedAt)} • every ${plant.cleanIntervalDays} days" else "Scheduled • every ${plant.cleanIntervalDays} days",
                                badgeTop = if (plant.lastCleanedAt != null) calculateDueDateStr(plant.lastCleanedAt, plant.cleanIntervalDays) else "Scheduled",
                                badgeBottom = if (cleanRemaining == 0) "Due now" else if (cleanRemaining != null) "in $cleanRemaining days" else "every ${plant.cleanIntervalDays}d",
                                isUrgent = cleanRemaining == 0
                            )
                        }

                        // Feed manure
                        if (plant.fertilizeIntervalDays > 0) {
                            val fertRemaining = daysSinceFeed?.let { maxOf(0, plant.fertilizeIntervalDays - it) }
                            ComingUpItemRow(
                                icon = "🪣",
                                iconColor = HungryAmberColor,
                                title = "Feed manure",
                                subtitle = if (plant.lastFertilizedAt != null) "Last ${formatShortDate(plant.lastFertilizedAt)} • every ${plant.fertilizeIntervalDays} days" else "Scheduled • every ${plant.fertilizeIntervalDays} days",
                                badgeTop = if (plant.lastFertilizedAt != null) calculateDueDateStr(plant.lastFertilizedAt, plant.fertilizeIntervalDays) else "Scheduled",
                                badgeBottom = if (fertRemaining == 0) "Due now" else if (fertRemaining != null) "in $fertRemaining days" else "every ${plant.fertilizeIntervalDays}d",
                                isUrgent = fertRemaining == 0
                            )
                        }

                        // Prune
                        if (plant.pruneIntervalDays > 0) {
                            ComingUpItemRow(
                                icon = "✂",
                                iconColor = PruneRedColor,
                                title = "Prune",
                                subtitle = if (plant.lastPrunedAt != null) "Last ${formatShortDate(plant.lastPrunedAt)} • as needed" else "As needed",
                                badgeTop = if (plant.lastPrunedAt != null) calculateDueDateStr(plant.lastPrunedAt, plant.pruneIntervalDays) else "When needed",
                                badgeBottom = "check then"
                            )
                        }

                        // Refresh top soil
                        if (plant.refreshSoilIntervalDays > 0) {
                            val daysSinceSoil = plant.lastSoilRefreshedAt?.let { ((now - it) / oneDayMillis).toInt() }
                            val soilRemaining = daysSinceSoil?.let { maxOf(0, plant.refreshSoilIntervalDays - it) }
                            ComingUpItemRow(
                                icon = "🪨",
                                iconColor = SoilBrownColor,
                                title = "Refresh top soil",
                                subtitle = if (plant.lastSoilRefreshedAt != null) "Last ${formatShortDate(plant.lastSoilRefreshedAt)} • every ${plant.refreshSoilIntervalDays} days" else "Scheduled • every ${plant.refreshSoilIntervalDays} days",
                                badgeTop = if (plant.lastSoilRefreshedAt != null) calculateDueDateStr(plant.lastSoilRefreshedAt, plant.refreshSoilIntervalDays) else "Scheduled",
                                badgeBottom = if (soilRemaining != null) "in $soilRemaining days" else "every 6 mo"
                            )
                        }

                        // Repot
                        if (plant.repotIntervalDays > 0) {
                            val daysSinceRepot = plant.lastRepottedAt?.let { ((now - it) / oneDayMillis).toInt() }
                            val repotRemaining = daysSinceRepot?.let { maxOf(0, plant.repotIntervalDays - it) }
                            ComingUpItemRow(
                                icon = "🪴",
                                iconColor = RepotOliveColor,
                                title = "Repot",
                                subtitle = if (plant.lastRepottedAt != null) "Last ${formatShortDate(plant.lastRepottedAt)} • every 2 years" else "Scheduled • every 2 years",
                                badgeTop = if (plant.lastRepottedAt != null) calculateDueDateStr(plant.lastRepottedAt, plant.repotIntervalDays, includeYear = true) else "Scheduled",
                                badgeBottom = if (repotRemaining != null) "in $repotRemaining days" else "every 2 yrs"
                            )
                        }
                    }
                }
            }

            // 5. Growth journey
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Growth journey",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "${photos.size} photos • $daysSinceCreated days",
                        style = MaterialTheme.typography.bodySmall,
                        color = Bark,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (photos.isEmpty()) {
                    // Empty photo state card
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
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🌱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start your plant's growth journey",
                                fontWeight = FontWeight.Bold,
                                color = Forest,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add photos with notes to track growth milestones, repotting, and new leaves over time.",
                                color = Mute,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showPhotoSourceDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = SurfaceWarm,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add First Photo",
                                    color = SurfaceWarm,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    val safeIndex = selectedPhotoIndex.coerceIn(0, photos.lastIndex)
                    val activePhoto = photos[safeIndex]

                    val photoDateStr = remember(activePhoto.capturedAt) {
                        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(activePhoto.capturedAt))
                    }
                    val photoFullDateTimeStr = remember(activePhoto.capturedAt) {
                        SimpleDateFormat("EEE, d MMM yyyy • h:mm a", Locale.getDefault()).format(Date(activePhoto.capturedAt))
                    }
                    val photoDayIndex = maxOf(1, ((activePhoto.capturedAt - plant.createdAt) / oneDayMillis).toInt())

                    // 1. Big Main Photo Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .border(1.dp, Sand, RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = Paper),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AnimatedContent(
                                targetState = activePhoto,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "detailPhoto"
                            ) { photo ->
                                AsyncImage(
                                    model = photo.photoUri,
                                    contentDescription = photo.caption,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Top overlay row: Day badge on left, Quick Delete button on right
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Forest.copy(alpha = 0.85f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$photoDateStr • Day $photoDayIndex",
                                        color = SurfaceWarm,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceWarm.copy(alpha = 0.92f))
                                        .clickable { showDeletePhotoDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete photo",
                                        tint = Terracotta,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Photo Notes and Date Card just below the photo
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
                                .padding(14.dp)
                        ) {
                            // Date and Delete row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Forest,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = photoFullDateTimeStr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Forest
                                    )
                                }

                                TextButton(
                                    onClick = { showDeletePhotoDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = Terracotta,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Delete",
                                        color = Terracotta,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Note display section
                            val noteText = activePhoto.caption?.takeIf { it.isNotBlank() } ?: "No note recorded for this photo."
                            val hasCustomNote = !activePhoto.caption.isNullOrBlank() && activePhoto.caption != "Photo added"

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Paper)
                                    .border(1.dp, Sand.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                                        contentDescription = null,
                                        tint = if (hasCustomNote) Moss else Sand,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = noteText,
                                        fontSize = 13.sp,
                                        color = if (hasCustomNote) Ink else Mute,
                                        lineHeight = 18.sp,
                                        fontStyle = if (hasCustomNote) FontStyle.Normal else FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. "Watch it grow" playback bar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Sand, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Forest)
                                        .clickable { isPlayingSlideshow = !isPlayingSlideshow },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingSlideshow) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlayingSlideshow) "Pause" else "Play",
                                        tint = SurfaceWarm,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Watch it grow",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Forest
                                    )
                                    Text(
                                        text = "Plays photos from early to latest",
                                        fontSize = 11.sp,
                                        color = Mute
                                    )
                                }
                            }

                            // Speed toggle button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Paper)
                                    .border(1.dp, Sand, RoundedCornerShape(8.dp))
                                    .clickable {
                                        playbackSpeed = when (playbackSpeed) {
                                            1f -> 2f
                                            2f -> 0.5f
                                            else -> 1f
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${playbackSpeed}x".replace(".0", ""),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Horizontal Thumbnail Strip with Add button and all thumbnails below
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "+" Add Photo thumbnail card
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showPhotoSourceDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Paper)
                                    .border(1.5.dp, Moss, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Add photo",
                                    tint = Forest,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Add",
                                fontSize = 10.sp,
                                color = Forest,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Photo Thumbnails
                        photos.forEachIndexed { index, photo ->
                            val isSelected = index == safeIndex
                            val thumbDateStr = remember(photo.capturedAt) {
                                SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(photo.capturedAt))
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedPhotoIndex = index }
                            ) {
                                AsyncImage(
                                    model = photo.photoUri,
                                    contentDescription = photo.caption,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) Forest else Sand,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = thumbDateStr,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Forest else Mute,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // 6. Care timeline
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Care timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Forest,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                val timelineFilterOptions = listOf("All", "Water", "Clean", "Food", "Repot", "Prune")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    timelineFilterOptions.forEach { filter ->
                        FilterChip(
                            selected = selectedTimelineFilter == filter,
                            onClick = { selectedTimelineFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Forest,
                                selectedLabelColor = SurfaceWarm,
                                containerColor = SurfaceWarm
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedTimelineFilter == filter,
                                borderColor = Sand
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val filteredLogs = remember(careLogs, selectedTimelineFilter) {
                    careLogs.filter { log ->
                        when (selectedTimelineFilter) {
                            "All" -> true
                            "Water" -> log.careType == "WATER"
                            "Clean" -> log.careType == "CLEAN"
                            "Food" -> log.careType in listOf("FEED", "FEED_MANURE", "FERTILIZE")
                            "Repot" -> log.careType == "REPOT"
                            "Prune" -> log.careType == "PRUNE"
                            else -> true
                        }
                    }
                }

                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceWarm)
                            .border(1.dp, Sand, RoundedCornerShape(12.dp))
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No care logs recorded yet for $selectedTimelineFilter.",
                            fontSize = 12.sp,
                            color = Mute
                        )
                    }
                } else {
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
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            filteredLogs.take(10).forEach { log ->
                                CareTimelineItemRow(log = log)
                            }
                        }
                    }
                }
            }

            // 7. "Something wrong with this plant?" Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDoctor),
                colors = CardDefaults.cardColors(containerColor = Forest),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Something wrong with this plant?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWarm,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Find common problems and how to fix them",
                            style = MaterialTheme.typography.bodySmall,
                            color = LeafBright,
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Diagnose",
                        tint = SurfaceWarm,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 8. Plant Disease Diagnosis & Treatment (Below 'Something wrong with this plant')
            PlantDiseaseSection(
                plant = plant,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Photo source picker dialog: Camera or Gallery
        if (showPhotoSourceDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoSourceDialog = false },
                title = {
                    Text(
                        text = "Add Plant Photo",
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Choose how to capture ${plant.nickname}'s progress:",
                            fontSize = 13.sp,
                            color = Bark
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 1: Live Camera
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPhotoSourceDialog = false
                                    showLiveCamera = true
                                }
                                .border(1.dp, Sand, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Paper),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Moss.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Moss,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Take Photo",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Forest
                                    )
                                    Text(
                                        text = "Use live camera with plant viewfinder",
                                        fontSize = 11.sp,
                                        color = Mute
                                    )
                                }
                            }
                        }

                        // Option 2: Choose from Gallery
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPhotoSourceDialog = false
                                    photoPickerLauncher.launch("image/*")
                                }
                                .border(1.dp, Sand, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Paper),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Leaf.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = Forest,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Choose from Gallery",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Forest
                                    )
                                    Text(
                                        text = "Pick an existing photo from device",
                                        fontSize = 11.sp,
                                        color = Mute
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPhotoSourceDialog = false }) {
                        Text("Cancel", color = Bark)
                    }
                },
                containerColor = SurfaceWarm,
                shape = RoundedCornerShape(18.dp)
            )
        }

        // Live Camera Dialog
        if (showLiveCamera) {
            LivePlantCameraDialog(
                onDismiss = { showLiveCamera = false },
                onPhotoCaptured = { bitmap ->
                    showLiveCamera = false
                    try {
                        val photosDir = File(context.filesDir, "plant_photos")
                        if (!photosDir.exists()) photosDir.mkdirs()
                        val photoFile = File(photosDir, "plant_${plant.id}_${System.currentTimeMillis()}.jpg")
                        photoFile.outputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                        }
                        pendingPhotoUri = Uri.fromFile(photoFile).toString()
                        pendingPhotoNote = ""
                        showAddNoteDialog = true
                    } catch (e: Exception) {
                        // Ignore write error
                    }
                }
            )
        }

        // Add Photo with Note Dialog
        if (showAddNoteDialog && pendingPhotoUri != null) {
            AlertDialog(
                onDismissRequest = {
                    showAddNoteDialog = false
                    pendingPhotoUri = null
                    pendingPhotoNote = ""
                },
                title = {
                    Text(
                        text = "Add Photo & Note",
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = pendingPhotoUri,
                            contentDescription = "New photo preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Sand, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = pendingPhotoNote,
                            onValueChange = { pendingPhotoNote = it },
                            label = { Text("Note (e.g. New leaf, repotted)") },
                            placeholder = { Text("How is the plant looking today?") },
                            singleLine = false,
                            maxLines = 3,
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
                            val note = pendingPhotoNote.trim().ifEmpty { "Photo added" }
                            viewModel.addPlantPhoto(plant.id, pendingPhotoUri!!, note)
                            selectedPhotoIndex = photos.size // select newly added photo
                            showAddNoteDialog = false
                            pendingPhotoUri = null
                            pendingPhotoNote = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Photo", color = SurfaceWarm, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddNoteDialog = false
                            pendingPhotoUri = null
                            pendingPhotoNote = ""
                        }
                    ) {
                        Text("Cancel", color = Mute)
                    }
                },
                containerColor = SurfaceWarm,
                shape = RoundedCornerShape(18.dp)
            )
        }

        // Delete Photo Confirmation Dialog
        if (showDeletePhotoDialog && photos.isNotEmpty()) {
            val safeIndex = selectedPhotoIndex.coerceIn(0, photos.lastIndex)
            val photoToDelete = photos.getOrNull(safeIndex)
            if (photoToDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDeletePhotoDialog = false },
                    title = {
                        Text(
                            text = "Delete Photo?",
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete this photo and its note? This action cannot be undone.",
                            color = Ink,
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeletePhotoDialog = false
                                viewModel.deletePlantPhoto(photoToDelete.id, photoToDelete.photoUri)
                                if (selectedPhotoIndex >= photos.size - 1) {
                                    selectedPhotoIndex = maxOf(0, photos.size - 2)
                                }
                            }
                        ) {
                            Text("Delete", color = Terracotta, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeletePhotoDialog = false }) {
                            Text("Cancel", color = Forest)
                        }
                    },
                    containerColor = SurfaceWarm,
                    shape = RoundedCornerShape(18.dp)
                )
            }
        }

        // Edit Plant Nickname Dialog
        if (showEditNicknameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNicknameDialog = false },
                title = {
                    Text(
                        text = "Edit Plant Nickname",
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Give this plant a custom nickname to easily identify it in your garden (e.g. Balcony Pothos, Office Snake Plant):",
                            fontSize = 13.sp,
                            color = Bark,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = editNicknameInput,
                            onValueChange = { editNicknameInput = it },
                            label = { Text("Nickname") },
                            placeholder = { Text(plant.commonName) },
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
                            val newName = editNicknameInput.trim()
                            if (newName.isNotBlank()) {
                                viewModel.updatePlantNickname(plant.id, newName)
                                showEditNicknameDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", color = SurfaceWarm, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNicknameDialog = false }) {
                        Text("Cancel", color = Mute)
                    }
                },
                containerColor = SurfaceWarm,
                shape = RoundedCornerShape(18.dp)
            )
        }

        if (showScheduleSheet) {
            val masterDays = viewModel.getSuggestedWateringIntervalDays(plant)
            WateringScheduleBottomSheet(
                plant = plant,
                masterIntervalDays = masterDays,
                onDismiss = { showScheduleSheet = false },
                onSave = { newSchedule ->
                    viewModel.saveWaterSchedule(plant, newSchedule)
                }
            )
        }
    }
}

@Composable
private fun PlantStatusTagChip(tag: PlantGardenTag) {
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
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "${tag.icon} ${tag.label}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private data class CareStatItem(
    val icon: String,
    val countText: String,
    val label: String,
    val subtext: String,
    val isHighlighted: Boolean = false
)

@Composable
private fun CareStatGridCard(
    icon: String,
    countText: String,
    label: String,
    subtext: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = modifier.border(
            1.dp,
            if (isHighlighted) Leaf.copy(alpha = 0.5f) else Sand,
            RoundedCornerShape(14.dp)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) HappyTagBg else SurfaceWarm
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Text(icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = countText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Forest
            )
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Forest
            )
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = if (isHighlighted) Forest else Mute,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ComingUpItemRow(
    icon: String,
    iconColor: Color,
    title: String,
    subtitle: String,
    badgeTop: String,
    badgeBottom: String,
    isUrgent: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Forest
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Bark
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = badgeTop,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isUrgent) Terracotta else Forest
            )
            Text(
                text = badgeBottom,
                fontSize = 10.sp,
                color = if (isUrgent) Terracotta else Mute
            )
        }
    }
}

@Composable
private fun CareTimelineItemRow(log: PlantCareLogEntity) {
    val (icon, iconColor) = when (log.careType) {
        "WATER" -> "💧" to SkyBlueColor
        "CLEAN" -> "✦" to DustyPurpleColor
        "FEED", "FEED_MANURE", "FERTILIZE" -> "🪣" to HungryAmberColor
        "REPOT" -> "🪴" to RepotOliveColor
        "PRUNE" -> "✂" to PruneRedColor
        "PHOTO" -> "📷" to Forest
        else -> "🌱" to Forest
    }

    val actionLabel = when (log.careType) {
        "WATER" -> "Watered"
        "CLEAN" -> "Leaves cleaned"
        "FEED", "FEED_MANURE", "FERTILIZE" -> "Fed manure"
        "REPOT" -> "Repotted"
        "PRUNE" -> "Pruned"
        "PHOTO" -> "Photo added"
        else -> log.careType.lowercase().replaceFirstChar { it.uppercase() }
    }

    val dateFormatted = remember(log.timestamp) {
        val now = System.currentTimeMillis()
        val oneDay = 86_400_000L
        val diff = now - log.timestamp
        if (diff < oneDay) {
            "Today, " + SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(log.timestamp))
        } else {
            SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(log.timestamp))
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = actionLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Forest
            )
            val subnote = log.remark?.takeIf { it.isNotBlank() && it != "Completed" && it != "Logged from details" }
            if (subnote != null) {
                Text(
                    text = subnote,
                    fontSize = 11.sp,
                    color = Ink
                )
            }
        }

        Text(
            text = dateFormatted,
            fontSize = 11.sp,
            color = Mute
        )
    }
}

private fun formatDaysAgo(days: Int?): String {
    return when {
        days == null -> "Never"
        days == 0 -> "Today"
        days == 1 -> "Yesterday"
        else -> "$days days ago"
    }
}

private fun formatShortDate(timestamp: Long?): String {
    if (timestamp == null) return "Never"
    return SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
}

private fun calculateDueDateStr(lastDone: Long, intervalDays: Int, includeYear: Boolean = false): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = lastDone
    cal.add(Calendar.DAY_OF_YEAR, intervalDays)
    val pattern = if (includeYear) "d MMM yyyy" else "d MMM"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(cal.time)
}

// Icon circle colors matching design palette
private val SkyBlueColor = Color(0xFF4A89A6)
private val DustyPurpleColor = Color(0xFF6B478A)
private val HungryAmberColor = Color(0xFFE89828)
private val PruneRedColor = Color(0xFFA0452E)
private val SoilBrownColor = Color(0xFF7A583A)
private val RepotOliveColor = Color(0xFF4F6F3B)
