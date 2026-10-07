package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CameraColorScheme = darkColorScheme(
    primary = CameraGold,
    onPrimary = CameraBlack,
    primaryContainer = CameraSurfaceElevated,
    onPrimaryContainer = CameraGoldLight,
    secondary = CameraWhite,
    onSecondary = CameraBlack,
    background = CameraBlack,
    onBackground = CameraWhite,
    surface = CameraSurfaceDark,
    onSurface = CameraWhite,
    surfaceVariant = CameraSurfaceElevated,
    onSurfaceVariant = CameraTextSecondary,
    error = CameraError,
    onError = CameraWhite
)

@Composable
fun RealShotTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CameraBlack.toArgb()
                window.navigationBarColor = CameraBlack.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = CameraColorScheme,
        typography = Typography,
        content = content
    )
}
