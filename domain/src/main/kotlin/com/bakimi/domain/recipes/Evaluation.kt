package com.bakimi.domain.recipes

import com.bakimi.domain.ingredients.decimalsFor
import com.bakimi.domain.ingredients.stockOf
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.calculateShortage
import com.bakimi.domain.inventory.round

enum class IngredientStatus {
    /** There is enough. */
    COVERED,

    /** There is some, but not enough. */
    INSUFFICIENT,

    /** Nothing is recorded. */
    MISSING,

    /** It is recorded in a unit that cannot be converted (for example, garlic in grams versus pieces). */
    OTHER_UNIT,
}

/**
 * @property required ingredient with the quantity already scaled to the chosen servings.
 * @property available stock in the recipe unit (compatible units only).
 * @property shortage quantity still needed, in the recipe unit.
 */
data class EvaluatedIngredient(
    val required: RecipeIngredient,
    val available: Double,
    val shortage: Double,
    val state: IngredientStatus,
    /** The stock was calculated with an approximate weight per piece. */
    val approximate: Boolean = false,
)

data class RecipeEvaluation(
    val recipe: Recipe,
    val servings: Int,
    val ingredients: List<EvaluatedIngredient>,
) {
    /** Ingredients that are not fully covered. */
    val missing: Int get() = ingredients.count { it.state != IngredientStatus.COVERED }

    /** Ingredients that are already covered. */
    val covered: Int get() = ingredients.size - missing

    /** Can be prepared with what is at home. */
    val isReady: Boolean get() = missing == 0
}

/**
 * Compares a recipe with the inventory for [servings] servings.
 *
 * Adds up the stock of every record that matches each ingredient and calculates
 * shortage = max(0, required − stock). Converts pieces↔weight only with the ingredient's
 * approximate weight per piece, and in that case marks the result as approximate.
 */
fun evaluateRecipe(recipe: Recipe, servings: Int, inventory: List<InventoryItem>): RecipeEvaluation {
    val factor = servings.toDouble() / recipe.baseServings
    val ingredients = recipe.ingredients.map { base ->
        val required = base.copy(quantity = round(base.quantity * factor, decimals = 3))
        val stock = stockOf(base.ingredient, base.unit, inventory)
        val shortage = round(
            calculateShortage(required.quantity, stock.quantity),
            decimalsFor(stock.approximate),
        )
        val state = when {
            shortage == 0.0 -> IngredientStatus.COVERED
            stock.hasOtherUnit -> IngredientStatus.OTHER_UNIT
            stock.quantity > 0.0 -> IngredientStatus.INSUFFICIENT
            else -> IngredientStatus.MISSING
        }
        EvaluatedIngredient(required, stock.quantity, shortage, state, stock.approximate)
    }
    return RecipeEvaluation(recipe, servings, ingredients)
}
