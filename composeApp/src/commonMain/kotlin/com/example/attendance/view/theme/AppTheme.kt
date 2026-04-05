package com.example.attendance.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F355E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6F5),
    onPrimaryContainer = Color(0xFF102241),
    secondary = Color(0xFF4E596B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EDF4),
    onSecondaryContainer = Color(0xFF1F2938),
    tertiary = Color(0xFF6F5A3B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF1E9DC),
    onTertiaryContainer = Color(0xFF2E2414),
    surface = Color(0xFFF8F9FC),
    onSurface = Color(0xFF161C28),
    surfaceVariant = Color(0xFFEEF2F8),
    onSurfaceVariant = Color(0xFF5A667D),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    background = Color(0xFFEBEEF4),
    onBackground = Color(0xFF161D2A),
    outline = Color(0xFFB8C2D5),
    outlineVariant = Color(0xFFD9E0EC),
    scrim = Color(0xFF0E1117)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BB0DC),
    onPrimary = Color(0xFF142846),
    primaryContainer = Color(0xFF1B3156),
    onPrimaryContainer = Color(0xFFDCE5F9),
    secondary = Color(0xFFB0BACC),
    onSecondary = Color(0xFF253040),
    secondaryContainer = Color(0xFF2E394B),
    onSecondaryContainer = Color(0xFFDCE3F1),
    tertiary = Color(0xFFD0B58D),
    onTertiary = Color(0xFF3A2D18),
    tertiaryContainer = Color(0xFF57452B),
    onTertiaryContainer = Color(0xFFF4E5CC),
    surface = Color(0xFF0A0F18),
    onSurface = Color(0xFFE7ECF6),
    surfaceVariant = Color(0xFF121A28),
    onSurfaceVariant = Color(0xFF9EABC2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF060A12),
    onBackground = Color(0xFFE4E9F3),
    outline = Color(0xFF35415A),
    outlineVariant = Color(0xFF212B3E),
    scrim = Color(0xFF000000)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.SemiBold),
    displayMedium = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.SemiBold),
    displaySmall = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

data class AppTextSizes(
    val inputLabel: TextUnit = 13.sp,
    val inputText: TextUnit = 14.sp,
    val buttonText: TextUnit = 14.sp,
    val cardTitle: TextUnit = 18.sp,
    val cardSubtitle: TextUnit = 14.sp,
    val helperText: TextUnit = 12.sp
)

data class AppMetrics(
    val inputMinHeight: Dp = 55.dp,
    val buttonHeight: Dp = 46.dp,
    val cardRadius: Dp = 16.dp,
    val modalHorizontalPadding: Dp = 20.dp,
    val modalVerticalPadding: Dp = 12.dp,
    val thinBorder: Dp = 1.dp
)

private val LocalAppTextSizes = staticCompositionLocalOf { AppTextSizes() }
private val LocalAppMetrics = staticCompositionLocalOf { AppMetrics() }

object AttendanceThemeTokens {
    val textSizes: AppTextSizes
        @Composable get() = LocalAppTextSizes.current

    val metrics: AppMetrics
        @Composable get() = LocalAppMetrics.current
}

@Composable
fun AttendanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val textSizes = AppTextSizes()
    val metrics = AppMetrics()

    CompositionLocalProvider(
        LocalAppTextSizes provides textSizes,
        LocalAppMetrics provides metrics
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
