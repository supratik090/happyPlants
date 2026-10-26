package com.hos.happy.plants.feature.garden.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.local.entity.DailyCareTaskEntity
import com.hos.happy.plants.ui.theme.AmberLight
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.LeafBright
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SkyBlue
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.Terracotta
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TaskCardDeckView(
    tasks: List<DailyCareTaskEntity>,
    onCompleteTask: (DailyCareTaskEntity) -> Unit,
    onOpenSnapshotForSpace: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, Sand, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Leaf.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = Forest,
                        modifier = Modifier.size(34.dp)
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
                    text = "No care tasks pending in your deck today. As the day rolls over at midnight, new daily tasks will be evaluated.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Maintain local ordered deck of task IDs so swipe-left can move cards to the back
    val activeTaskIds = remember { mutableStateListOf<String>() }
    var shakenCardId by remember { mutableStateOf<String?>(null) }
    var shakenAttempts by remember { mutableIntStateOf(0) }
    var priorityWarningMessage by remember { mutableStateOf<String?>(null) }

    // Sync task IDs when the incoming tasks list changes (e.g. initial load or completed task removed)
    LaunchedEffect(tasks) {
        val currentSet = tasks.map { it.id }.toSet()
        activeTaskIds.retainAll(currentSet)
        tasks.forEach { t ->
            if (!activeTaskIds.contains(t.id)) {
                activeTaskIds.add(t.id)
            }
        }
    }

    val taskMap = remember(tasks) { tasks.associateBy { it.id } }
    val orderedTasks = activeTaskIds.mapNotNull { taskMap[it] }

    if (orderedTasks.isEmpty()) {
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val topTask = orderedTasks.first()

    // Drag offset for the top card
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 140.dp.toPx() }

    // Clear shake attempts when top card changes
    LaunchedEffect(topTask.id) {
        if (shakenCardId != topTask.id) {
            shakenCardId = null
            shakenAttempts = 0
            priorityWarningMessage = null
        }
        offsetX.snapTo(0f)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Deck Header Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Forest)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Card 1 of ${orderedTasks.size}",
                        color = SurfaceWarm,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val mandatoryCount = orderedTasks.count { it.priority == 1 }
                if (mandatoryCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Terracotta.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🚨 $mandatoryCount Mandatory",
                            color = Terracotta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "Swipe 👉 Complete • 👈 Back",
                fontSize = 11.sp,
                color = Mute,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Warning banner when user tries to left-swipe a priority 1 card
        AnimatedVisibility(visible = priorityWarningMessage != null) {
            priorityWarningMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Terracotta.copy(alpha = 0.12f))
                        .border(1.dp, Terracotta.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, fontSize = 12.sp, color = Terracotta, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Card Stack Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Render up to 3 cards in depth (back to front)
            val visibleCount = minOf(3, orderedTasks.size)

            for (i in (visibleCount - 1) downTo 0) {
                val task = orderedTasks[i]
                val isTop = (i == 0)

                val stackScale = 1f - (i * 0.05f)
                val stackOffsetY = (i * 14).dp

                if (isTop) {
                    // Top Card with Gestures
                    val rotationDeg = (offsetX.value / 25f).coerceIn(-18f, 18f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(offsetX.value.roundToInt(), stackOffsetY.roundToPx()) }
                            .scale(stackScale)
                            .rotate(rotationDeg)
                            .pointerInput(topTask.id) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        val currentX = offsetX.value
                                        coroutineScope.launch {
                                            if (currentX > swipeThreshold) {
                                                // SWIPE RIGHT: Complete Task
                                                offsetX.animateTo(
                                                    targetValue = 1200f,
                                                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                                                )
                                                onCompleteTask(topTask)
                                                activeTaskIds.remove(topTask.id)
                                                offsetX.snapTo(0f)
                                            } else if (currentX < -swipeThreshold) {
                                                // SWIPE LEFT: Move to back of deck
                                                // Check Priority 1 Mandatory rule:
                                                if (topTask.priority == 1 && (shakenCardId != topTask.id || shakenAttempts < 1)) {
                                                    // First attempt on Priority 1: SHAKE and return to position!
                                                    shakenCardId = topTask.id
                                                    shakenAttempts += 1
                                                    priorityWarningMessage = "🚨 Mandatory watering task! Swipe left again to send to back."

                                                    // Shake animation sequence
                                                    val shakeKeyframes = listOf(-40f, 40f, -30f, 30f, -15f, 0f)
                                                    for (shakeOffset in shakeKeyframes) {
                                                        offsetX.animateTo(
                                                            targetValue = shakeOffset,
                                                            animationSpec = tween(45, easing = FastOutSlowInEasing)
                                                        )
                                                    }
                                                } else {
                                                    // Allowed to send to back!
                                                    priorityWarningMessage = null
                                                    offsetX.animateTo(
                                                        targetValue = -1200f,
                                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                                    )
                                                    activeTaskIds.remove(topTask.id)
                                                    activeTaskIds.add(topTask.id)
                                                    shakenCardId = null
                                                    shakenAttempts = 0
                                                    offsetX.snapTo(0f)
                                                }
                                            } else {
                                                // Return to center
                                                offsetX.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                )
                                            }
                                        }
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        coroutineScope.launch {
                                            offsetX.snapTo(offsetX.value + dragAmount)
                                        }
                                    }
                                )
                            }
                    ) {
                        DeckCardContent(
                            task = topTask,
                            swipeOffset = offsetX.value,
                            onComplete = {
                                coroutineScope.launch {
                                    offsetX.animateTo(1200f, tween(260))
                                    onCompleteTask(topTask)
                                    activeTaskIds.remove(topTask.id)
                                    offsetX.snapTo(0f)
                                }
                            },
                            onSendToBack = {
                                coroutineScope.launch {
                                    if (topTask.priority == 1 && (shakenCardId != topTask.id || shakenAttempts < 1)) {
                                        shakenCardId = topTask.id
                                        shakenAttempts += 1
                                        priorityWarningMessage = "🚨 Mandatory watering task! Tap Send to Back again to bypass."
                                        val shakeKeyframes = listOf(-35f, 35f, -25f, 25f, -10f, 0f)
                                        for (shakeOffset in shakeKeyframes) {
                                            offsetX.animateTo(shakeOffset, tween(45))
                                        }
                                    } else {
                                        priorityWarningMessage = null
                                        offsetX.animateTo(-1200f, tween(260))
                                        activeTaskIds.remove(topTask.id)
                                        activeTaskIds.add(topTask.id)
                                        shakenCardId = null
                                        shakenAttempts = 0
                                        offsetX.snapTo(0f)
                                    }
                                }
                            },
                            onOpenSnapshot = {
                                onOpenSnapshotForSpace(topTask.targetId)
                            }
                        )
                    }
                } else {
                    // Background Deck Cards
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = stackOffsetY)
                            .scale(stackScale)
                            .graphicsLayer { alpha = 0.88f - (i * 0.15f) }
                    ) {
                        DeckCardContent(
                            task = task,
                            swipeOffset = 0f,
                            onComplete = {},
                            onSendToBack = {},
                            onOpenSnapshot = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckCardContent(
    task: DailyCareTaskEntity,
    swipeOffset: Float,
    onComplete: () -> Unit,
    onSendToBack: () -> Unit,
    onOpenSnapshot: () -> Unit
) {
    // Dynamic swipe indicator color overlay
    val swipeColor = when {
        swipeOffset > 80f -> LeafBright.copy(alpha = (swipeOffset / 400f).coerceIn(0f, 0.25f))
        swipeOffset < -80f -> Terracotta.copy(alpha = (-swipeOffset / 400f).coerceIn(0f, 0.25f))
        else -> Color.Transparent
    }

    val priorityColor = when (task.priority) {
        1 -> Terracotta
        2 -> SkyBlue
        else -> Forest
    }

    val priorityBadgeText = when (task.priority) {
        1 -> "🚨 Priority 1 • Mandatory"
        2 -> "📸 Priority 2 • Weekly Snapshot"
        else -> "✨ Priority 3 • Routine Care"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp)
            .border(
                width = if (task.priority == 1) 2.dp else 1.dp,
                color = if (task.priority == 1) Terracotta.copy(alpha = 0.5f) else Sand,
                shape = RoundedCornerShape(22.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(swipeColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Priority Badge & Urgency Status
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(priorityColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = priorityBadgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor
                            )
                        }

                        if (task.urgencyDays > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Terracotta.copy(alpha = 0.18f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Overdue ${task.urgencyDays}d",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Terracotta
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Leaf.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Due Today",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Forest
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hero Plant / Space Content
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (task.taskType == "SNAPSHOT") {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SkyBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = SkyBlue,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        } else if (task.targetPhotoUri != null) {
                            AsyncImage(
                                model = task.targetPhotoUri,
                                contentDescription = task.targetTitle,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Paper),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (task.taskType) {
                                        "WATER" -> "💧"
                                        "ROTATE" -> "🔄"
                                        "INSPECT" -> "🔍"
                                        "FEED" -> "🌱"
                                        "CLEAN" -> "✨"
                                        "PRUNE" -> "✂️"
                                        "REPOT" -> "🪴"
                                        else -> "🌿"
                                    },
                                    fontSize = 32.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Forest,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = task.targetTitle,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = task.targetSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Bark,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Guidance Note
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Paper)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "💡 ${task.description}",
                            fontSize = 12.sp,
                            color = Ink,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Bottom Action Buttons
                Column {
                    if (task.taskType == "SNAPSHOT") {
                        Button(
                            onClick = onOpenSnapshot,
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Take Snapshot for ${task.targetTitle}",
                                color = SurfaceWarm,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSendToBack,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Sand),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Bark),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Bark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Send to Back",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = onComplete,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (task.priority == 1) Terracotta else Moss
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SurfaceWarm, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mark Done",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWarm
                            )
                        }
                    }
                }
            }

            // Swipe Right Stamp Overlay ("COMPLETED")
            if (swipeOffset > 80f) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.TopStart)
                        .rotate(-16f)
                        .border(3.dp, LeafBright, RoundedCornerShape(8.dp))
                        .background(SurfaceWarm.copy(alpha = 0.9f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "DONE ✓",
                        color = LeafBright,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }

            // Swipe Left Stamp Overlay ("SKIP / BACK")
            if (swipeOffset < -80f) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.TopEnd)
                        .rotate(16f)
                        .border(3.dp, Terracotta, RoundedCornerShape(8.dp))
                        .background(SurfaceWarm.copy(alpha = 0.9f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (task.priority == 1) "MANDATORY ⚠️" else "SKIP ↶",
                        color = Terracotta,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
