package com.hos.happy.plants.feature.garden.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hos.happy.plants.feature.garden.TodayCareTask
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm

@Composable
fun CareActionDialog(
    task: TodayCareTask,
    onDismiss: () -> Unit,
    onConfirm: (remark: String?, photoUri: String?) -> Unit
) {
    var remarkInput by remember { mutableStateOf("") }
    var attachedPhotoUri by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(task.activity.icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Mark ${task.activity.title} Done",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${task.plant.nickname} • ${task.plant.roomTag}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Bark
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "💡 ${task.goldenRule}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Forest,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = remarkInput,
                    onValueChange = { remarkInput = it },
                    placeholder = { Text("e.g. Soil was completely dry, gave 250ml") },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Moss,
                        cursorColor = Moss
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Photo attachment option (uses plant's current photo as reference or logs current snapshot)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Sand, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWarm)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (task.plant.photoUri != null) {
                                AsyncImage(
                                    model = task.plant.photoUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column {
                                Text("Growth Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Forest)
                                Text("Add to plant journey", fontSize = 10.sp, color = Mute)
                            }
                        }

                        Button(
                            onClick = {
                                // Uses plant photo or registers photo event
                                attachedPhotoUri = task.plant.photoUri
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (attachedPhotoUri != null) Moss else Sand
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (attachedPhotoUri != null) Icons.Default.Check else Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = SurfaceWarm,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (attachedPhotoUri != null) "Attached" else "Attach",
                                fontSize = 11.sp,
                                color = SurfaceWarm
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(remarkInput.ifBlank { null }, attachedPhotoUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm", color = SurfaceWarm, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Mute)
            }
        },
        containerColor = SurfaceWarm,
        shape = RoundedCornerShape(18.dp)
    )
}
