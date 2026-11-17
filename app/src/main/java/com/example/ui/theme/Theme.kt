package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StadiumDarkColorScheme = darkColorScheme(
    primary = PitchEmerald,
    onPrimary = StadiumObsidian,
    primaryContainer = PitchEmeraldDeep,
    onPrimaryContainer = PitchEmeraldBright,
    secondary = TrophyGold,
    onSecondary = StadiumObsidian,
    secondaryContainer = TrophyGoldContainer,
    onSecondaryContainer = TrophyGold,
    tertiary = SkyAssist,
    onTertiary = StadiumObsidian,
    background = StadiumObsidian,
    onBackground = ChalkWhite,
    surface = StadiumSurface,
    onSurface = ChalkWhite,
    surfaceVariant = StadiumSurfaceElevated,
    onSurfaceVariant = ChalkMuted,
    outline = StadiumCardBorder,
    error = CrimsonCard,
    onError = Color.White,
    errorContainer = CrimsonContainer,
    onErrorContainer = Color(0xFFFCA5A5)
)

private val StadiumLightColorScheme = lightColorScheme(
    primary = LightPitchPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = PitchEmeraldDeep,
    secondary = TrophyGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = SkyAssist,
    background = LightPitchBackground,
    onBackground = LightOnPitch,
    surface = LightPitchSurface,
    onSurface = LightOnPitch,
    surfaceVariant = LightPitchSurfaceVariant,
    onSurfaceVariant = Color(0xFF3F594D),
    outline = Color(0xFFB7D3C4),
    error = CrimsonCard,
    onError = Color.White
)

val PitchDraftShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PitchDraftTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) StadiumDarkColorScheme else StadiumLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PitchDraftTypography,
        shapes = PitchDraftShapes,
        content = content
    )
}
