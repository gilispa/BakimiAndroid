package com.bakimi.domain.recetas

import com.bakimi.domain.ingredientes.decimalesPara
import com.bakimi.domain.ingredientes.existenciaDe
import com.bakimi.domain.inventario.ItemInventario
import com.bakimi.domain.inventario.calcularFaltante
import com.bakimi.domain.inventario.redondear

enum class EstadoIngrediente {
    /** Hay suficiente. */
    COMPLETO,

    /** Hay algo, pero no alcanza. */
    INSUFICIENTE,

    /** No hay nada registrado. */
    FALTA,

    /** Está registrado en una unidad que no se puede convertir (por ejemplo, ajo en gramos frente a piezas). */
    OTRA_UNIDAD,
}

/**
 * @property requerido ingrediente con la cantidad ya ajustada a las porciones elegidas.
 * @property disponible existencia en la unidad de la receta (solo unidades compatibles).
 * @property faltante cantidad por conseguir en la unidad de la receta.
 */
data class IngredienteEvaluado(
    val requerido: IngredienteReceta,
    val disponible: Double,
    val faltante: Double,
    val estado: EstadoIngrediente,
    /** La existencia se calculó con un peso aproximado por pieza. */
    val aproximado: Boolean = false,
)

data class EvaluacionReceta(
    val receta: Receta,
    val porciones: Int,
    val ingredientes: List<IngredienteEvaluado>,
) {
    /** Ingredientes que no están cubiertos por completo. */
    val faltantes: Int get() = ingredientes.count { it.estado != EstadoIngrediente.COMPLETO }

    /** Ingredientes que ya están cubiertos. */
    val completos: Int get() = ingredientes.size - faltantes

    /** Se puede preparar con lo que hay. */
    val lista: Boolean get() = faltantes == 0
}

/**
 * Compara una receta con el inventario para [porciones] porciones.
 *
 * Suma las existencias de todos los registros que coinciden con cada ingrediente y calcula
 * faltante = max(0, requerido − existencia). Convierte piezas↔peso solo con el peso aproximado
 * por pieza del ingrediente, y en ese caso marca el resultado como aproximado.
 */
fun evaluarReceta(receta: Receta, porciones: Int, inventario: List<ItemInventario>): EvaluacionReceta {
    val factor = porciones.toDouble() / receta.porcionesBase
    val ingredientes = receta.ingredientes.map { base ->
        val requerido = base.copy(cantidad = redondear(base.cantidad * factor, decimales = 3))
        val existencia = existenciaDe(base.ingrediente, base.unidad, inventario)
        val faltante = redondear(
            calcularFaltante(requerido.cantidad, existencia.cantidad),
            decimalesPara(existencia.aproximada),
        )
        val estado = when {
            faltante == 0.0 -> EstadoIngrediente.COMPLETO
            existencia.hayEnOtraUnidad -> EstadoIngrediente.OTRA_UNIDAD
            existencia.cantidad > 0.0 -> EstadoIngrediente.INSUFICIENTE
            else -> EstadoIngrediente.FALTA
        }
        IngredienteEvaluado(requerido, existencia.cantidad, faltante, estado, existencia.aproximada)
    }
    return EvaluacionReceta(receta, porciones, ingredientes)
}
