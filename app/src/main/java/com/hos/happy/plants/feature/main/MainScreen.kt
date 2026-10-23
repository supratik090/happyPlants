package com.hos.happy.plants.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
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
import com.hos.happy.plants.feature.auth.AuthViewModel
import com.hos.happy.plants.feature.env.EnvScreen
import com.hos.happy.plants.feature.env.EnvViewModel
import com.hos.happy.plants.feature.garden.GardenPlaceholderScreen
import com.hos.happy.plants.feature.research.ResearchPlaceholderScreen
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Moss
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.SurfaceWarm

sealed class NavTab(val title: String, val icon: ImageVector) {
    data object Garden : NavTab("Garden", Icons.Default.Yard)
    data object Research : NavTab("Research", Icons.Default.MenuBook)
    data object Env : NavTab("Env", Icons.Default.WbSunny)
}

@Composable
fun MainScreen(
    envViewModel: EnvViewModel,
    authViewModel: AuthViewModel,
    onSwitchProfile: () -> Unit
) {
    val tabs = remember { listOf(NavTab.Garden, NavTab.Research, NavTab.Env) }
    // Start on Env tab as requested
    var selectedTabIndex by remember { mutableIntStateOf(2) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWarm,
                tonalElevation = 6.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
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
                0 -> GardenPlaceholderScreen()
                1 -> ResearchPlaceholderScreen()
                2 -> EnvScreen(
                    envViewModel = envViewModel,
                    authViewModel = authViewModel,
                    onSwitchProfile = onSwitchProfile
                )
            }
        }
    }
}
