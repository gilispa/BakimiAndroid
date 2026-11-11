package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.convert
import com.bakimi.domain.ingredients.decimalsFor
import com.bakimi.domain.ingredients.stockOf
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.calculateShortage
import com.bakimi.domain.inventory.round
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.recipes.Recipe

/** Recipe chosen for the week, with its servings. */
data class PlannedRecipe(val recipe: Recipe, val servings: Int)

/**
 * What the whole plan needs of an ingredient, counting the stock only once.
 *
 * @property unit unit in which the recipes are added up.
 * @property approximate some quantity came from an approximate weight per piece.
 * @property recipes ids of the plan recipes that use the ingredient.
 */
data class Requirement(
    val ingredient: Ingredient,
    val unit: MeasureUnit,
    val required: Double,
    val available: Double,
    val shortage: Double,
    val approximate: Boolean,
    val recipes: List<String>,
)

/**
 * Adds up what every plan recipe asks for per ingredient and subtracts the stock only once:
 * shortage = max(0, total required − stock).
 */
fun consolidatePlan(plan: List<PlannedRecipe>, inventory: List<InventoryItem>): List<Requirement> {
    // One entry per ingredient, in the unit of the first recipe that asks for it.
    val accumulated = LinkedHashMap<String, Accumulator>()
    plan.forEach { (recipe, servings) ->
        val factor = servings.toDouble() / recipe.baseServings
        recipe.ingredients.forEach { used ->
            val quantity = used.quantity * factor
            val previous = accumulated[used.ingredient.id]
            val conversion = previous?.let { used.ingredient.convert(quantity, used.unit, it.unit) }
            if (previous == null || conversion == null) {
                // First time, or a unit that cannot be added up: it gets its own entry.
                val key = if (previous == null) used.ingredient.id else "${used.ingredient.id}|${used.unit}"
                val current = accumulated.getOrPut(key) { Accumulator(used.ingredient, used.unit) }
                current.add(quantity, approximate = false, recipe.id)
            } else {
                previous.add(conversion.quantity, conversion.approximate, recipe.id)
            }
        }
    }
    return accumulated.values.map { it.toRequirement(inventory) }
}

private class Accumulator(val ingredient: Ingredient, val unit: MeasureUnit) {
    var required = 0.0
    var approximate = false
    val recipes = mutableListOf<String>()

    fun add(quantity: Double, approximate: Boolean, recipeId: String) {
        required += quantity
        this.approximate = this.approximate || approximate
        if (recipeId !in recipes) recipes += recipeId
    }

    fun toRequirement(inventory: List<InventoryItem>): Requirement {
        val stock = stockOf(ingredient, unit, inventory)
        val approximate = approximate || stock.approximate
        val decimals = decimalsFor(approximate)
        val total = round(required, decimals)
        return Requirement(
            ingredient = ingredient,
            unit = unit,
            required = total,
            available = stock.quantity,
            shortage = round(calculateShortage(total, stock.quantity), decimals),
            approximate = approximate,
            recipes = recipes.toList(),
        )
    }
}
