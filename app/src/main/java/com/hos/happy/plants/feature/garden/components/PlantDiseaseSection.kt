package com.hos.happy.plants.feature.garden.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.Terracotta

/**
 * Plant Disease Diagnosis Section embedded directly below "Something wrong with this plant?".
 * Allows uploading up to 3 pictures from camera or gallery, calls Pl@ntNet diseases API,
 * and displays diagnosis results with how to treat them.
 */
@Composable
fun PlantDiseaseSection(
    plant: PlantEntity,
    viewModel: GardenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val diseaseState by viewModel.diseaseState.collectAsState()

    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var showLiveCamera by remember { mutableStateOf(false) }

    // Gallery image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    bitmap?.let { bmp -> viewModel.addDiseaseImage(bmp) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Plant Disease Diagnosis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Forest,
                fontSize = 17.sp
            )
            if (diseaseState.selectedBitmaps.isNotEmpty() || diseaseState.results.isNotEmpty() || diseaseState.error != null) {
                TextButton(
                    onClick = { viewModel.clearDiseaseState() },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = Moss,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset",
                        color = Moss,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Main Card Container
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header & Info
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔬", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Disease & Treatment Scanner",
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upload up to 3 photos of affected leaves or stems from camera or gallery to identify plant diseases and how to treat them.",
                        fontSize = 12.sp,
                        color = Bark,
                        lineHeight = 17.sp
                    )
                }

                // Photo Upload Slots (up to 3 photos) + Daily limit badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Photos (${diseaseState.selectedBitmaps.size}/3)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Forest
                    )

                    // Daily limit badge: 3/day
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Uploaded photos with delete badge
                    diseaseState.selectedBitmaps.forEachIndexed { index, bitmap ->
                        Box(modifier = Modifier.weight(1f)) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Plant photo ${index + 1}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, Forest.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Remove button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .clickable { viewModel.removeDiseaseImage(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    // Add Photo Slot (if less than 3)
                    if (diseaseState.selectedBitmaps.size < 3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Paper)
                                .border(1.dp, Sand, RoundedCornerShape(12.dp))
                                .clickable { showPhotoSourceDialog = true },
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
                                    fontSize = 11.sp,
                                    color = Moss,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Empty spacers to keep slot widths even when 1 or 2 slots are present
                    val emptySlots = 2 - diseaseState.selectedBitmaps.size
                    repeat(emptySlots.coerceAtLeast(0)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Scan Action Button
                val isLimitReached = diseaseState.remainingScansToday <= 0
                Button(
                    onClick = { viewModel.diagnosePlantDiseases() },
                    enabled = diseaseState.selectedBitmaps.isNotEmpty() && !diseaseState.isLoading && !isLimitReached,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        disabledContainerColor = Sand
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    if (diseaseState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scanning with Pl@ntNet AI…",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else if (isLimitReached) {
                        Text(
                            text = "Daily limit reached (3/3 used today)",
                            color = Mute,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = if (diseaseState.selectedBitmaps.isEmpty()) "Add photo to diagnose" else "🔎  Identify disease & treatment",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (isLimitReached && diseaseState.error == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Sand, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Paper),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏳", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Daily limit reached (3/3 used). Resets at 12:00 AM IST.",
                                fontSize = 12.sp,
                                color = Bark,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Loading Progress Indicator
                AnimatedVisibility(visible = diseaseState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Terracotta,
                            trackColor = Sand
                        )
                        Text(
                            text = "Matching symptoms against botanical pathologies…",
                            fontSize = 11.sp,
                            color = Mute
                        )
                    }
                }

                // Error Message (When API is down, out of quota, or failed)
                diseaseState.error?.let { errorMsg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Terracotta.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Terracotta.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMsg,
                                color = Terracotta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Results: Probable Diseases & How to Treat Them
                if (diseaseState.results.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Diagnosis & Treatment",
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            fontSize = 15.sp
                        )

                        diseaseState.results.forEachIndexed { index, result ->
                            DiseaseInlineResultCard(result = result, rank = index + 1)
                        }

                        // Attribution
                        Text(
                            text = "Pathology identification powered by Pl@ntNet Diseases API",
                            color = Mute,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Photo Source Picker Dialog (Camera vs Gallery)
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
                        text = "Take a clear close-up of the affected leaf, stem, or symptom:",
                        fontSize = 12.sp,
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
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Moss.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Moss,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Take Photo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest
                                )
                                Text(
                                    text = "Capture using camera viewfinder",
                                    fontSize = 11.sp,
                                    color = Mute
                                )
                            }
                        }
                    }

                    // Option 2: Gallery Picker
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
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Leaf.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Forest,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Choose from Gallery",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest
                                )
                                Text(
                                    text = "Pick an existing image from phone",
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
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Live Camera Dialog
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

/**
 * Result card displaying individual detected disease, score, reference image,
 * and comprehensive treatment instructions.
 */
@Composable
private fun DiseaseInlineResultCard(
    result: PlantDiseaseResult,
    rank: Int
) {
    val confidenceColor = when {
        result.confidencePercent >= 70 -> Terracotta
        result.confidencePercent >= 40 -> Color(0xFFE89828)
        else -> Mute
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Sand, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Paper),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Rank, Name, Confidence Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                when (rank) {
                                    1 -> Terracotta.copy(alpha = 0.15f)
                                    2 -> Color(0xFFE89828).copy(alpha = 0.15f)
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

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = result.diseaseName,
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "EPPO Code: ${result.eppoCode}",
                            color = Mute,
                            fontSize = 10.sp
                        )
                    }
                }

                // Confidence pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(confidenceColor.copy(alpha = 0.12f))
                        .border(1.dp, confidenceColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${result.confidencePercent}% match",
                        color = confidenceColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Reference Image from PlantNet (if present)
            result.referenceImageUrl?.let { imageUrl ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceWarm)
                        .padding(8.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Disease illustration",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pl@ntNet Reference Image",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Forest
                        )
                        Text(
                            text = "Verified botanical illustration",
                            fontSize = 10.sp,
                            color = Mute
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Sand.copy(alpha = 0.7f))
            )

            // Treatment Advice
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "💊 How to treat it:",
                    fontWeight = FontWeight.Bold,
                    color = Forest,
                    fontSize = 12.sp
                )
                Text(
                    text = result.treatmentAdvice,
                    fontSize = 12.sp,
                    color = Bark,
                    lineHeight = 17.sp
                )
            }

            // More Information Link
            result.infoUrl?.let { url ->
                Text(
                    text = "🌐 More info: $url",
                    color = Moss,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
