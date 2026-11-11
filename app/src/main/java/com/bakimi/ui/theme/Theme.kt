package com.bakimi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BakimiColorScheme = lightColorScheme(
    primary = Coral,
    onPrimary = Eggplant,
    secondary = SageGreen,
    onSecondary = Eggplant,
    tertiary = ButterYellow,
    onTertiary = Eggplant,
    background = Cream,
    onBackground = Eggplant,
    surface = WarmWhite,
    onSurface = Eggplant,
    error = DeepCoral,
    onError = Color.White,
)

@Composable
fun BakimiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BakimiColorScheme,
        // Default Material 3 typography; the Nunito font is added later.
        typography = Typography(),
        content = content,
    )
}
