package com.hos.happy.plants.feature.garden.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.feature.garden.TodayCareTask
import com.hos.happy.plants.ui.theme.AmberLight
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.Terracotta

@Composable
fun TodayActionsView(
    tasks: List<TodayCareTask>,
    onActionClicked: (TodayCareTask) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        Card(
            modifier = modifier
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
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Leaf.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = Forest,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "All Caught Up! 🌿",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "All essential and routine activities for your plants are completed. Check back tomorrow!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        val essentialTasks = tasks.filter { it.isEssential }
        val goodToHaveTasks = tasks.filter { !it.isEssential }

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Essential Section
            if (essentialTasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Terracotta.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🚨 Essential (${essentialTasks.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Terracotta
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Must do for healthy plants",
                            fontSize = 12.sp,
                            color = Mute
                        )
                    }

                    essentialTasks.forEach { task ->
                        TodayTaskCard(
                            task = task,
                            onActionClicked = { onActionClicked(task) }
                        )
                    }
                }
            }

            // Good to Have Section
            if (goodToHaveTasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Leaf.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✨ Good to Have (${goodToHaveTasks.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Routine maintenance & care",
                            fontSize = 12.sp,
                            color = Mute
                        )
                    }

                    goodToHaveTasks.forEach { task ->
                        TodayTaskCard(
                            task = task,
                            onActionClicked = { onActionClicked(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayTaskCard(
    task: TodayCareTask,
    onActionClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (task.isEssential) Terracotta.copy(alpha = 0.4f) else Sand,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Plant image & information
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.plant.photoUri != null) {
                        AsyncImage(
                            model = task.plant.photoUri,
                            contentDescription = task.plant.nickname,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Paper),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(task.activity.icon, fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(task.activity.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.activity.title,
                                fontWeight = FontWeight.Bold,
                                color = Forest,
                                fontSize = 15.sp
                            )
                        }

                        Text(
                            text = "${task.plant.nickname} • ${task.plant.roomTag}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Bark,
                            fontSize = 12.sp,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Urgency Badge
                        val badgeColor = if (task.isOverdue) Terracotta else if (task.isEssential) AmberLight else Moss
                        val badgeText = if (task.isOverdue) {
                            "Overdue by ${task.urgencyDays}d"
                        } else {
                            "Due today"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isOverdue) Terracotta else Forest
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Action Button
                Button(
                    onClick = onActionClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isEssential) Terracotta else Moss
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mark Done",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWarm
                    )
                }
            }

            // Guidance hint
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Paper)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "💡 ${task.goldenRule}",
                    fontSize = 11.sp,
                    color = Ink,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
