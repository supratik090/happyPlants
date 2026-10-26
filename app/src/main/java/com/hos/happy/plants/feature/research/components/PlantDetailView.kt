package com.hos.happy.plants.feature.research.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.ui.theme.AmberLight
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
import com.hos.happy.plants.ui.theme.Terracotta

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlantDetailView(
    plant: MasterPlant,
    onBack: () -> Unit,
    onAddToGarden: (MasterPlant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back navigation button
        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Sand),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Moss, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back to Search", color = Moss, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        // Hero Header Card: Image & Identity
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Sand, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val imageUrl = plant.displayImageUrl ?: plant.image?.thumbnailUrl ?: plant.image?.directImageUrl
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = plant.commonName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plant.commonName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = plant.botanicalName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Bark,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Button(
                        onClick = { onAddToGarden(plant) },
                        colors = ButtonDefaults.buttonColors(containerColor = Moss),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to Garden", color = SurfaceWarm, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Indian Regional Names
                plant.localNames?.let { localMap ->
                    if (localMap.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Indian Nursery / Regional Names:",
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
            }
        }

        // Card 1: Best Growing Conditions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Sand, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Leaf.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Forest, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Best Growing Conditions",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Light Conditions
                plant.lightLux?.let { light ->
                    ConditionMetricRow(
                        icon = "☀️",
                        label = "Light Needs",
                        ideal = "Ideal: ${light.idealMin?.toInt() ?: "-"} – ${light.idealMax?.toInt() ?: "-"} lx",
                        detail = "Tolerates: ${light.minTolerated?.toInt() ?: "-"} – ${light.maxTolerated?.toInt() ?: "-"} lx (check with Light Meter)"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Temperature Conditions
                plant.temperature?.let { temp ->
                    ConditionMetricRow(
                        icon = "🌡️",
                        label = "Temperature",
                        ideal = "Ideal: ${temp.idealMin?.toInt() ?: "-"}°C – ${temp.idealMax?.toInt() ?: "-"}°C",
                        detail = "Extreme Limits: ${temp.tolerableMin?.toInt() ?: "-"}°C – ${temp.tolerableMax?.toInt() ?: "-"}°C"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Humidity Conditions
                plant.humidity?.let { hum ->
                    ConditionMetricRow(
                        icon = "💧",
                        label = "Relative Humidity",
                        ideal = "Ideal: ${hum.min?.toInt() ?: "-"}% – ${hum.max?.toInt() ?: "-"}%",
                        detail = if ((hum.min ?: 0f) >= 60f) "Appreciates misting or humid bathrooms" else "Adapts well to normal household air"
                    )
                }
            }
        }

        // Card 2: Care Routine & Schedules
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Sand, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SkyBlue.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Forest, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Care Routine & Watering Frequency",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Watering breakdown
                plant.watering?.let { water ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Paper)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("🚰 Watering Schedule", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Forest)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("☀️ Hot/Dry Months (Mar–Jun):", fontSize = 12.sp, color = Ink)
                                Text(
                                    "Every ${water.hotDryMonths?.min ?: 7}–${water.hotDryMonths?.max ?: 10} days",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Moss
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🌧️ Monsoon & Winter (Jul–Feb):", fontSize = 12.sp, color = Ink)
                                Text(
                                    "Every ${water.monsoonAndWinter?.min ?: 14}–${water.monsoonAndWinter?.max ?: 20} days",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Moss
                                )
                            }

                            water.note?.let { note ->
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Sand, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "💡 $note",
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Bark
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Maintenance intervals row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CareIntervalBadge(
                        modifier = Modifier.weight(1f),
                        emoji = "🌿",
                        label = "Fertilize",
                        interval = "Every ${plant.fertilizeEveryDays ?: 60}d"
                    )
                    CareIntervalBadge(
                        modifier = Modifier.weight(1f),
                        emoji = "🪴",
                        label = "Top Soil",
                        interval = "Every ${plant.refreshTopSoilEveryDays ?: 180}d"
                    )
                    CareIntervalBadge(
                        modifier = Modifier.weight(1f),
                        emoji = "🔄",
                        label = "Repot",
                        interval = "Every ${plant.repotEveryDays ?: 730}d"
                    )
                }
            }
        }

        // Card 3: Soil & Potting Recipe
        plant.soil?.let { soil ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🪴 Potting Mix & Soil Guidance",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    soil.inSimpleWords?.let { words ->
                        Text(text = words, fontSize = 12.sp, color = Ink, lineHeight = 17.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    soil.mixRecipe?.let { recipe ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Paper)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Suggested Mix Recipe:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(recipe, fontSize = 12.sp, color = Bark, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    if (soil.phMin != null && soil.phMax != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Optimal pH: ${soil.phMin} – ${soil.phMax}",
                            fontSize = 11.sp,
                            color = Mute
                        )
                    }
                }
            }
        }

        // Card 4: Common Problems & Solutions
        if (plant.commonProblems.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AmberLight.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Forest, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Common Problems & Diagnostic Solutions",
                            style = MaterialTheme.typography.titleMedium,
                            color = Forest,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    plant.commonProblems.forEachIndexed { index, problem ->
                        if (index > 0) {
                            HorizontalDivider(color = Sand.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 10.dp))
                        }

                        Column {
                            Text(
                                text = "⚠️ ${problem.problem}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Symptom: ${problem.doYouSeeThisProblem}",
                                fontSize = 11.sp,
                                color = Mute
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Text("How to solve:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Moss)
                            problem.solveItLikeThis.forEach { step ->
                                Text(
                                    text = " • $step",
                                    fontSize = 11.sp,
                                    color = Ink,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ConditionMetricRow(
    icon: String,
    label: String,
    ideal: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Forest)
            Text(ideal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Moss)
            Text(detail, fontSize = 11.sp, color = Mute)
        }
    }
}

@Composable
private fun CareIntervalBadge(
    emoji: String,
    label: String,
    interval: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Paper)
            .border(1.dp, Sand, RoundedCornerShape(10.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = Mute)
            Text(interval, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest)
        }
    }
}
