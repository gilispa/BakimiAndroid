package com.bakimi.domain.shopping

import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.matchesIngredient
import com.bakimi.domain.ingredients.normalizeName
import com.bakimi.domain.inventory.InventoryItem
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.inventory.convert
import com.bakimi.domain.inventory.round

/**
 * Purchased product, with its actual quantity, ready to go into Mi refri.
 *
 * @property ingredient catalog ingredient; `null` for "other" products.
 */
data class Purchased(
    val ingredient: Ingredient?,
    val name: String,
    val quantity: Double,
    val unit: MeasureUnit,
)

/**
 * Calculates the inventory records to save in order to add [purchasedItems].
 *
 * Adds to an existing record of the same food in a compatible unit (preferring its usual location
 * and reusing depleted records); if there is none, creates a new one in the usual location. The
 * whole purchased quantity is added: a 1 kg bag adds 1 kg even if only 200 g were missing. Never
 * uses approximate equivalences to add.
 *
 * @return updated or new records (id 0), each one only once.
 */
fun restock(inventory: List<InventoryItem>, purchasedItems: List<Purchased>): List<InventoryItem> {
    // Working records; new ones are added here so two purchases of the same food end up together.
    val records = inventory.toMutableList()
    val modified = mutableSetOf<Int>()

    purchasedItems.filter { it.quantity > 0.0 }.forEach { purchased ->
        val index = destination(records, purchased)
        if (index == null) {
            records += InventoryItem(
                name = purchased.ingredient?.name ?: purchased.name.trim(),
                quantity = purchased.quantity,
                unit = purchased.unit,
                location = purchased.ingredient?.suggestedLocation ?: StorageLocation.PANTRY,
                ingredientId = purchased.ingredient?.id,
            )
            modified += records.lastIndex
        } else {
            val current = records[index]
            val increase = convert(purchased.quantity, purchased.unit, current.unit) ?: return@forEach
            records[index] = current.copy(
                quantity = round(current.quantity + increase, decimals = 3),
                ingredientId = current.ingredientId ?: purchased.ingredient?.id,
            )
            modified += index
        }
    }
    return modified.sorted().map { records[it] }
}

/** Record the purchase is added to: same food, compatible unit, preferably in its usual location. */
private fun destination(records: List<InventoryItem>, purchased: Purchased): Int? {
    val candidates = records.indices.filter { i ->
        val record = records[i]
        val sameFood = purchased.ingredient?.let { record.matchesIngredient(it) }
            ?: (record.ingredientId == null && normalizeName(record.name) == normalizeName(purchased.name))
        sameFood && record.unit.isCompatibleWith(purchased.unit)
    }
    val usualLocation = purchased.ingredient?.suggestedLocation
    return candidates.firstOrNull { records[it].location == usualLocation } ?: candidates.firstOrNull()
}
