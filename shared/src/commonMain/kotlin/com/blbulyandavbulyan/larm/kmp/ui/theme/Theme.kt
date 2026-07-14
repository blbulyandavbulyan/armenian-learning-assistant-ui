package com.blbulyandavbulyan.larm.kmp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val gradientTop: Color,
    val gradientBottom: Color,
    val saveButton: Color,
    val emptyMessage: Color,
    val innerBoxBackground: Color,
    val unfocusedBorder: Color,
    val saveButtonLoadingRing: Color
)

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

private val DarkAppColors = AppColors(
    gradientTop = DarkBackgroundTop,
    gradientBottom = DarkBackgroundBottom,
    saveButton = DarkSaveButton,
    emptyMessage = Color.White.copy(alpha = 0.5f),
    innerBoxBackground = Color.White.copy(alpha = 0.05f),
    unfocusedBorder = Color.White.copy(alpha = 0.3f),
    saveButtonLoadingRing = Color.White
)

private val LightAppColors = AppColors(
    gradientTop = LightBackgroundTop,
    gradientBottom = LightBackgroundBottom,
    saveButton = LightSaveButton,
    emptyMessage = Color.Black.copy(alpha = 0.5f),
    innerBoxBackground = Color.Black.copy(alpha = 0.05f),
    unfocusedBorder = Color.Black.copy(alpha = 0.3f),
    saveButtonLoadingRing = PrimaryRed
)

private val DarkColorPalette = darkColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    background = DarkBackgroundBottom,
    onBackground = Color.White,
    surface = DarkBackgroundTop,
    onSurface = Color.White,
    onSurfaceVariant = DarkSubtitle,
    error = PrimaryRed
)

private val LightColorPalette = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    background = LightBackgroundBottom,
    onBackground = Color(0xFF212529),
    surface = LightBackgroundTop,
    onSurface = Color(0xFF212529),
    onSurfaceVariant = LightSubtitle,
    error = PrimaryRed
)

@Composable
fun ArmenianLearningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorPalette else LightColorPalette
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}
