package com.hos.happy.plants.feature.env.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.data.local.entity.RoomEnvSnapshotEntity
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.HappyTagBg
import com.hos.happy.plants.ui.theme.HappyTagText
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import com.hos.happy.plants.ui.theme.Terracotta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SnapshotConfirmationData(
    val snapshot: RoomEnvSnapshotEntity,
    val previousSnapshot: RoomEnvSnapshotEntity?,
    val roomName: String,
    val plantCount: Int,
    val daysSinceLeafClean: Int?,
    val leafCleaningTaskAdded: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapshotConfirmationBottomSheet(
    data: SnapshotConfirmationData,
    onDismiss: () -> Unit,
    onAddLeafCleaning: () -> Unit,
    onDeleteSnapshot: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val snapshot = data.snapshot
    val prev = data.previousSnapshot

    val dateFormatted = remember(snapshot.recordedAt) {
        SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(snapshot.recordedAt))
    }

    val plantCountText = remember(data.plantCount) {
        "${data.plantCount} ${if (data.plantCount == 1) "plant" else "plants"}"
    }

    val lightLabel = remember(snapshot.lightLux) {
        SnapshotLabelEvaluator.evaluateLight(snapshot.lightLux)
    }
    val tempLabel = remember(snapshot.temperatureCelsius) {
        SnapshotLabelEvaluator.evaluateTemperature(snapshot.temperatureCelsius)
    }
    val humLabel = remember(snapshot.humidityPercentage) {
        SnapshotLabelEvaluator.evaluateHumidity(snapshot.humidityPercentage)
    }

    val dustBadge = remember(snapshot.aqi) {
        SnapshotLabelEvaluator.evaluateDustBadge(snapshot.aqi)
    }

    val aqiVal = snapshot.aqi ?: 120
    val aqiCategory = remember(aqiVal) {
        SnapshotLabelEvaluator.getAqiCategory(aqiVal)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF9F7F2),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Sand) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // 1. Header Row: Circular checkmark + "Snapshot saved"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Forest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = SurfaceWarm,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Snapshot saved",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${data.roomName} • $dateFormatted • $plantCountText",
                        style = MaterialTheme.typography.bodySmall,
                        color = Mute,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. "THIS SPACE IS" Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HappyTagText.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = HappyTagBg.copy(alpha = 0.65f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "THIS SPACE IS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Forest.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${lightLabel.text} • ${tempLabel.text} • ${humLabel.text}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = SnapshotLabelEvaluator.getSpaceAdvice(
                            snapshot.lightLux,
                            snapshot.temperatureCelsius,
                            snapshot.humidityPercentage
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // 3. "Trends" Section (only shown if previous snapshot exists!)
            if (prev != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "TRENDS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Mute,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Sand.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(12.dp))

                // Light Trend Row
                TrendMetricRow(
                    metricName = "Light",
                    valueText = "${snapshot.lightLux.toInt()} lux",
                    label = lightLabel,
                    trendText = SnapshotLabelEvaluator.formatLightTrend(snapshot.lightLux, prev.lightLux),
                    startVal = prev.lightLux,
                    endVal = snapshot.lightLux,
                    sparklineColor = Color(0xFFD97706)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Sand.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Temperature Trend Row
                val currTemp = snapshot.temperatureCelsius ?: 25f
                val prevTemp = prev.temperatureCelsius ?: 25f
                TrendMetricRow(
                    metricName = "Temperature",
                    valueText = "${currTemp.toInt()}°C",
                    label = tempLabel,
                    trendText = SnapshotLabelEvaluator.formatTemperatureTrend(
                        snapshot.temperatureCelsius,
                        prev.temperatureCelsius
                    ),
                    startVal = prevTemp,
                    endVal = currTemp,
                    sparklineColor = Color(0xFF2E6B34)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Sand.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Humidity Trend Row
                val currHum = snapshot.humidityPercentage ?: 60f
                val prevHum = prev.humidityPercentage ?: 60f
                TrendMetricRow(
                    metricName = "Humidity",
                    valueText = "${currHum.toInt()}%",
                    label = humLabel,
                    trendText = SnapshotLabelEvaluator.formatHumidityTrend(
                        snapshot.humidityPercentage,
                        prev.humidityPercentage
                    ),
                    startVal = prevHum,
                    endVal = currHum,
                    sparklineColor = Color(0xFF0284C7)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. "Dust this week" Card (Number based, no graph needed)
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
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dust this week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            fontSize = 15.sp
                        )

                        MetricPill(label = dustBadge)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val leafWipeNotice = if (data.daysSinceLeafClean != null) {
                        "Leaves were last wiped ${data.daysSinceLeafClean} days ago, so they are probably dusty."
                    } else {
                        "Leaves haven't been wiped yet, so they are probably dusty."
                    }

                    Text(
                        text = "Average AQI $aqiVal ($aqiCategory). $leafWipeNotice",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (data.leafCleaningTaskAdded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(HappyTagBg)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = HappyTagText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Added to today's care tasks",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HappyTagText
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onAddLeafCleaning,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Moss)
                        ) {
                            Text(
                                text = "Add leaf cleaning to tasks",
                                color = Moss,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Done Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Forest)
            ) {
                Text(
                    text = "Done",
                    color = SurfaceWarm,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = onDeleteSnapshot,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete snapshot",
                    tint = Terracotta,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Delete snapshot (taken by mistake)",
                    color = Terracotta,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TrendMetricRow(
    metricName: String,
    valueText: String,
    label: MetricLabel,
    trendText: String,
    startVal: Float,
    endVal: Float,
    sparklineColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metricName,
                fontSize = 12.sp,
                color = Mute,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = valueText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )

                MetricPill(label = label)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = trendText,
                fontSize = 12.sp,
                color = Mute
            )
        }

        TrendSparkline(
            startVal = startVal,
            endVal = endVal,
            color = sparklineColor
        )
    }
}

@Composable
private fun MetricPill(label: MetricLabel) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(label.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label.text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = label.textColor
        )
    }
}

@Composable
private fun TrendSparkline(
    startVal: Float,
    endVal: Float,
    color: Color,
    modifier: Modifier = Modifier
        .width(68.dp)
        .height(30.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val diff = endVal - startVal

        val startY = when {
            diff > 0 -> h * 0.75f
            diff < 0 -> h * 0.25f
            else -> h * 0.5f
        }

        val endY = when {
            diff > 0 -> h * 0.25f
            diff < 0 -> h * 0.75f
            else -> h * 0.5f
        }

        val ctrlX1 = w * 0.35f
        val ctrlY1 = startY
        val ctrlX2 = w * 0.65f
        val ctrlY2 = endY

        val path = Path().apply {
            moveTo(2.dp.toPx(), startY)
            cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, w - 4.dp.toPx(), endY)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        drawCircle(
            color = color,
            radius = 3.5.dp.toPx(),
            center = Offset(w - 4.dp.toPx(), endY)
        )
    }
}
