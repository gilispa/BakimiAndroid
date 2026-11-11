package com.bakimi.domain.ingredientes

import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.inventario.redondear

/**
 * Cuánto hay de un ingrediente en el inventario, expresado en una unidad.
 *
 * @property aproximada se usó el peso aproximado por pieza.
 * @property hayEnOtraUnidad hay registros que no se pudieron convertir (por ejemplo, ajo en gramos
 *   frente a piezas); su cantidad no está incluida.
 */
data class Existencia(val cantidad: Double, val aproximada: Boolean, val hayEnOtraUnidad: Boolean)

/**
 * Suma los registros del inventario que corresponden a [ingrediente], convertidos a [unidad].
 * Los registros agotados no cuentan.
 */
fun existenciaDe(ingrediente: Ingrediente, unidad: Unidad, inventario: List<ItemInventario>): Existencia {
    val conversiones = inventario
        .filter { !it.agotado && it.corresponde(ingrediente) }
        .map { ingrediente.convertir(it.cantidad, it.unidad, unidad) }
    val aproximada = conversiones.any { it?.aproximada == true }
    return Existencia(
        cantidad = redondear(conversiones.sumOf { it?.cantidad ?: 0.0 }, decimalesPara(aproximada)),
        aproximada = aproximada,
        hayEnOtraUnidad = conversiones.any { it == null },
    )
}

/** Con un peso aproximado por pieza, más de un decimal sería precisión falsa. */
fun decimalesPara(aproximada: Boolean): Int = if (aproximada) 1 else 3

/**
 * Un registro vinculado al catálogo se compara por id. Los registros sin vínculo (alimentos
 * "otro" o anteriores al catálogo) se comparan por nombre.
 */
fun ItemInventario.corresponde(ingrediente: Ingrediente): Boolean =
    ingredienteId?.let { it == ingrediente.id } ?: ingrediente.coincideCon(nombre)
