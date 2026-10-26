package com.hos.happy.plants.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hos.happy.plants.feature.auth.AuthViewModel
import com.hos.happy.plants.feature.env.EnvScreen
import com.hos.happy.plants.feature.env.EnvViewModel
import com.hos.happy.plants.feature.garden.GardenScreen
import com.hos.happy.plants.feature.garden.GardenViewModel
import com.hos.happy.plants.feature.research.ResearchScreen
import com.hos.happy.plants.feature.research.ResearchViewModel
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.SurfaceWarm

import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hos.happy.plants.feature.garden.TodayTasksScreen

sealed class NavTab(val title: String, val icon: ImageVector) {
    data object Task : NavTab("Task", Icons.Default.Checklist)
    data object Garden : NavTab("Garden", Icons.Default.Yard)
    data object Research : NavTab("Research", Icons.AutoMirrored.Filled.MenuBook)
    data object Env : NavTab("Env", Icons.Default.WbSunny)
}

@Composable
fun MainScreen(
    gardenViewModel: GardenViewModel,
    envViewModel: EnvViewModel,
    authViewModel: AuthViewModel,
    onSwitchProfile: () -> Unit
) {
    val pendingDailyTasks by gardenViewModel.pendingDailyTasks.collectAsState()
    val envUiState by envViewModel.uiState.collectAsState()
    val aqiVal = envUiState.weatherSnapshot?.aqi ?: 120
    val isHighAqi = aqiVal >= 100

    val tabs = remember { listOf(NavTab.Task, NavTab.Garden, NavTab.Research, NavTab.Env) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWarm,
                tonalElevation = 6.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isTaskTab = tab is NavTab.Task
                    val pendingCount = pendingDailyTasks.size

                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            if (isTaskTab && pendingCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = Leaf,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = pendingCount.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Forest,
                            selectedTextColor = Forest,
                            indicatorColor = Leaf.copy(alpha = 0.35f),
                            unselectedIconColor = Mute,
                            unselectedTextColor = Mute
                        )
                    )
                }
            }
        },
        containerColor = PaperDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTabIndex) {
                0 -> TodayTasksScreen(
                    gardenViewModel = gardenViewModel,
                    authViewModel = authViewModel,
                    onSwitchProfile = onSwitchProfile,
                    onNavigateToEnv = { roomTag ->
                        if (!roomTag.isNullOrBlank()) {
                            envViewModel.selectRoomTag(roomTag)
                        }
                        selectedTabIndex = 3
                    },
                    onNavigateToGarden = {
                        selectedTabIndex = 1
                    },
                    isHighAqi = isHighAqi
                )
                1 -> GardenScreen(
                    gardenViewModel = gardenViewModel,
                    authViewModel = authViewModel,
                    onSwitchProfile = onSwitchProfile,
                    onNavigateToEnv = { roomTag ->
                        if (!roomTag.isNullOrBlank()) {
                            envViewModel.selectRoomTag(roomTag)
                            envViewModel.triggerNavigateToHoldReading()
                        }
                        selectedTabIndex = 3
                    }
                )
                2 -> {
                    val researchViewModel: ResearchViewModel = viewModel()
                    ResearchScreen(
                        researchViewModel = researchViewModel,
                        gardenViewModel = gardenViewModel,
                        onSwitchProfile = onSwitchProfile
                    )
                }
                3 -> EnvScreen(
                    envViewModel = envViewModel,
                    authViewModel = authViewModel,
                    onSwitchProfile = onSwitchProfile
                )
            }
        }
    }
}
