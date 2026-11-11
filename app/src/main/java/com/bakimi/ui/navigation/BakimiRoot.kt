package com.bakimi.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bakimi.ui.cooking.CookingModeViewModel
import com.bakimi.ui.cooking.CookingModeScreen
import com.bakimi.ui.shopping.AddProductScreen
import com.bakimi.ui.shopping.ShoppingScreen
import com.bakimi.ui.home.HomeScreen
import com.bakimi.ui.recipes.RecipeDetailViewModel
import com.bakimi.ui.recipes.RecipeDetailScreen
import com.bakimi.ui.fridge.ItemFormViewModel
import com.bakimi.ui.fridge.ItemFormScreen
import com.bakimi.ui.fridge.FridgeScreen
import com.bakimi.ui.week.WeekScreen

/** Form to add (no id) or edit (with id) a food in Mi refri. */
private const val ROUTE_ITEM_FORM = "fridge/item?${ItemFormViewModel.ARG_ID}={${ItemFormViewModel.ARG_ID}}"

private fun itemFormRoute(id: Long? = null): String =
    if (id == null) "fridge/item" else "fridge/item?${ItemFormViewModel.ARG_ID}=$id"

private const val ROUTE_RECIPE_DETAIL = "recipe/{${RecipeDetailViewModel.ARG_ID}}"

private fun recipeDetailRoute(id: String): String = "recipe/$id"

private const val ROUTE_ADD_PRODUCT = "shopping/add"

private const val ROUTE_COOKING_MODE =
    "cooking/{${CookingModeViewModel.ARG_RECIPE}}/{${CookingModeViewModel.ARG_SERVINGS}}"

private fun cookingModeRoute(recipeId: String, servings: Int): String = "cooking/$recipeId/$servings"

@Composable
fun BakimiRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // The bottom bar only appears in the four main sections.
    val showBar = currentDestination == null || Destination.entries.any { it.path == currentDestination.route }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == destination.path } == true,
                            onClick = { navController.goToSection(destination) },
                            // The visible label already names the destination; the icon is decorative.
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.HOME.path,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            Destination.entries.forEach { destination ->
                composable(destination.path) {
                    when (destination) {
                        Destination.HOME -> HomeScreen(
                            onOpenRecipe = { id -> navController.navigate(recipeDetailRoute(id)) },
                            onGoToFridge = { navController.goToSection(Destination.FRIDGE) },
                            onPlan = { navController.goToSection(Destination.WEEK) },
                        )

                        Destination.FRIDGE -> FridgeScreen(
                            onAdd = { navController.navigate(itemFormRoute()) },
                            onEdit = { id -> navController.navigate(itemFormRoute(id)) },
                        )

                        Destination.WEEK -> WeekScreen(
                            onOpenRecipe = { id -> navController.navigate(recipeDetailRoute(id)) },
                            onViewList = { navController.goToSection(Destination.SHOPPING) },
                        )

                        Destination.SHOPPING -> ShoppingScreen(
                            onAddProduct = { navController.navigate(ROUTE_ADD_PRODUCT) },
                            onPlan = { navController.goToSection(Destination.WEEK) },
                        )
                    }
                }
            }
            composable(
                route = ROUTE_ITEM_FORM,
                arguments = listOf(
                    navArgument(ItemFormViewModel.ARG_ID) {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                ),
            ) { entry ->
                ItemFormScreen(onFinish = { navController.navigateBackFrom(entry) })
            }
            composable(
                route = ROUTE_RECIPE_DETAIL,
                arguments = listOf(navArgument(RecipeDetailViewModel.ARG_ID) { type = NavType.StringType }),
            ) { entry ->
                RecipeDetailScreen(
                    onBack = { navController.navigateBackFrom(entry) },
                    onStartCooking = { id, servings -> navController.navigate(cookingModeRoute(id, servings)) },
                )
            }
            composable(
                route = ROUTE_COOKING_MODE,
                arguments = listOf(
                    navArgument(CookingModeViewModel.ARG_RECIPE) { type = NavType.StringType },
                    navArgument(CookingModeViewModel.ARG_SERVINGS) { type = NavType.IntType },
                ),
            ) { entry ->
                CookingModeScreen(onExit = { navController.navigateBackFrom(entry) })
            }
            composable(ROUTE_ADD_PRODUCT) { entry ->
                AddProductScreen(onFinish = { navController.navigateBackFrom(entry) })
            }
        }
    }
}

/** Switches section keeping each one's state, like the bottom bar. */
private fun NavController.goToSection(destination: Destination) {
    navigate(destination.path) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/** Goes back only if the screen is still active, so a double tap does not take the user out of Mi refri. */
private fun NavController.navigateBackFrom(entry: NavBackStackEntry) {
    if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        popBackStack()
    }
}
