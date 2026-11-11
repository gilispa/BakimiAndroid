package com.bakimi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BakimiColorScheme = lightColorScheme(
    primary = Coral,
    onPrimary = Berenjena,
    secondary = VerdeSalvia,
    onSecondary = Berenjena,
    tertiary = AmarilloMantequilla,
    onTertiary = Berenjena,
    background = Crema,
    onBackground = Berenjena,
    surface = BlancoCalido,
    onSurface = Berenjena,
    error = CoralProfundo,
    onError = Color.White,
)

@Composable
fun BakimiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BakimiColorScheme,
        // Tipografía por defecto de Material 3; la fuente Nunito se agrega después.
        typography = Typography(),
        content = content,
    )
}
