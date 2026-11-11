package com.bakimi.domain.cooking

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.convert
import com.bakimi.domain.ingredients.matchesIngredient
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.inventory.round
import com.bakimi.domain.recipes.Recipe

/** Quantity of an ingredient used while preparing a recipe. */
data class UsedIngredient(val ingredient: Ingredient, val quantity: Double, val unit: MeasureUnit)

/**
 * Used quantity that was not recorded in Mi refri (there was none, or less). It is not deducted:
 * the user is told so they can correct their inventory if needed.
 */
data class Discrepancy(val ingredient: Ingredient, val unrecorded: Double, val unit: MeasureUnit)

/**
 * @property changes inventory records with their new quantity.
 * @property approximate some deduction used the approximate weight per piece.
 */
data class Consumption(val changes: List<InventoryItem>, val discrepancies: List<Discrepancy>, val approximate: Boolean)

/** Recipe quantities scaled to the prepared servings; the user can correct them. */
fun defaultUsage(recipe: Recipe, servings: Int): List<UsedIngredient> {
    val factor = servings.toDouble() / recipe.baseServings
    return recipe.ingredients.map {
        UsedIngredient(it.ingredient, round(it.quantity * factor, decimals = 3), it.unit)
    }
}

/**
 * Calculates how Mi refri looks after using [usedItems].
 *
 * Takes first from what expires soonest, can split across several records of the same food and
 * never leaves negative quantities: whatever is missing is reported as a [Discrepancy].
 */
fun calculateConsumption(usedItems: List<UsedIngredient>, inventory: List<InventoryItem>): Consumption {
    val records = inventory.toMutableList()
    val modified = mutableSetOf<Int>()
    val discrepancies = mutableListOf<Discrepancy>()
    var approximate = false

    usedItems.filter { it.quantity > 0.0 }.forEach { used ->
        var remaining = used.quantity
        val candidates = records.indices
            .filter { !records[it].depleted && records[it].matchesIngredient(used.ingredient) }
            .sortedWith(compareBy(nullsLast()) { i: Int -> records[i].expiry })

        for (i in candidates) {
            if (remaining <= TOLERANCE) break
            val record = records[i]
            val available = used.ingredient.convert(record.quantity, record.unit, used.unit) ?: continue
            approximate = approximate || available.approximate
            if (available.quantity <= remaining + TOLERANCE) {
                records[i] = record.copy(quantity = 0.0)
                remaining -= available.quantity
            } else {
                val remove = used.ingredient.convert(remaining, used.unit, record.unit) ?: continue
                records[i] = record.copy(quantity = maxOf(0.0, round(record.quantity - remove.quantity, decimals = 3)))
                remaining = 0.0
            }
            modified += i
        }

        if (remaining > TOLERANCE) {
            discrepancies += Discrepancy(used.ingredient, round(remaining, decimals = 3), used.unit)
        }
    }
    return Consumption(modified.sorted().map { records[it] }, discrepancies, approximate)
}

private const val TOLERANCE = 1e-6
