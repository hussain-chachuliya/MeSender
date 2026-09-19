package com.mesender.app.presentation.theme

import androidx.compose.ui.graphics.Color

enum class AppTheme(val label: String) {
    GREEN("Green"),
    BLUE("Blue"),
    PURPLE("Purple"),
    ROSE("Rose")
}

data class ThemeColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val error: Color
)

fun lightPalette(
    primary: Color, onPrimary: Color, primaryContainer: Color, onPrimaryContainer: Color,
    secondary: Color, onSecondary: Color, secondaryContainer: Color, onSecondaryContainer: Color,
    tertiary: Color, onTertiary: Color, background: Color, onBackground: Color,
    surface: Color, onSurface: Color, surfaceVariant: Color, onSurfaceVariant: Color, error: Color
) = ThemeColors(
    primary, onPrimary, primaryContainer, onPrimaryContainer,
    secondary, onSecondary, secondaryContainer, onSecondaryContainer,
    tertiary, onTertiary, background, onBackground,
    surface, onSurface, surfaceVariant, onSurfaceVariant, error
)

fun darkPalette(
    primary: Color, onPrimary: Color, primaryContainer: Color, onPrimaryContainer: Color,
    secondary: Color, onSecondary: Color, secondaryContainer: Color, onSecondaryContainer: Color,
    tertiary: Color, onTertiary: Color, background: Color, onBackground: Color,
    surface: Color, onSurface: Color, surfaceVariant: Color, onSurfaceVariant: Color, error: Color
) = ThemeColors(
    primary, onPrimary, primaryContainer, onPrimaryContainer,
    secondary, onSecondary, secondaryContainer, onSecondaryContainer,
    tertiary, onTertiary, background, onBackground,
    surface, onSurface, surfaceVariant, onSurfaceVariant, error
)

object AppColors {
    val GreenLight = lightPalette(
        primary = Color(0xFF4C662B), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCDEBA1), onPrimaryContainer = Color(0xFF102000),
        secondary = Color(0xFF586249), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDCE8C8), onSecondaryContainer = Color(0xFF151E0B),
        tertiary = Color(0xFF386663), onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFFCFDF8), onBackground = Color(0xFF1A1C16),
        surface = Color(0xFFFCFDF8), onSurface = Color(0xFF1A1C16),
        surfaceVariant = Color(0xFFE0E4D5), onSurfaceVariant = Color(0xFF44483D),
        error = Color(0xFFBA1A1A)
    )
    val GreenDark = darkPalette(
        primary = Color(0xFFB2D382), onPrimary = Color(0xFF223700),
        primaryContainer = Color(0xFF344E13), onPrimaryContainer = Color(0xFFCDEBA1),
        secondary = Color(0xFFC0CCA8), onSecondary = Color(0xFF2A3319),
        secondaryContainer = Color(0xFF404A32), onSecondaryContainer = Color(0xFFDCE8C8),
        tertiary = Color(0xFFA0CCC8), onTertiary = Color(0xFF053734),
        background = Color(0xFF1A1C16), onBackground = Color(0xFFE3E3DC),
        surface = Color(0xFF1A1C16), onSurface = Color(0xFFE3E3DC),
        surfaceVariant = Color(0xFF44483D), onSurfaceVariant = Color(0xFFC4C8B9),
        error = Color(0xFFFFB4AB)
    )

    val BlueLight = lightPalette(
        primary = Color(0xFF1A5FB4), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD4E3FF), onPrimaryContainer = Color(0xFF001C3B),
        secondary = Color(0xFF545F71), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD8E3F8), onSecondaryContainer = Color(0xFF111C2B),
        tertiary = Color(0xFF6E5676), onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFFDFBFF), onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFFDFBFF), onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFDFE2EB), onSurfaceVariant = Color(0xFF43474E),
        error = Color(0xFFBA1A1A)
    )
    val BlueDark = darkPalette(
        primary = Color(0xFFA6C8FF), onPrimary = Color(0xFF00315C),
        primaryContainer = Color(0xFF004883), onPrimaryContainer = Color(0xFFD4E3FF),
        secondary = Color(0xFFBCC7DB), onSecondary = Color(0xFF263141),
        secondaryContainer = Color(0xFF3C4758), onSecondaryContainer = Color(0xFFD8E3F8),
        tertiary = Color(0xFFDABEE3), onTertiary = Color(0xFF3E2848),
        background = Color(0xFF1A1C1E), onBackground = Color(0xFFE2E2E6),
        surface = Color(0xFF1A1C1E), onSurface = Color(0xFFE2E2E6),
        surfaceVariant = Color(0xFF43474E), onSurfaceVariant = Color(0xFFC3C7CF),
        error = Color(0xFFFFB4AB)
    )

    val PurpleLight = lightPalette(
        primary = Color(0xFF7B4FA0), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF2DAFF), onPrimaryContainer = Color(0xFF2D004E),
        secondary = Color(0xFF665A6E), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFECDDEF), onSecondaryContainer = Color(0xFF21182C),
        tertiary = Color(0xFF815252), onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFFFF7FF), onBackground = Color(0xFF1D1A20),
        surface = Color(0xFFFFF7FF), onSurface = Color(0xFF1D1A20),
        surfaceVariant = Color(0xFFE8DFE9), onSurfaceVariant = Color(0xFF4B444D),
        error = Color(0xFFBA1A1A)
    )
    val PurpleDark = darkPalette(
        primary = Color(0xFFE1B8FF), onPrimary = Color(0xFF472068),
        primaryContainer = Color(0xFF5F377E), onPrimaryContainer = Color(0xFFF2DAFF),
        secondary = Color(0xFFD1C1D8), onSecondary = Color(0xFF372C3F),
        secondaryContainer = Color(0xFF4E4256), onSecondaryContainer = Color(0xFFECDDFF),
        tertiary = Color(0xFFF4B8B8), onTertiary = Color(0xFF4C2525),
        background = Color(0xFF1D1A20), onBackground = Color(0xFFE7E0E5),
        surface = Color(0xFF1D1A20), onSurface = Color(0xFFE7E0E5),
        surfaceVariant = Color(0xFF4B444D), onSurfaceVariant = Color(0xFFCDC3CC),
        error = Color(0xFFFFB4AB)
    )

    val RoseLight = lightPalette(
        primary = Color(0xFF9C4146), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDAD8), onPrimaryContainer = Color(0xFF400009),
        secondary = Color(0xFF775655), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9D7), onSecondaryContainer = Color(0xFF2C1515),
        tertiary = Color(0xFF745A2E), onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFFFFBFF), onBackground = Color(0xFF201A1A),
        surface = Color(0xFFFFFBFF), onSurface = Color(0xFF201A1A),
        surfaceVariant = Color(0xFFF4DDDD), onSurfaceVariant = Color(0xFF534343),
        error = Color(0xFFBA1A1A)
    )
    val RoseDark = darkPalette(
        primary = Color(0xFFFFB3B4), onPrimary = Color(0xFF5F1420),
        primaryContainer = Color(0xFF7E2C32), onPrimaryContainer = Color(0xFFFFDAD8),
        secondary = Color(0xFFE7BDBC), onSecondary = Color(0xFF442929),
        secondaryContainer = Color(0xFF5D3F3F), onSecondaryContainer = Color(0xFFFFD9D7),
        tertiary = Color(0xFFE4BF8C), onTertiary = Color(0xFF432D06),
        background = Color(0xFF201A1A), onBackground = Color(0xFFEDE0E0),
        surface = Color(0xFF201A1A), onSurface = Color(0xFFEDE0E0),
        surfaceVariant = Color(0xFF534343), onSurfaceVariant = Color(0xFFD8C2C2),
        error = Color(0xFFFFB4AB)
    )

    fun lightFor(theme: AppTheme): ThemeColors = when (theme) {
        AppTheme.GREEN -> GreenLight
        AppTheme.BLUE -> BlueLight
        AppTheme.PURPLE -> PurpleLight
        AppTheme.ROSE -> RoseLight
    }

    fun darkFor(theme: AppTheme): ThemeColors = when (theme) {
        AppTheme.GREEN -> GreenDark
        AppTheme.BLUE -> BlueDark
        AppTheme.PURPLE -> PurpleDark
        AppTheme.ROSE -> RoseDark
    }
}
