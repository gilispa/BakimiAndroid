package com.bakimi.domain.inventory

import java.math.BigDecimal
import java.math.RoundingMode

/** Physical dimension a unit measures. Conversion only happens between units of the same dimension. */
enum class Dimension { WEIGHT, VOLUME, COUNT }

/**
 * Inventory units.
 *
 * @property baseFactor how many base units (g, ml or pieces) one unit equals.
 * @property quickStep amount the quick buttons in Mi refri add or subtract.
 */
enum class MeasureUnit(
    val dimension: Dimension,
    val baseFactor: Double,
    val quickStep: Double,
) {
    GRAM(Dimension.WEIGHT, 1.0, 100.0),
    KILOGRAM(Dimension.WEIGHT, 1000.0, 0.5),
    MILLILITER(Dimension.VOLUME, 1.0, 100.0),
    LITER(Dimension.VOLUME, 1000.0, 0.5),
    PIECE(Dimension.COUNT, 1.0, 1.0),
    ;

    fun isCompatibleWith(other: MeasureUnit): Boolean = dimension == other.dimension
}

/**
 * Converts [quantity] [from] one unit [to] another.
 *
 * Returns `null` if the units are not compatible: never converts pieces to weight or weight to volume.
 */
fun convert(quantity: Double, from: MeasureUnit, to: MeasureUnit): Double? {
    if (!from.isCompatibleWith(to)) return null
    return round(quantity * from.baseFactor / to.baseFactor, decimals = 6)
}

/** Rounds so that sums like 0.1 + 0.2 do not show floating-point leftovers. */
internal fun round(value: Double, decimals: Int): Double =
    BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).toDouble()
