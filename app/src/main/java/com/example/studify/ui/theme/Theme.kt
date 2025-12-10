package com.example.studify.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Coffee,
    secondary = Banana,
    tertiary = Stone,
    background = Cream,
    surface = Paper,
    onPrimary = Color.White,
    onSecondary = Coffee,
    onBackground = Coffee,
    onSurface = Coffee,
    error = AccentRed
)

private val DarkColorScheme = darkColorScheme(
    primary = Coffee,
    secondary = Banana,
    tertiary = Stone,
    background = Color(0xFF2B211A),
    surface = Color(0xFF3A2A20),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFEFE9E3),
    onSurface = Color(0xFFEFE9E3),
    error = AccentRed
)

@Composable
fun StudifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        } else {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
