package com.hos.happy.plants.feature.research.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.model.PlantIdentificationResult
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SkyBlue
import com.hos.happy.plants.ui.theme.SurfaceWarm

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdentifiedPlantCard(
    result: PlantIdentificationResult,
    onDismiss: () -> Unit,
    onAddToGarden: (PlantIdentificationResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Leaf, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Confidence Badge & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Forest)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌿", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Identified Plant",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWarm
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Mute,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Images Row: Reference Image + User Captured Photo Thumbnail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Identified Reference Image (PlantNet or Wikipedia or Master Plant)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Paper)
                        .border(1.dp, Sand, RoundedCornerShape(14.dp))
                ) {
                    if (!result.referenceImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = result.referenceImageUrl,
                            contentDescription = result.commonName,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier.matchParentSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🌿", fontSize = 36.sp)
                            Text("Species Reference", fontSize = 11.sp, color = Mute)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Forest.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Reference Photo", fontSize = 10.sp, color = SurfaceWarm, fontWeight = FontWeight.SemiBold)
                    }
                }

                // User's Live Captured Photo
                result.userCapturedBitmap?.let { userBmp ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Paper)
                            .border(1.dp, Sand, RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            bitmap = userBmp.asImageBitmap(),
                            contentDescription = "Your Photo",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Moss.copy(alpha = 0.85f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Your Photo", fontSize = 10.sp, color = SurfaceWarm, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Identified Identity
            Text(
                text = result.commonName,
                style = MaterialTheme.typography.headlineSmall,
                color = Forest,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = result.botanicalName,
                style = MaterialTheme.typography.titleMedium,
                color = Bark,
                fontStyle = FontStyle.Italic
            )

            // Family / Genus Tag
            if (!result.family.isNullOrBlank() || !result.genus.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                val taxonomy = listOfNotNull(
                    result.genus?.let { "Genus: $it" },
                    result.family?.let { "Family: $it" }
                ).joinToString(" • ")
                Text(
                    text = taxonomy,
                    fontSize = 11.sp,
                    color = Mute,
                    fontWeight = FontWeight.Medium
                )
            }

            // Indian Regional Names (if matched to catalog or extracted)
            result.localNames?.let { localMap ->
                if (localMap.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Regional & Nursery Names:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Bark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        localMap.forEach { (lang, name) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Paper)
                                    .border(1.dp, Sand, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${lang.replaceFirstChar { it.uppercase() }}: $name",
                                    fontSize = 11.sp,
                                    color = Ink
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Sand, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Light Conditions Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Paper)
                    .border(1.dp, Sand, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Leaf.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Forest, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Light Conditions", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Forest)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(result.lightCondition, fontSize = 12.sp, color = Ink, lineHeight = 17.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Moisture / Watering Needs Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Paper)
                    .border(1.dp, Sand, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SkyBlue.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Forest, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Moisture & Watering Needs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Forest)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(result.moistureNeed, fontSize = 12.sp, color = Ink, lineHeight = 17.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Potting Soil Mix Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Paper)
                    .border(1.dp, Sand, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("🪴 Soil & Potting Recipe", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Forest)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(result.soilAdvice, fontSize = 12.sp, color = Bark, lineHeight = 17.sp)
                }
            }

            // Description / Extract if available
            result.description?.let { desc ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = Mute,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add to Garden Action Button
            Button(
                onClick = { onAddToGarden(result) },
                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Garden", color = SurfaceWarm, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
