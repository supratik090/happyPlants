package com.hos.happy.plants.feature.garden.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.data.remote.model.PlantDiseaseResult
import com.hos.happy.plants.feature.garden.GardenViewModel
import com.hos.happy.plants.feature.research.components.LivePlantCameraDialog
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.LeafBright
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.Terracotta

/**
 * Full-screen disease diagnosis sheet. The user can upload up to 3 photos (camera or gallery),
 * then tap "Scan for diseases" to call the PlantNet Diseases API. Results are displayed with
 * confidence scores and treatment advice.
 */
@Composable
fun PlantDiseaseDiagnosisSheet(
    plant: PlantEntity,
    viewModel: GardenViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val diseaseState by viewModel.diseaseState.collectAsState()

    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var showLiveCamera by remember { mutableStateOf(false) }

    // Clear state when entering the sheet (so stale results don't show)
    DisposableEffect(Unit) {
        viewModel.clearDiseaseState()
        onDispose { /* keep results visible after navigating back */ }
    }

    // Gallery picker — converts URI to bitmap and hands it to ViewModel
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                bitmap?.let { bmp -> viewModel.addDiseaseImage(bmp) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(containerColor = PaperDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ─── Header ───────────────────────────────────────────────────────
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
                Column {
                    Text(
                        text = "Disease scan",
                        style = MaterialTheme.typography.titleLarge,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = plant.nickname,
                        style = MaterialTheme.typography.bodySmall,
                        color = Bark,
                        fontSize = 12.sp
                    )
                }
            }

            // ─── Instruction card ─────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔬 How it works", fontWeight = FontWeight.Bold, color = Forest, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add 1–3 photos of the sick plant (showing affected leaves, spots, or stems). " +
                                "All photos must be of the same plant. Tap \"Scan for diseases\" and we'll " +
                                "identify probable issues and how to treat them.",
                        color = Bark,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // ─── Photo upload grid (up to 3 slots) ───────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Affected plant photos (${diseaseState.selectedBitmaps.size}/3)",
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 15.sp
                    )
                    val remaining = diseaseState.remainingScansToday
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (remaining > 0) Leaf.copy(alpha = 0.2f) else Terracotta.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (remaining > 0) "$remaining/3 daily scans left" else "0/3 scans left today",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remaining > 0) Forest else Terracotta
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Show uploaded photos
                    diseaseState.selectedBitmaps.forEachIndexed { index, bitmap ->
                        DiseasePhotoSlot(
                            bitmap = bitmap,
                            onRemove = { viewModel.removeDiseaseImage(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Add slot if fewer than 3 photos
                    if (diseaseState.selectedBitmaps.size < 3) {
                        AddPhotoSlot(
                            onClick = { showPhotoSourceDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Empty spacers for remaining slots
                    val emptySlotsNeeded = 2 - diseaseState.selectedBitmaps.size
                    repeat(emptySlotsNeeded.coerceAtLeast(0)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // ─── Scan button ──────────────────────────────────────────────────
            val isLimitReached = diseaseState.remainingScansToday <= 0
            Button(
                onClick = { viewModel.diagnosePlantDiseases() },
                enabled = diseaseState.selectedBitmaps.isNotEmpty() && !diseaseState.isLoading && !isLimitReached,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Terracotta,
                    disabledContainerColor = Sand
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (diseaseState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Scanning...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                } else if (isLimitReached) {
                    Text("Daily limit reached (3/3 used today)", color = Mute, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                } else {
                    Text("🔎  Scan for diseases", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // ─── Loading indicator ────────────────────────────────────────────
            AnimatedVisibility(visible = diseaseState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceWarm)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Terracotta,
                        trackColor = Sand
                    )
                    Text(
                        text = "Analysing ${diseaseState.selectedBitmaps.size} photo(s) for diseases…",
                        color = Bark,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Powered by Pl@ntNet AI",
                        color = Mute,
                        fontSize = 10.sp
                    )
                }
            }

            // ─── Error state ──────────────────────────────────────────────────
            diseaseState.error?.let { errorMsg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Terracotta.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Terracotta.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(errorMsg, color = Terracotta, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }

            // ─── Results ──────────────────────────────────────────────────────
            if (diseaseState.results.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Diagnosis results",
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Results sorted by confidence. This is a preliminary scan — " +
                                "consult a plant specialist for severe cases.",
                        color = Mute,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    diseaseState.results.forEachIndexed { index, result ->
                        DiseaseResultCard(result = result, rank = index + 1)
                    }
                }

                // PlantNet attribution
                Text(
                    text = "Disease identification powered by Pl@ntNet (my.plantnet.org)",
                    color = Mute,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ─── Photo source dialog (Camera / Gallery) ───────────────────────────────
    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Add Photo", fontWeight = FontWeight.Bold, color = Forest) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Photograph the affected leaf, stem, or spot clearly.",
                        color = Bark,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Camera option
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
                                Text("Take Photo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Forest)
                                Text("Use camera to capture the issue", fontSize = 11.sp, color = Mute)
                            }
                        }
                    }

                    // Gallery option
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoSourceDialog = false
                                galleryLauncher.launch("image/*")
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
                                Text("Choose from Gallery", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Forest)
                                Text("Pick an existing photo from device", fontSize = 11.sp, color = Mute)
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

    // ─── Live Camera Dialog ────────────────────────────────────────────────────
    if (showLiveCamera) {
        LivePlantCameraDialog(
            onDismiss = { showLiveCamera = false },
            onPhotoCaptured = { bitmap ->
                showLiveCamera = false
                viewModel.addDiseaseImage(bitmap)
            }
        )
    }
}

// ─── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun DiseasePhotoSlot(
    bitmap: Bitmap,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Disease photo",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, Forest.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        // Remove button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoSlot(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWarm)
            .border(1.5.dp, Sand, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Moss.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add photo",
                    tint = Moss,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add photo",
                color = Moss,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DiseaseResultCard(
    result: PlantDiseaseResult,
    rank: Int
) {
    val confidenceColor = when {
        result.confidencePercent >= 70 -> Terracotta
        result.confidencePercent >= 40 -> Color(0xFFE89828) // amber
        else -> Mute
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Sand, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: rank + disease name + confidence badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    // Rank badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when (rank) {
                                    1 -> Terracotta.copy(alpha = 0.15f)
                                    2 -> Color(0xFFE89828).copy(alpha = 0.12f)
                                    else -> Sand
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            color = when (rank) {
                                1 -> Terracotta
                                2 -> Color(0xFFE89828)
                                else -> Mute
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = result.diseaseName,
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "EPPO: ${result.eppoCode}",
                            color = Mute,
                            fontSize = 10.sp
                        )
                    }
                }

                // Confidence badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(confidenceColor.copy(alpha = 0.12f))
                        .border(1.dp, confidenceColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${result.confidencePercent}%",
                        color = confidenceColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Reference image from API (if available)
            result.referenceImageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Disease reference image",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Sand, RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Reference image from Pl@ntNet database",
                        color = Mute,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Sand)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Treatment advice section
            Text(
                text = "💊 How to treat",
                fontWeight = FontWeight.Bold,
                color = Forest,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = result.treatmentAdvice,
                color = Bark,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            // Info link if available
            result.infoUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔗 More info: ${url.take(60)}${if (url.length > 60) "…" else ""}",
                    color = Moss,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
