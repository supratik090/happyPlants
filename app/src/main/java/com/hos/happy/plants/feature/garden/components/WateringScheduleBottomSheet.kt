package com.hos.happy.plants.feature.garden.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.data.local.entity.PlantEntity
import com.hos.happy.plants.data.model.WateringSchedule
import com.hos.happy.plants.data.model.WateringSchedule.Companion.fromEntity
import com.hos.happy.plants.data.model.WateringSchedule.Companion.scheduleLabel
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.Terracotta
import kotlinx.coroutines.launch

private val WaterBlue = Color(0xFF2172E5)
private val SelectedGreen = Color(0xFF4F6F3B)
private val CardBorder = Color(0xFFD8C8A4)
private val TextDark = Color(0xFF2B2118)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WateringScheduleBottomSheet(
    plant: PlantEntity,
    masterIntervalDays: Int,
    onDismiss: () -> Unit,
    onSave: (WateringSchedule) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Derive initial state from existing custom schedule or master data
    val existingSchedule = remember(plant) {
        fromEntity(plant.waterScheduleType, plant.waterScheduleValue)
    }

    var selectedPattern by remember {
        mutableStateOf(
            when (existingSchedule) {
                is WateringSchedule.Weekdays -> "WEEKDAYS"
                is WateringSchedule.Monthly -> "MONTHLY"
                else -> "INTERVAL"
            }
        )
    }

    // INTERVAL state
    var intervalDays by remember {
        mutableIntStateOf(
            (existingSchedule as? WateringSchedule.Interval)?.intervalDays ?: masterIntervalDays
        )
    }

    // WEEKDAYS state: 1=Mon..7=Sun
    val selectedWeekdays = remember {
        mutableStateListOf<Int>().apply {
            val init = (existingSchedule as? WateringSchedule.Weekdays)?.weekdays ?: emptyList()
            addAll(init)
        }
    }

    // MONTHLY state: list of dates 1..28
    val selectedDates = remember {
        mutableStateListOf<Int>().apply {
            val init = (existingSchedule as? WateringSchedule.Monthly)?.dates ?: emptyList()
            addAll(init)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWarm,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Text(
                text = "💧 Watering Schedule",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Forest
            )
            Text(
                text = "${plant.nickname} · ${plant.roomTag}",
                fontSize = 13.sp,
                color = Mute,
                modifier = Modifier.padding(top = 2.dp)
            )
            // Master data suggestion
            if (masterIntervalDays > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Leaf.copy(alpha = 0.18f))
                        .clickable {
                            selectedPattern = "INTERVAL"
                            intervalDays = masterIntervalDays
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Suggested: every ${masterIntervalDays} days",
                        fontSize = 12.sp,
                        color = Forest,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Pattern selector header
            Text(
                text = "Choose a watering pattern",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(10.dp))

            // ---- Pattern Card: INTERVAL ----
            PatternCard(
                isSelected = selectedPattern == "INTERVAL",
                emoji = "📅",
                title = "Every N Days",
                subtitle = "e.g. water every 7 days",
                onClick = { selectedPattern = "INTERVAL" }
            ) {
                if (selectedPattern == "INTERVAL") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Water every", fontSize = 13.sp, color = Mute)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StepperButton("-") {
                                if (intervalDays > 1) intervalDays--
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$intervalDays",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Forest
                                )
                                Text("days", fontSize = 11.sp, color = Mute)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            StepperButton("+") {
                                if (intervalDays < 60) intervalDays++
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Slider(
                        value = intervalDays.toFloat(),
                        onValueChange = { intervalDays = it.toInt() },
                        valueRange = 1f..60f,
                        steps = 58,
                        colors = SliderDefaults.colors(
                            thumbColor = Moss,
                            activeTrackColor = Moss,
                            inactiveTrackColor = Sand
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 day", fontSize = 11.sp, color = Mute)
                        Text("60 days", fontSize = 11.sp, color = Mute)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---- Pattern Card: WEEKDAYS ----
            PatternCard(
                isSelected = selectedPattern == "WEEKDAYS",
                emoji = "📆",
                title = "Specific Weekdays",
                subtitle = "e.g. every Monday & Thursday",
                onClick = { selectedPattern = "WEEKDAYS" }
            ) {
                if (selectedPattern == "WEEKDAYS") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select watering days:", fontSize = 13.sp, color = Mute)
                    Spacer(modifier = Modifier.height(10.dp))
                    WeekdayPicker(
                        selectedWeekdays = selectedWeekdays,
                        onToggle = { day ->
                            if (selectedWeekdays.contains(day)) selectedWeekdays.remove(day)
                            else selectedWeekdays.add(day)
                        }
                    )
                    if (selectedWeekdays.isNotEmpty()) {
                        val preview = WateringSchedule.Weekdays(selectedWeekdays.sorted()).scheduleLabel()
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Paper)
                                .padding(10.dp)
                        ) {
                            Text(
                                "💡 Selected: $preview",
                                fontSize = 12.sp,
                                color = Forest
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---- Pattern Card: MONTHLY ----
            PatternCard(
                isSelected = selectedPattern == "MONTHLY",
                emoji = "🗓️",
                title = "Dates of Month",
                subtitle = "e.g. 1st, 10th, 20th every month",
                onClick = { selectedPattern = "MONTHLY" }
            ) {
                if (selectedPattern == "MONTHLY") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select dates each month:", fontSize = 13.sp, color = Mute)
                    Spacer(modifier = Modifier.height(10.dp))
                    MonthDatePicker(
                        selectedDates = selectedDates,
                        onToggle = { date ->
                            if (selectedDates.contains(date)) selectedDates.remove(date)
                            else selectedDates.add(date)
                        }
                    )
                    if (selectedDates.isNotEmpty()) {
                        val preview = WateringSchedule.Monthly(selectedDates.sorted()).scheduleLabel()
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Paper)
                                .padding(10.dp)
                        ) {
                            Text(
                                "💡 Selected: $preview",
                                fontSize = 12.sp,
                                color = Forest
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            val isValid = when (selectedPattern) {
                "INTERVAL" -> intervalDays >= 1
                "WEEKDAYS" -> selectedWeekdays.isNotEmpty()
                "MONTHLY" -> selectedDates.isNotEmpty()
                else -> false
            }

            Button(
                onClick = {
                    if (!isValid) return@Button
                    val schedule: WateringSchedule = when (selectedPattern) {
                        "WEEKDAYS" -> WateringSchedule.Weekdays(selectedWeekdays.sorted())
                        "MONTHLY" -> WateringSchedule.Monthly(selectedDates.sorted())
                        else -> WateringSchedule.Interval(intervalDays)
                    }
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onSave(schedule)
                        onDismiss()
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Moss,
                    disabledContainerColor = Sand
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "💾  Save Watering Schedule",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---- Sub-components ----

@Composable
private fun PatternCard(
    isSelected: Boolean,
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFFE5EFE2) else Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Moss else CardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFFD0E8C8) else Color(0xFFF3EFDF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(subtitle, fontSize = 12.sp, color = Mute)
                }
                // Radio circle
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Moss else Color.Transparent)
                        .border(
                            width = if (isSelected) 0.dp else 2.dp,
                            color = if (isSelected) Moss else Color(0xFFBBBBBB),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (label == "+") Moss else Color.White)
            .border(2.dp, Moss, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (label == "+") Color.White else Moss
        )
    }
}

@Composable
private fun WeekdayPicker(
    selectedWeekdays: List<Int>,
    onToggle: (Int) -> Unit
) {
    // 1=Mon, 2=Tue, ... 7=Sun
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val fullNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEachIndexed { index, label ->
            val day = index + 1
            val isSelected = selectedWeekdays.contains(day)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Moss else Color.White)
                        .border(
                            width = if (isSelected) 0.dp else 2.dp,
                            color = if (isSelected) Moss else Color(0xFFDDDDDD),
                            shape = CircleShape
                        )
                        .clickable { onToggle(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF555555)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fullNames[index],
                    fontSize = 10.sp,
                    color = if (isSelected) Forest else Mute,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun MonthDatePicker(
    selectedDates: List<Int>,
    onToggle: (Int) -> Unit
) {
    // 4 rows × 7 columns = 28 dates
    val rows = (1..28).chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { date ->
                    val isSelected = selectedDates.contains(date)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(if (isSelected) WaterBlue else Color.White)
                            .border(
                                width = if (isSelected) 0.dp else 1.dp,
                                color = if (isSelected) WaterBlue else Color(0xFFDDDDDD),
                                shape = CircleShape
                            )
                            .clickable { onToggle(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$date",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF555555),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Fill remaining cells in last row
                repeat(7 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
