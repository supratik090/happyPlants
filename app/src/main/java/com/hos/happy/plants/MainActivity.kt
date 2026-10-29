package com.hos.happy.plants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hos.happy.plants.data.repository.PlantMasterRepository
import com.hos.happy.plants.feature.auth.AuthViewModel
import com.hos.happy.plants.feature.auth.ProfileChooserScreen
import com.hos.happy.plants.feature.auth.RegisterProfileScreen
import com.hos.happy.plants.feature.env.EnvViewModel
import com.hos.happy.plants.feature.garden.GardenViewModel
import com.hos.happy.plants.feature.main.MainScreen
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.HappyPlantsTheme
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.hos.happy.plants.data.notification.DailyNotificationScheduler

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val envViewModel: EnvViewModel by viewModels()
    private val gardenViewModel: GardenViewModel by viewModels()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Seed master plants (including Tamil, Telugu, Kannada, etc.) into Room DB on startup/install
        PlantMasterRepository(applicationContext)

        // Request POST_NOTIFICATIONS on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            HappyPlantsTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    envViewModel = envViewModel,
                    gardenViewModel = gardenViewModel
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    envViewModel: EnvViewModel,
    gardenViewModel: GardenViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    // While checking if a profile session is active, show the botanical splash screen
    if (authState.isSessionChecking) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PaperDark),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_app_splash),
                    contentDescription = "Metro Greens Logo",
                    modifier = Modifier
                        .size(130.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(26.dp))
                        .border(1.dp, Sand, androidx.compose.foundation.shape.RoundedCornerShape(26.dp))
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Metro Greens",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Forest
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Urban Plant Care & Botanical Encyclopedia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Bark
                )
                Spacer(modifier = Modifier.height(28.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Moss,
                    strokeWidth = 2.5.dp
                )
            }
        }
        return
    }

    // Determine start destination once session check completes
    val startDest = if (authState.activeProfile != null) "main" else "profile_chooser"

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable("profile_chooser") {
            ProfileChooserScreen(
                authViewModel = authViewModel,
                onProfileSelected = {
                    navController.navigate("main") {
                        popUpTo("profile_chooser") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegistrationSuccess = {
                    navController.navigate("main") {
                        popUpTo("profile_chooser") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                gardenViewModel = gardenViewModel,
                envViewModel = envViewModel,
                authViewModel = authViewModel,
                onSwitchProfile = {
                    authViewModel.switchProfile()
                    navController.navigate("profile_chooser") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}
