package com.hos.happy.plants.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BotanicalColorScheme = lightColorScheme(
    primary = Moss,
    onPrimary = SurfaceWarm,
    primaryContainer = Leaf.copy(alpha = 0.35f),
    onPrimaryContainer = Forest,
    secondary = Bark,
    onSecondary = SurfaceWarm,
    secondaryContainer = Sand.copy(alpha = 0.4f),
    onSecondaryContainer = Ink,
    background = PaperDark,
    onBackground = Ink,
    surface = SurfaceWarm,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = Mute,
    outline = Sand
)

@Composable
fun HappyPlantsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = BotanicalColorScheme,
        typography = Typography,
        content = content
    )
}
