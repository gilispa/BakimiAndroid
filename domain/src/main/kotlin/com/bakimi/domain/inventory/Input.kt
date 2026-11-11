package com.bakimi.domain.inventory

/**
 * Parses the quantity typed by the user. Accepts a decimal point or comma ("1.5" or "1,5").
 *
 * Returns `null` if the text is not a number, is negative or is not finite.
 */
fun parseQuantity(text: String): Double? {
    val value = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
    return value.takeIf { isValidQuantity(it) }
}

/** Errors that prevent saving a food. */
enum class ItemError { EMPTY_NAME, INVALID_QUANTITY }

/** Checks that the food has a name and a finite, non-negative quantity. */
fun validate(item: InventoryItem): Set<ItemError> = buildSet {
    if (item.name.isBlank()) add(ItemError.EMPTY_NAME)
    if (!isValidQuantity(item.quantity)) add(ItemError.INVALID_QUANTITY)
}

private fun isValidQuantity(value: Double): Boolean = value.isFinite() && value >= 0.0
