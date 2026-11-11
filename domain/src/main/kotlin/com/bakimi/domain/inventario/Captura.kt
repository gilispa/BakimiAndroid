package com.bakimi.domain.inventario

/**
 * Interpreta la cantidad escrita por el usuario. Acepta punto o coma decimal ("1.5" o "1,5").
 *
 * Devuelve `null` si el texto no es un número, es negativo o no es finito.
 */
fun parsearCantidad(texto: String): Double? {
    val valor = texto.trim().replace(',', '.').toDoubleOrNull() ?: return null
    return valor.takeIf { esCantidadValida(it) }
}

/** Errores que impiden guardar un alimento. */
enum class ErrorItem { NOMBRE_VACIO, CANTIDAD_INVALIDA }

/** Revisa que el alimento tenga nombre y una cantidad finita no negativa. */
fun validar(item: ItemInventario): Set<ErrorItem> = buildSet {
    if (item.nombre.isBlank()) add(ErrorItem.NOMBRE_VACIO)
    if (!esCantidadValida(item.cantidad)) add(ErrorItem.CANTIDAD_INVALIDA)
}

private fun esCantidadValida(valor: Double): Boolean = valor.isFinite() && valor >= 0.0
