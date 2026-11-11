package com.bakimi.domain.inventario

import java.math.BigDecimal
import java.math.RoundingMode

/** Magnitud física que mide una unidad. Solo se convierte entre unidades de la misma magnitud. */
enum class Magnitud { PESO, VOLUMEN, PIEZAS }

/**
 * Unidades del inventario.
 *
 * @property factorBase cuántas unidades base (g, ml o piezas) equivale una unidad.
 * @property pasoRapido cantidad que suman o restan los botones rápidos de Mi refri.
 */
enum class Unidad(
    val magnitud: Magnitud,
    val factorBase: Double,
    val pasoRapido: Double,
) {
    GRAMO(Magnitud.PESO, 1.0, 100.0),
    KILOGRAMO(Magnitud.PESO, 1000.0, 0.5),
    MILILITRO(Magnitud.VOLUMEN, 1.0, 100.0),
    LITRO(Magnitud.VOLUMEN, 1000.0, 0.5),
    PIEZA(Magnitud.PIEZAS, 1.0, 1.0),
    ;

    fun esCompatibleCon(otra: Unidad): Boolean = magnitud == otra.magnitud
}

/**
 * Convierte [cantidad] de la unidad [de] a la unidad [a].
 *
 * Devuelve `null` si las unidades no son compatibles: nunca convierte piezas a peso ni peso a volumen.
 */
fun convertir(cantidad: Double, de: Unidad, a: Unidad): Double? {
    if (!de.esCompatibleCon(a)) return null
    return redondear(cantidad * de.factorBase / a.factorBase, decimales = 6)
}

/** Redondea para que sumas como 0.1 + 0.2 no muestren residuos de punto flotante. */
internal fun redondear(valor: Double, decimales: Int): Double =
    BigDecimal.valueOf(valor).setScale(decimales, RoundingMode.HALF_UP).toDouble()
