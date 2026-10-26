package com.hos.happy.plants.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.data.local.entity.UserProfileEntity
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Ink
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.Paper
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileChooserScreen(
    authViewModel: AuthViewModel,
    onProfileSelected: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val profiles by authViewModel.allProfiles.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()

    var selectedProfileForLogin by remember { mutableStateOf<UserProfileEntity?>(null) }
    var passwordInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Botanical App Header
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Forest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Leaf,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Happy Plants",
                style = MaterialTheme.typography.displayMedium,
                color = Forest
            )

            Text(
                text = "Choose your plant parent profile",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (uiState.isLoading && profiles.isEmpty()) {
                // Subtle loader while reading profiles from local DB
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Moss,
                        strokeWidth = 2.5.dp
                    )
                }
            } else if (profiles.isEmpty()) {
                // First launch state - no profiles created yet
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Sand)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome to Happy Plants",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Forest
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first local profile to begin tracking room micro-climates, light levels, and personal plant care.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mute
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToRegister,
                            colors = ButtonDefaults.buttonColors(containerColor = Moss),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SurfaceWarm)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Profile", color = SurfaceWarm, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // List of existing profiles that can be selected from
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(profiles) { profile ->
                        ProfileCardItem(
                            profile = profile,
                            onSelect = {
                                authViewModel.selectProfile(profile) {
                                    onProfileSelected()
                                }
                            },
                            onUnlockRequest = {
                                selectedProfileForLogin = profile
                                passwordInput = ""
                                authViewModel.clearError()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onNavigateToRegister,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Moss),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Moss)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Another Profile", color = Moss, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Login Password Dialog
        selectedProfileForLogin?.let { profile ->
            AlertDialog(
                onDismissRequest = {
                    selectedProfileForLogin = null
                    authViewModel.clearError()
                },
                title = {
                    Text(
                        text = "Unlock ${profile.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Forest
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter password to sign in as ${profile.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mute
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                authViewModel.clearError()
                            },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Bark)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Moss,
                                focusedLabelColor = Moss
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        uiState.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.loginWithProfile(profile, passwordInput) {
                                selectedProfileForLogin = null
                                onProfileSelected()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Moss)
                    ) {
                        Text("Log In", color = SurfaceWarm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        selectedProfileForLogin = null
                        authViewModel.clearError()
                    }) {
                        Text("Cancel", color = Mute)
                    }
                },
                containerColor = SurfaceWarm,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun ProfileCardItem(
    profile: UserProfileEntity,
    onSelect: () -> Unit,
    onUnlockRequest: () -> Unit
) {
    val lastActiveStr = remember(profile.lastLoginAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(profile.lastLoginAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .border(1.dp, Sand, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Forest),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.take(1).uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Leaf,
                    fontFamily = FontFamily.Serif
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Forest
                )
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mute
                )
                Text(
                    text = "Last active: $lastActiveStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = Bark
                )
            }

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = Moss),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Select", color = SurfaceWarm, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
