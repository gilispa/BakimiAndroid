package com.bakimi.domain.inventory

/**
 * Calculates how much of an ingredient still has to be bought.
 *
 * shortage = max(0, total required - stock)
 *
 * Both quantities must use the same base unit (g, ml or pieces).
 *
 * @param required total quantity required by the selected recipes.
 * @param stock quantity available in the inventory.
 * @return the missing quantity; never negative.
 */
fun calculateShortage(required: Double, stock: Double): Double =
    maxOf(0.0, required - stock)
