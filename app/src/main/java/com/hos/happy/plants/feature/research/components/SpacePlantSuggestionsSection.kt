package com.hos.happy.plants.feature.research.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.feature.research.SpacePlantSuggestionItem
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.HappyTagBg
import com.hos.happy.plants.ui.theme.HappyTagText
import com.hos.happy.plants.ui.theme.HungryTagBg
import com.hos.happy.plants.ui.theme.HungryTagText
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.ThirstyTagBg
import com.hos.happy.plants.ui.theme.ThirstyTagText

@Composable
fun SpacePlantSuggestionsSection(
    spaceSuggestions: List<SpacePlantSuggestionItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSelectPlant: (MasterPlant) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show if there are spaces with snapshots
    if (spaceSuggestions.isEmpty()) {
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header Row
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
                        .clip(RoundedCornerShape(8.dp))
                        .background(Leaf.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🪴", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Suggestions by Growing Space",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Matched to recorded snapshot readings",
                        style = MaterialTheme.typography.labelSmall,
                        color = Bark,
                        fontSize = 11.sp
                    )
                }
            }

            // Refresh Button
            val infiniteTransition = rememberInfiniteTransition(label = "refreshSpin")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing)
                ),
                label = "rotation"
            )

            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh suggestions",
                    tint = Moss,
                    modifier = Modifier
                        .size(18.dp)
                        .then(if (isRefreshing) Modifier.rotate(rotation) else Modifier)
                )
            }
        }

        // Space Suggestion Cards
        spaceSuggestions.forEach { spaceItem ->
            SpaceSuggestionCard(
                spaceItem = spaceItem,
                onSelectPlant = onSelectPlant
            )
        }
    }
}

@Composable
private fun SpaceSuggestionCard(
    spaceItem: SpacePlantSuggestionItem,
    onSelectPlant: (MasterPlant) -> Unit
) {
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
                .padding(14.dp)
        ) {
            // Space Name & Snapshot Reading Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Moss,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = spaceItem.roomTag,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 14.sp
                    )
                }

                // Metric Pills from Snapshot
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SnapshotLabelPill(text = spaceItem.lightLabel, type = "LIGHT")
                    SnapshotLabelPill(text = spaceItem.tempLabel, type = "TEMP")
                    SnapshotLabelPill(text = spaceItem.humidityLabel, type = "HUM")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Suggestions List or Empty State
            if (spaceItem.suggestedPlants.isNotEmpty()) {
                Text(
                    text = "Top ${spaceItem.suggestedPlants.size} recommended houseplants for this space:",
                    fontSize = 11.sp,
                    color = Bark,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontally scrollable row of suggested plants
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    spaceItem.suggestedPlants.forEach { plant ->
                        SuggestedPlantMiniCard(
                            plant = plant,
                            onClick = { onSelectPlant(plant) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Paper)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No catalog plants match this space's extreme conditions yet.",
                        fontSize = 11.sp,
                        color = Mute,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestedPlantMiniCard(
    plant: MasterPlant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
            .border(1.dp, Sand.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Paper),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val imgUrl = plant.displayImageUrl ?: plant.image?.thumbnailUrl ?: plant.image?.directImageUrl
            if (imgUrl != null) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = plant.commonName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceWarm),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌿", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = plant.commonName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Forest,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = plant.botanicalName,
                fontSize = 10.sp,
                color = Bark,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Light requirements hint
            val idealMin = plant.lightLux?.idealMin?.toInt() ?: 1000
            val idealMax = plant.lightLux?.idealMax?.toInt() ?: 5000
            Text(
                text = "☀️ ${formatLuxK(idealMin)}–${formatLuxK(idealMax)} lux",
                fontSize = 9.sp,
                color = Moss,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SnapshotLabelPill(
    text: String,
    type: String
) {
    val (bgColor, textColor) = when (text.lowercase()) {
        "bright indirect", "comfortable", "moderate" -> Pair(HappyTagBg, HappyTagText)
        "very bright", "cool", "humid" -> Pair(Leaf.copy(alpha = 0.2f), Forest)
        "sunny", "warm", "dry" -> Pair(ThirstyTagBg, ThirstyTagText)
        "dim", "hot", "cold", "very humid" -> Pair(HungryTagBg, HungryTagText)
        else -> Pair(Paper, Bark)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private fun formatLuxK(lux: Int): String {
    return if (lux >= 1000) {
        val k = lux / 1000
        "${k}k"
    } else {
        "$lux"
    }
}
