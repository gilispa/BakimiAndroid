package com.bakimi.domain.ingredientes

import com.bakimi.domain.inventario.Magnitud
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.inventario.redondear
import com.bakimi.domain.inventario.convertir as convertirUnidad

/**
 * Resultado de convertir una cantidad de un ingrediente.
 *
 * @property aproximada se usó el peso aproximado por pieza; la interfaz debe indicarlo.
 */
data class Conversion(val cantidad: Double, val aproximada: Boolean)

/**
 * Convierte [cantidad] de [de] a [a] para este ingrediente.
 *
 * Entre unidades compatibles (g↔kg, ml↔l) la conversión es exacta. Entre piezas y peso solo se
 * convierte si el ingrediente tiene [Ingrediente.gramosPorPieza], y el resultado es aproximado
 * (las piezas se redondean a un decimal, porque más precisión sería engañosa). Devuelve `null`
 * si no hay forma razonable de convertir.
 */
fun Ingrediente.convertir(cantidad: Double, de: Unidad, a: Unidad): Conversion? {
    convertirUnidad(cantidad, de, a)?.let { return Conversion(it, aproximada = false) }

    val gramos = gramosPorPieza ?: return null
    val resultado = when {
        de == Unidad.PIEZA && a.magnitud == Magnitud.PESO ->
            convertirUnidad(cantidad * gramos, Unidad.GRAMO, a)

        a == Unidad.PIEZA && de.magnitud == Magnitud.PESO ->
            convertirUnidad(cantidad, de, Unidad.GRAMO)?.let { it / gramos }

        else -> null
    } ?: return null
    // Contar "33.33 tortillas" sería precisión falsa; en peso se conserva al gramo (0.18 kg).
    val decimales = if (a == Unidad.PIEZA) 1 else 3
    return Conversion(redondear(resultado, decimales), aproximada = true)
}
