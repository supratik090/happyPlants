package com.hos.happy.plants.feature.garden.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.data.local.entity.DailyCareTaskEntity
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Mockup-aligned color palette
private val WaterBlue = Color(0xFF2172E5)
private val CleanPurple = Color(0xFF7E57C2)
private val CareOchre = Color(0xFFA15D1C)
private val WeeklyGreen = Color(0xFF2E7D32)
private val DarkCardBg = Color(0xFF1B2E24)
private val MintCircleBg = Color(0xFFE8F5E9)
private val HighAqiBg = Color(0xFFFFE0B2)
private val HighAqiText = Color(0xFFE65100)
private val TextDark = Color(0xFF1B2E24)
private val TextGrey = Color(0xFF718177)
private val CardBorderColor = Color(0xFFE5EAE5)
private val TrackColor = Color(0xFFE0E8E1)
private val DoneGreen = Color(0xFF2E7D32)
private val SkipSlate = Color(0xFF455A64)

@Composable
fun TodayActionsView(
    pendingTasks: List<DailyCareTaskEntity>,
    completedTasks: List<DailyCareTaskEntity>,
    isHighAqi: Boolean = true,
    onCompleteTask: (DailyCareTaskEntity) -> Unit,
    onSkipTask: (DailyCareTaskEntity) -> Unit,
    onUndoTask: (DailyCareTaskEntity) -> Unit,
    onMarkAllDoneForSpace: (taskType: String, spaceName: String) -> Unit,
    onOpenSnapshotForSpace: (String) -> Unit = {},
    onAddGardenPhoto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalTasks = pendingTasks.size + completedTasks.size
    val progress = if (totalTasks > 0) completedTasks.size.toFloat() / totalTasks.toFloat() else 0f

    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    // Partition tasks by category
    val waterTasks = pendingTasks.filter { it.taskType == "WATER" }
    val cleanTasks = pendingTasks.filter { it.taskType == "CLEAN" }
    val manureTasks = pendingTasks.filter { it.taskType in listOf("FEED_MANURE", "FEED") }
    val weeklySnapshotTasks = pendingTasks.filter { it.taskType == "SNAPSHOT" }
    val weeklyPhotoTasks = pendingTasks.filter { it.taskType == "GARDEN_PHOTO" }
    val plantCareTasks = pendingTasks.filter {
        it.taskType !in listOf("WATER", "CLEAN", "SNAPSHOT", "GARDEN_PHOTO", "FEED_MANURE", "FEED")
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // --- 1. Header Section ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = todayDateFormatted,
                fontSize = 13.sp,
                color = TextGrey,
                fontWeight = FontWeight.Normal
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Today",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${completedTasks.size} / $totalTasks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = WeeklyGreen
                    )
                    Text(
                        text = "tasks done",
                        fontSize = 11.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = WeeklyGreen,
                trackColor = TrackColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtle swipe guide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← Swipe left to skip",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextGrey
                )
                Text(
                    text = "Swipe right to mark done →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextGrey
                )
            }
        }

        // --- 2. Category: Water ---
        if (waterTasks.isNotEmpty()) {
            val spacesCount = waterTasks.map { it.spaceName.ifBlank { "Other" } }.distinct().size
            ActivityHeader(
                icon = Icons.Default.WaterDrop,
                iconBg = WaterBlue,
                title = "Water",
                subtitle = "${waterTasks.size} plants in $spacesCount spaces"
            )

            val groupedBySpace = waterTasks.groupBy { it.spaceName.ifBlank { "Other" } }
            groupedBySpace.forEach { (space, spaceTasks) ->
                SpaceSection(
                    spaceName = space,
                    plantCount = spaceTasks.size,
                    onMarkAllDone = { onMarkAllDoneForSpace("WATER", space) }
                ) {
                    spaceTasks.forEach { task ->
                        SwipeableTaskRow(
                            task = task,
                            onComplete = { onCompleteTask(task) },
                            onSkip = { onSkipTask(task) }
                        )
                    }
                }
            }
        }

        // --- 3. Category: Clean leaves ---
        if (cleanTasks.isNotEmpty()) {
            val spacesCount = cleanTasks.map { it.spaceName.ifBlank { "Other" } }.distinct().size
            ActivityHeader(
                icon = Icons.Default.AutoAwesome,
                iconBg = CleanPurple,
                title = "Clean leaves",
                subtitle = "${cleanTasks.size} plants in $spacesCount spaces",
                badgeText = if (isHighAqi) "High AQI" else null
            )

            val groupedBySpace = cleanTasks.groupBy { it.spaceName.ifBlank { "Other" } }
            groupedBySpace.forEach { (space, spaceTasks) ->
                SpaceSection(
                    spaceName = space,
                    plantCount = spaceTasks.size,
                    onMarkAllDone = { onMarkAllDoneForSpace("CLEAN", space) }
                ) {
                    spaceTasks.forEach { task ->
                        SwipeableTaskRow(
                            task = task,
                            onComplete = { onCompleteTask(task) },
                            onSkip = { onSkipTask(task) }
                        )
                    }
                }
            }
        }

        // --- Category: Feeding Manure (Rule 5 - Priority 3) ---
        if (manureTasks.isNotEmpty()) {
            val spacesCount = manureTasks.map { it.spaceName.ifBlank { "Other" } }.distinct().size
            ActivityHeader(
                icon = Icons.Default.Spa,
                iconBg = CareOchre,
                title = "Feeding Manure",
                subtitle = "${manureTasks.size} plants in $spacesCount spaces"
            )

            val groupedBySpace = manureTasks.groupBy { it.spaceName.ifBlank { "Other" } }
            groupedBySpace.forEach { (space, spaceTasks) ->
                SpaceSection(
                    spaceName = space,
                    plantCount = spaceTasks.size,
                    onMarkAllDone = { onMarkAllDoneForSpace("FEED_MANURE", space) }
                ) {
                    spaceTasks.forEach { task ->
                        SwipeableTaskRow(
                            task = task,
                            onComplete = { onCompleteTask(task) },
                            onSkip = { onSkipTask(task) }
                        )
                    }
                }
            }
        }

        // --- 4. Category: Plant care ---
        if (plantCareTasks.isNotEmpty()) {
            ActivityHeader(
                icon = Icons.Default.Yard,
                iconBg = WeeklyGreen,
                title = "Plant care",
                subtitle = "Single-plant tasks, not tied to a space"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                plantCareTasks.forEach { task ->
                    SwipeableTaskRow(
                        task = task,
                        onComplete = { onCompleteTask(task) },
                        onSkip = { onSkipTask(task) }
                    )
                }
            }
        }

        // State when all daily tasks are completed (shown above weekly task section)
        val hasPendingDailyTasks = waterTasks.isNotEmpty() || cleanTasks.isNotEmpty() ||
            manureTasks.isNotEmpty() || plantCareTasks.isNotEmpty()
        if (!hasPendingDailyTasks) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🌿", fontSize = 36.sp)
                    Text(
                        text = "All daily tasks completed for today!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Your plants are happy and thriving.",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }
            }
        }

        // --- 5. Category: Weekly tasks ---
        val hasWeeklyContent = weeklySnapshotTasks.isNotEmpty() || weeklyPhotoTasks.isNotEmpty()
        if (hasWeeklyContent) {
            ActivityHeader(
                icon = Icons.Default.CameraAlt,
                iconBg = WeeklyGreen,
                title = "Weekly tasks",
                subtitle = "Once a week"
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Task 1: Add a photo of your garden
                weeklyPhotoTasks.forEach { photoTask ->
                    SwipeableTaskContainer(
                        onComplete = { onCompleteTask(photoTask) },
                        onSkip = { onSkipTask(photoTask) }
                    ) {
                        WeeklyPhotoRow(
                            title = "Add a photo of your garden",
                            subtitle = "Weekly · due today",
                            onClick = onAddGardenPhoto
                        )
                    }
                }

                // Task 2: Weekly snapshot is due (Dark Card matching mockup)
                weeklySnapshotTasks.forEach { snapTask ->
                    SwipeableTaskContainer(
                        onComplete = { onCompleteTask(snapTask) },
                        onSkip = { onSkipTask(snapTask) }
                    ) {
                        WeeklySnapshotDarkCard(
                            title = "Weekly snapshot is due",
                            subtitle = "${snapTask.targetId} · snapshot due",
                            onClick = { onOpenSnapshotForSpace(snapTask.targetId) }
                        )
                    }
                }
            }
        }

        // --- 6. Completed Tasks Section (Revisit & Undo) ---
        if (completedTasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "✓ Completed Today (${completedTasks.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                completedTasks.forEach { task ->
                    CompletedTaskRow(
                        task = task,
                        onUndo = { onUndoTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityHeader(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Squircle Icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(HighAqiBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighAqiText
                        )
                    }
                }
            }

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextGrey
            )
        }
    }
}

@Composable
private fun SpaceSection(
    spaceName: String,
    plantCount: Int,
    onMarkAllDone: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Space header: "Living room  3 plants" | [ Mark all done ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = spaceName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "$plantCount plant${if (plantCount > 1) "s" else ""}",
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }

            // Pill-shaped "Mark all done" button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, WeeklyGreen, RoundedCornerShape(50))
                    .clickable { onMarkAllDone() }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mark all done",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WeeklyGreen
                )
            }
        }

        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTaskContainer(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onComplete()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onSkip()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val bgColor = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> DoneGreen
                SwipeToDismissBoxValue.EndToStart -> SkipSlate
                SwipeToDismissBoxValue.Settled -> Color.Transparent
            }

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.Settled -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .padding(horizontal = 18.dp),
                contentAlignment = alignment
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Done",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Skip",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Skip",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        content = {
            content()
        }
    )
}

@Composable
private fun SwipeableTaskRow(
    task: DailyCareTaskEntity,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    SwipeableTaskContainer(
        onComplete = onComplete,
        onSkip = onSkip
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mint green circle with leaf icon or photo
                if (!task.targetPhotoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = task.targetPhotoUri,
                        contentDescription = task.targetTitle,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MintCircleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = WeeklyGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.targetTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description.ifBlank { task.targetSubtitle },
                        fontSize = 12.sp,
                        color = TextGrey
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyPhotoRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MintCircleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = WeeklyGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextGrey
                )
            }
        }
    }
}

@Composable
private fun WeeklySnapshotDarkCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(WeeklyGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFFA3D9A5)
                )
            }

            Text(
                text = "›",
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CompletedTaskRow(
    task: DailyCareTaskEntity,
    onUndo: () -> Unit
) {
    val completedTimeStr = remember(task.completedAt) {
        val time = task.completedAt ?: task.createdAt
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(time))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(WeeklyGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${task.targetTitle} • ${task.title}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Text(
                    text = "${if (task.spaceName.isNotBlank()) "${task.spaceName} • " else ""}Completed at $completedTimeStr",
                    fontSize = 11.sp,
                    color = TextGrey
                )
            }

            TextButton(
                onClick = onUndo,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Undo",
                        tint = TextGrey,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Undo",
                        fontSize = 12.sp,
                        color = TextGrey
                    )
                }
            }
        }
    }
}
