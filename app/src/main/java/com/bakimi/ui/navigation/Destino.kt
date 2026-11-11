package com.bakimi.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.bakimi.R

enum class Destino(
    val ruta: String,
    @StringRes val etiqueta: Int,
    val icono: ImageVector,
) {
    INICIO("inicio", R.string.nav_inicio, Icons.Default.Home),
    REFRI("refri", R.string.nav_refri, Icons.Default.Kitchen),
    SEMANA("semana", R.string.nav_semana, Icons.Default.DateRange),
    COMPRAS("compras", R.string.nav_compras, Icons.Default.ShoppingCart),
}
