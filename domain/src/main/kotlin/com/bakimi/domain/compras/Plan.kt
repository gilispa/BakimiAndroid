package com.bakimi.domain.compras

import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.convertir
import com.bakimi.domain.ingredientes.decimalesPara
import com.bakimi.domain.ingredientes.existenciaDe
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.calcularFaltante
import com.bakimi.domain.inventario.redondear
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.recetas.Receta

/** Receta elegida para la semana con sus porciones. */
data class RecetaPlaneada(val receta: Receta, val porciones: Int)

/**
 * Lo que el plan completo necesita de un ingrediente, contando la existencia una sola vez.
 *
 * @property unidad unidad en la que se suman las recetas.
 * @property aproximado alguna cantidad salió de un peso aproximado por pieza.
 * @property recetas ids de las recetas del plan que usan el ingrediente.
 */
data class Necesidad(
    val ingrediente: Ingrediente,
    val unidad: Unidad,
    val requerido: Double,
    val disponible: Double,
    val faltante: Double,
    val aproximado: Boolean,
    val recetas: List<String>,
)

/**
 * Suma lo que piden todas las recetas del plan por ingrediente y resta la existencia una sola
 * vez: faltante = max(0, total requerido − existencia).
 */
fun consolidarPlan(plan: List<RecetaPlaneada>, inventario: List<ItemInventario>): List<Necesidad> {
    // Una entrada por ingrediente, en la unidad de la primera receta que lo pide.
    val acumulado = LinkedHashMap<String, Acumulado>()
    plan.forEach { (receta, porciones) ->
        val factor = porciones.toDouble() / receta.porcionesBase
        receta.ingredientes.forEach { usado ->
            val cantidad = usado.cantidad * factor
            val previo = acumulado[usado.ingrediente.id]
            val conversion = previo?.let { usado.ingrediente.convertir(cantidad, usado.unidad, it.unidad) }
            if (previo == null || conversion == null) {
                // Primera vez, o una unidad que no se puede sumar: va en su propia entrada.
                val clave = if (previo == null) usado.ingrediente.id else "${usado.ingrediente.id}|${usado.unidad}"
                val actual = acumulado.getOrPut(clave) { Acumulado(usado.ingrediente, usado.unidad) }
                actual.agregar(cantidad, aproximado = false, receta.id)
            } else {
                previo.agregar(conversion.cantidad, conversion.aproximada, receta.id)
            }
        }
    }
    return acumulado.values.map { it.aNecesidad(inventario) }
}

private class Acumulado(val ingrediente: Ingrediente, val unidad: Unidad) {
    var requerido = 0.0
    var aproximado = false
    val recetas = mutableListOf<String>()

    fun agregar(cantidad: Double, aproximado: Boolean, recetaId: String) {
        requerido += cantidad
        this.aproximado = this.aproximado || aproximado
        if (recetaId !in recetas) recetas += recetaId
    }

    fun aNecesidad(inventario: List<ItemInventario>): Necesidad {
        val existencia = existenciaDe(ingrediente, unidad, inventario)
        val aproximada = aproximado || existencia.aproximada
        val decimales = decimalesPara(aproximada)
        val total = redondear(requerido, decimales)
        return Necesidad(
            ingrediente = ingrediente,
            unidad = unidad,
            requerido = total,
            disponible = existencia.cantidad,
            faltante = redondear(calcularFaltante(total, existencia.cantidad), decimales),
            aproximado = aproximada,
            recetas = recetas.toList(),
        )
    }
}
