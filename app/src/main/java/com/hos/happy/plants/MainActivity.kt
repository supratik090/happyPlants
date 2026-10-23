package com.hos.happy.plants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hos.happy.plants.feature.auth.AuthViewModel
import com.hos.happy.plants.feature.auth.ProfileChooserScreen
import com.hos.happy.plants.feature.auth.RegisterProfileScreen
import com.hos.happy.plants.feature.env.EnvViewModel
import com.hos.happy.plants.feature.garden.GardenViewModel
import com.hos.happy.plants.feature.main.MainScreen
import com.hos.happy.plants.ui.theme.HappyPlantsTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val envViewModel: EnvViewModel by viewModels()
    private val gardenViewModel: GardenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

    // Determine start destination based on whether a profile is already unlocked
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
