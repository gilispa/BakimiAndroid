package com.bakimi.domain.ingredients

import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.inventory.round

/**
 * How much of an ingredient there is in the inventory, expressed in one unit.
 *
 * @property approximate the approximate weight per piece was used.
 * @property hasOtherUnit there are records that could not be converted (for example, garlic in
 * grams versus pieces); their quantity is not included.
 */
data class Stock(val quantity: Double, val approximate: Boolean, val hasOtherUnit: Boolean)

/**
 * Adds up the inventory records that correspond to [ingredient], converted to [unit].
 * Depleted records do not count.
 */
fun stockOf(ingredient: Ingredient, unit: MeasureUnit, inventory: List<InventoryItem>): Stock {
    val conversions = inventory
        .filter { !it.depleted && it.matchesIngredient(ingredient) }
        .map { ingredient.convert(it.quantity, it.unit, unit) }
    val approximate = conversions.any { it?.approximate == true }
    return Stock(
        quantity = round(conversions.sumOf { it?.quantity ?: 0.0 }, decimalsFor(approximate)),
        approximate = approximate,
        hasOtherUnit = conversions.any { it == null },
    )
}

/** With an approximate weight per piece, more than one decimal would be false precision. */
fun decimalsFor(approximate: Boolean): Int = if (approximate) 1 else 3

/**
 * A record linked to the catalog is compared by id. Unlinked records ("other" foods or records
 * created before the catalog) are compared by name.
 */
fun InventoryItem.matchesIngredient(ingredient: Ingredient): Boolean =
    ingredientId?.let { it == ingredient.id } ?: ingredient.matches(name)
