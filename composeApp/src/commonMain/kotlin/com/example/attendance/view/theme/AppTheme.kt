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
    primary = Color(0xFF203A63),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E7FF),
    onPrimaryContainer = Color(0xFF0E274A),
    secondary = Color(0xFF47617F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE7F5),
    onSecondaryContainer = Color(0xFF102338),
    tertiary = Color(0xFF716143),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4E9D6),
    onTertiaryContainer = Color(0xFF2F2412),
    surface = Color(0xFFF7F9FD),
    onSurface = Color(0xFF121C2E),
    surfaceVariant = Color(0xFFEAF0F8),
    onSurfaceVariant = Color(0xFF4F5F78),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    background = Color(0xFFEDF2FA),
    onBackground = Color(0xFF101A2A),
    outline = Color(0xFFACBAD1),
    outlineVariant = Color(0xFFD2DCEB),
    scrim = Color(0xFF0E1117)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA6BDE5),
    onPrimary = Color(0xFF11294A),
    primaryContainer = Color(0xFF1A355C),
    onPrimaryContainer = Color(0xFFDCE8FF),
    secondary = Color(0xFFB6C5D8),
    onSecondary = Color(0xFF203449),
    secondaryContainer = Color(0xFF30475F),
    onSecondaryContainer = Color(0xFFDEEAF9),
    tertiary = Color(0xFFD7BD93),
    onTertiary = Color(0xFF3D2F17),
    tertiaryContainer = Color(0xFF5B4828),
    onTertiaryContainer = Color(0xFFF7E7CA),
    surface = Color(0xFF0B1220),
    onSurface = Color(0xFFE7EEF9),
    surfaceVariant = Color(0xFF141F33),
    onSurfaceVariant = Color(0xFFA6B6CD),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF070E1A),
    onBackground = Color(0xFFE3EBF8),
    outline = Color(0xFF3E506C),
    outlineVariant = Color(0xFF27344A),
    scrim = Color(0xFF000000)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
    displayMedium = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp),
    displaySmall = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 23.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

data class AppTextSizes(
    val inputLabel: TextUnit = 13.sp,
    val inputText: TextUnit = 15.sp,
    val buttonText: TextUnit = 15.sp,
    val cardTitle: TextUnit = 18.sp,
    val cardSubtitle: TextUnit = 14.sp,
    val helperText: TextUnit = 12.sp
)

data class AppMetrics(
    val inputMinHeight: Dp = 42.dp,
    val buttonHeight: Dp = 48.dp,
    val cardRadius: Dp = 18.dp,
    val modalHorizontalPadding: Dp = 20.dp,
    val modalVerticalPadding: Dp = 14.dp,
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
