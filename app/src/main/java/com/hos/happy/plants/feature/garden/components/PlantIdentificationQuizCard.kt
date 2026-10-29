package com.hos.happy.plants.feature.garden.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.data.repository.PlantMasterRepository
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import kotlinx.coroutines.delay

private val CareOchre = Color(0xFFA15D1C)

data class PlantQuizQuestion(
    val targetPlant: MasterPlant,
    val options: List<String>
)

@Composable
fun PlantIdentificationQuizCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val masterRepo = remember(context) { PlantMasterRepository(context) }
    val allPlants = remember(masterRepo) { masterRepo.getAllPlants() }

    val candidates = remember(allPlants) {
        allPlants.filter {
            val url = it.displayImageUrl ?: it.image?.thumbnailUrl ?: it.image?.directImageUrl
            !url.isNullOrBlank() && it.commonName.isNotBlank()
        }
    }

    var currentQuestion by remember { mutableStateOf<PlantQuizQuestion?>(null) }
    var disabledOptions by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isAnsweredCorrectly by remember { mutableStateOf(false) }
    var isQuizFailed by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var streak by remember { mutableIntStateOf(0) }

    // Generate or regenerate a question
    fun loadNextQuestion() {
        if (candidates.size >= 4) {
            val target = candidates.random()
            val distractors = candidates
                .filter { it.id != target.id && it.commonName != target.commonName }
                .shuffled()
                .distinctBy { it.commonName }
                .take(3)
                .map { it.commonName }

            val options = (distractors + target.commonName).shuffled()
            currentQuestion = PlantQuizQuestion(targetPlant = target, options = options)
            disabledOptions = emptySet()
            isAnsweredCorrectly = false
            isQuizFailed = false
            selectedOption = null
        }
    }

    LaunchedEffect(candidates) {
        if (currentQuestion == null && candidates.isNotEmpty()) {
            loadNextQuestion()
        }
    }

    // Auto-advance to next plant after quiz failure
    LaunchedEffect(isQuizFailed) {
        if (isQuizFailed) {
            delay(2600L)
            if (isQuizFailed) {
                loadNextQuestion()
            }
        }
    }

    val question = currentQuestion

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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Clean vector Quiz icon + Title + Streak badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(Forest.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Identify the Plant",
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 15.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (streak > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF3E0),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streak Streak",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = {
                            streak = 0
                            loadNextQuestion()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Skip or New Question",
                            tint = Mute,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (question == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Moss,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                val plant = question.targetPlant
                val imageUrl = plant.displayImageUrl ?: plant.image?.thumbnailUrl ?: plant.image?.directImageUrl

                // Plant Image Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8EFE8)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!imageUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = "Guess this plant",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(190.dp),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(190.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Leaf,
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            error = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFlorist,
                                        contentDescription = null,
                                        tint = Moss,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Urban Plant Preview", fontSize = 11.sp, color = Mute)
                                }
                            }
                        )
                    }

                    // Floating prompt badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Forest.copy(alpha = 0.88f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = "Can you name this plant?",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Paper,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // 4 Multiple Choice Options (with clean letter indicators A, B, C, D)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    question.options.forEachIndexed { index, optionText ->
                        val isTarget = (optionText == plant.commonName)
                        val isDisabled = disabledOptions.contains(optionText)
                        val isChosenCorrect = isAnsweredCorrectly && isTarget
                        val isFailedSelection = isQuizFailed && selectedOption == optionText
                        val isCorrectReveal = isQuizFailed && isTarget

                        val optionLetter = ('A' + index).toString()

                        val bgColor by animateColorAsState(
                            targetValue = when {
                                isChosenCorrect || isCorrectReveal -> Color(0xFFE8F5E9)
                                isFailedSelection -> Color(0xFFFFEBEE)
                                isDisabled -> Color(0xFFF5F5F5)
                                else -> Paper
                            },
                            animationSpec = tween(250),
                            label = "optionBg"
                        )

                        val borderColor by animateColorAsState(
                            targetValue = when {
                                isChosenCorrect || isCorrectReveal -> Color(0xFF2E7D32)
                                isFailedSelection -> Color(0xFFE53935)
                                isDisabled -> Color(0xFFE0E0E0)
                                else -> Sand
                            },
                            animationSpec = tween(250),
                            label = "optionBorder"
                        )

                        val textColor = when {
                            isChosenCorrect || isCorrectReveal -> Color(0xFF1B5E20)
                            isFailedSelection -> Color(0xFFC62828)
                            isDisabled -> Color(0xFF9E9E9E)
                            else -> Ink
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    enabled = !isDisabled && !isAnsweredCorrectly && !isQuizFailed,
                                    onClick = {
                                        selectedOption = optionText
                                        if (isTarget) {
                                            isAnsweredCorrectly = true
                                            isQuizFailed = false
                                            streak++
                                        } else {
                                            val remainingOptionsCount = question.options.size - disabledOptions.size
                                            if (remainingOptionsCount > 2) {
                                                // More than 2 options remain: eliminate this choice
                                                disabledOptions = disabledOptions + optionText
                                            } else {
                                                // Exactly 2 options remain and user guessed wrong!
                                                // Quiz failed: reset streak and advance
                                                disabledOptions = disabledOptions + optionText
                                                isQuizFailed = true
                                                streak = 0
                                            }
                                        }
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Professional circular indicator
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = when {
                                                    isChosenCorrect || isCorrectReveal -> Color(0xFF2E7D32)
                                                    isFailedSelection -> Color(0xFFE53935)
                                                    isDisabled -> Color(0xFFE0E0E0)
                                                    else -> Sand.copy(alpha = 0.5f)
                                                },
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when {
                                            isChosenCorrect || isCorrectReveal -> Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Correct",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            isFailedSelection || isDisabled -> Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Wrong",
                                                tint = if (isFailedSelection) Color.White else Color(0xFF757575),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            else -> Text(
                                                text = optionLetter,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optionText,
                                        fontSize = 13.sp,
                                        fontWeight = if (isChosenCorrect || isCorrectReveal) FontWeight.Bold else FontWeight.Medium,
                                        color = textColor,
                                        textDecoration = if (isDisabled || isFailedSelection) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }

                                if (isChosenCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else if (isCorrectReveal) {
                                    Text(
                                        text = "Correct Plant",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                } else if (isFailedSelection) {
                                    Text(
                                        text = "Wrong",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                } else if (isDisabled) {
                                    Text(
                                        text = "Eliminated",
                                        fontSize = 10.sp,
                                        color = Color(0xFF9E9E9E),
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }

                // Success / Correct Choice Acknowledgment Banner
                AnimatedVisibility(
                    visible = isAnsweredCorrectly,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F5E9))
                            .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF1B5E20),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Brilliant! You spotted it!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "${plant.commonName} (${plant.botanicalName})",
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { loadNextQuestion() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Next Plant ➡️",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Quiz Failed Feedback Banner (When 2 options remained and user guessed wrong)
                AnimatedVisibility(
                    visible = isQuizFailed,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFFEF5350).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quiz Failed",
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Quiz Failed • Streak Reset to 0",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = "The plant was ${plant.commonName} (${plant.botanicalName})",
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF37474F)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Next plant in 2s...",
                                fontSize = 11.sp,
                                color = Color(0xFF757575),
                                fontStyle = FontStyle.Italic
                            )
                            Button(
                                onClick = { loadNextQuestion() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Next Plant ➡️",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // In-progress Hint (when 1 or 2 options were eliminated, before failing)
                if (!isAnsweredCorrectly && !isQuizFailed && disabledOptions.isNotEmpty()) {
                    val remaining = question.options.size - disabledOptions.size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (remaining > 2) {
                                "That's not it! $remaining choices remaining."
                            } else {
                                "Final chance: 2 choices remaining!"
                            },
                            fontSize = 11.sp,
                            color = if (remaining > 2) CareOchre else Color(0xFFD32F2F),
                            fontWeight = FontWeight.SemiBold
                        )

                        TextButton(
                            onClick = {
                                streak = 0
                                loadNextQuestion()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("Skip ⏩", fontSize = 11.sp, color = Mute)
                        }
                    }
                }
            }
        }
    }
}
