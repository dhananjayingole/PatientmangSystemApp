package eu.tutorials.patientmanagementapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ── Brand Palette ─────────────────────────────────────────────────────────────
val Navy900   = Color(0xFF0A1628)
val Navy800   = Color(0xFF0D1F3C)
val Navy700   = Color(0xFF122247)
val Blue600   = Color(0xFF1565C0)
val Blue500   = Color(0xFF1976D2)
val Blue400   = Color(0xFF1E88E5)
val Cyan400   = Color(0xFF26C6DA)
val Teal500   = Color(0xFF00897B)
val RedAlert  = Color(0xFFD32F2F)
val RedLight  = Color(0xFFEF5350)
val Amber500  = Color(0xFFFFC107)
val Green500  = Color(0xFF43A047)
val Surface   = Color(0xFF0F1B2D)
val Surface2  = Color(0xFF162236)
val OnSurface = Color(0xFFE8EDF5)
val Outline   = Color(0xFF1E3A5F)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFFCE93D8),
    tertiary = Color(0xFFA5D6A7),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onTertiary = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    secondary = Color(0xFF7B1FA2),
    tertiary = Color(0xFF388E3C),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000)
)

@Composable
fun PatientManagementAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}