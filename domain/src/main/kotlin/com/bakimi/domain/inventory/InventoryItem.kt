package com.bakimi.domain.inventory

import java.time.LocalDate

/** Where a food is stored. The order defines the group order in Mi refri. */
enum class StorageLocation { FRIDGE, FREEZER, PANTRY }

/**
 * A food recorded in the inventory.
 *
 * @property id 0 until it is saved.
 * @property expiry optional date; `null` if the user did not record it.
 * @property ingredientId catalog ingredient it corresponds to; `null` if the user recorded it as an
 * "other" food outside the catalog (or in records created before the catalog).
 */
data class InventoryItem(
    val id: Long = 0,
    val name: String,
    val quantity: Double,
    val unit: MeasureUnit,
    val location: StorageLocation,
    val expiry: LocalDate? = null,
    val ingredientId: String? = null,
) {
    /** A depleted food keeps its record with zero quantity so it can be restocked. */
    val depleted: Boolean get() = quantity <= 0.0
}
