package com.bakimi.domain.cocina

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.convertir
import com.bakimi.domain.ingredientes.corresponde
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.inventario.redondear
import com.bakimi.domain.recetas.Receta

/** Cantidad de un ingrediente que se usó al preparar una receta. */
data class IngredienteUsado(val ingrediente: Ingrediente, val cantidad: Double, val unidad: Unidad)

/**
 * Lo usado que no estaba registrado en Mi refri (no había, o había menos). No se descuenta: se
 * avisa para que el usuario corrija su inventario si hace falta.
 */
data class Diferencia(val ingrediente: Ingrediente, val sinRegistrar: Double, val unidad: Unidad)

/**
 * @property cambios registros de inventario con su nueva cantidad.
 * @property aproximado algún descuento usó el peso aproximado por pieza.
 */
data class Consumo(val cambios: List<ItemInventario>, val diferencias: List<Diferencia>, val aproximado: Boolean)

/** Cantidades de la receta ajustadas a las porciones preparadas; el usuario puede corregirlas. */
fun usadosPorDefecto(receta: Receta, porciones: Int): List<IngredienteUsado> {
    val factor = porciones.toDouble() / receta.porcionesBase
    return receta.ingredientes.map {
        IngredienteUsado(it.ingrediente, redondear(it.cantidad * factor, decimales = 3), it.unidad)
    }
}

/**
 * Calcula cómo queda Mi refri después de usar [usados].
 *
 * Toma primero de lo que caduca antes, puede repartir entre varios registros del mismo alimento y
 * nunca deja cantidades negativas: lo que falte se reporta como [Diferencia].
 */
fun calcularConsumo(usados: List<IngredienteUsado>, inventario: List<ItemInventario>): Consumo {
    val registros = inventario.toMutableList()
    val modificados = mutableSetOf<Int>()
    val diferencias = mutableListOf<Diferencia>()
    var aproximado = false

    usados.filter { it.cantidad > 0.0 }.forEach { usado ->
        var restante = usado.cantidad
        val candidatos = registros.indices
            .filter { !registros[it].agotado && registros[it].corresponde(usado.ingrediente) }
            .sortedWith(compareBy(nullsLast()) { i: Int -> registros[i].caducidad })

        for (i in candidatos) {
            if (restante <= TOLERANCIA) break
            val registro = registros[i]
            val disponible = usado.ingrediente.convertir(registro.cantidad, registro.unidad, usado.unidad) ?: continue
            aproximado = aproximado || disponible.aproximada
            if (disponible.cantidad <= restante + TOLERANCIA) {
                registros[i] = registro.copy(cantidad = 0.0)
                restante -= disponible.cantidad
            } else {
                val quitar = usado.ingrediente.convertir(restante, usado.unidad, registro.unidad) ?: continue
                registros[i] = registro.copy(cantidad = maxOf(0.0, redondear(registro.cantidad - quitar.cantidad, decimales = 3)))
                restante = 0.0
            }
            modificados += i
        }

        if (restante > TOLERANCIA) {
            diferencias += Diferencia(usado.ingrediente, redondear(restante, decimales = 3), usado.unidad)
        }
    }
    return Consumo(modificados.sorted().map { registros[it] }, diferencias, aproximado)
}

private const val TOLERANCIA = 1e-6
