package com.bakimi.domain.ingredients

import com.bakimi.domain.inventory.Dimension
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.inventory.round
import com.bakimi.domain.inventory.convert as convertUnit

/**
 * Result of converting a quantity of an ingredient.
 *
 * @property approximate the approximate weight per piece was used; the UI must say so.
 */
data class Conversion(val quantity: Double, val approximate: Boolean)

/**
 * Converts [quantity] [from] one unit [to] another for this ingredient.
 *
 * Between compatible units (g↔kg, ml↔l) the conversion is exact. Between pieces and weight it only
 * converts if the ingredient has [Ingredient.gramsPerPiece], and the result is approximate
 * (pieces are rounded to one decimal, because more precision would be misleading). Returns `null`
 * if there is no reasonable way to convert.
 */
fun Ingredient.convert(quantity: Double, from: MeasureUnit, to: MeasureUnit): Conversion? {
    convertUnit(quantity, from, to)?.let { return Conversion(it, approximate = false) }

    val grams = gramsPerPiece ?: return null
    val result = when {
        from == MeasureUnit.PIECE && to.dimension == Dimension.WEIGHT ->
            convertUnit(quantity * grams, MeasureUnit.GRAM, to)

        to == MeasureUnit.PIECE && from.dimension == Dimension.WEIGHT ->
            convertUnit(quantity, from, MeasureUnit.GRAM)?.let { it / grams }

        else -> null
    } ?: return null
    // Counting "33.33 tortillas" would be false precision; weight keeps gram precision (0.18 kg).
    val decimals = if (to == MeasureUnit.PIECE) 1 else 3
    return Conversion(round(result, decimals), approximate = true)
}
