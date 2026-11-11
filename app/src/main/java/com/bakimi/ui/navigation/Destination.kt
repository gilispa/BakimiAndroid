package com.bakimi.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.bakimi.R

enum class Destination(
    val path: String,
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    HOME("home", R.string.nav_home, Icons.Default.Home),
    FRIDGE("fridge", R.string.nav_fridge, Icons.Default.Kitchen),
    WEEK("week", R.string.nav_week, Icons.Default.DateRange),
    SHOPPING("shopping", R.string.nav_shopping, Icons.Default.ShoppingCart),
}
