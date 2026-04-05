package com.example.attendance.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF007AFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAF3FF),
    onPrimaryContainer = Color(0xFF001F3F),
    secondary = Color(0xFF5E6A7D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F4F9),
    onSecondaryContainer = Color(0xFF1A1F29),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF111827),
    outline = Color(0xFFD9DEE8)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF63A6FF),
    onPrimary = Color(0xFF001A33),
    primaryContainer = Color(0xFF0B2742),
    onPrimaryContainer = Color(0xFFD7E9FF),
    secondary = Color(0xFFB3BDCC),
    onSecondary = Color(0xFF1A2230),
    secondaryContainer = Color(0xFF2A3342),
    onSecondaryContainer = Color(0xFFE0E5ED),
    surface = Color(0xFF111317),
    onSurface = Color(0xFFE7EAF0),
    surfaceVariant = Color(0xFF1B1F26),
    onSurfaceVariant = Color(0xFF9EA5B3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF0B0D11),
    onBackground = Color(0xFFE7EAF0),
    outline = Color(0xFF303748)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

@Composable
fun AttendanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
