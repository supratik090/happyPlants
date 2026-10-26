package com.hos.happy.plants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.HappyPlantsTheme
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.PaperDark

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val envViewModel: EnvViewModel by viewModels()
    private val gardenViewModel: GardenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Seed master plants (including Tamil, Telugu, Kannada, etc.) into Room DB on startup/install
        PlantMasterRepository(applicationContext)

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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Forest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
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
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Moss,
                    strokeWidth = 2.dp
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
